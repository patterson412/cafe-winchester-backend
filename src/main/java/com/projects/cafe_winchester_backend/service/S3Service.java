package com.projects.cafe_winchester_backend.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final CloudFrontClient cloudFrontClient;
    private final String bucketName;
    private final String region;
    private final String cloudFrontDomain;
    private final String cloudFrontKeyPairId;
    private final String cloudFrontPrivateKeyPath;

    public S3Service(
            S3Client s3Client,
            CloudFrontClient cloudFrontClient,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.cloudfront.domain}") String cloudFrontDomain,
            @Value("${aws.cloudfront.key-pair-id}") String cloudFrontKeyPairId,
            @Value("${aws.cloudfront.private-key-path}") String cloudFrontPrivateKeyPath) {
        this.s3Client = s3Client;
        this.cloudFrontClient = cloudFrontClient;
        this.bucketName = bucketName;
        this.region = region;
        this.cloudFrontDomain = cloudFrontDomain;
        this.cloudFrontKeyPairId = cloudFrontKeyPairId;
        this.cloudFrontPrivateKeyPath = cloudFrontPrivateKeyPath;
    }

    public enum ImageType {
        MENU_ITEM("menu-items/"),
        SHOP("shop/"),
        BANNER("banners/"),
        PROFILE("profile/");

        private final String path;

        ImageType(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println("S3Service shutdown initiated...");
        cleanupBucketFolders();
        System.out.println("S3Service shutdown completed.");
    }

    public String uploadFile(MultipartFile file, ImageType imageType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        // Image validation
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        try {
            // Generate filename with folder structure
            String fileName = imageType.getPath() + generateUniqueFileName(file.getOriginalFilename());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(),
                            file.getSize()));

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    @Cacheable(
            // The name of the cache in Redis where the URLs will be stored
            value = "imageKeys",

            // Use the objectKey parameter as the cache key (e.g., "menu-items/photo1.jpg")
            key = "#objectKey",

            // Only execute caching if objectKey is not null and not empty string
            // This prevents unnecessary method execution for invalid inputs
            condition = "#objectKey != null && #objectKey.length() > 0",

            // Don't cache if the method returns null
            // This prevents caching failed URL generations
            unless = "#result == null"
    )
    public String generateSignedUrl(String objectKey) {
        try {
            // Read the private key file as binary for .der format
            byte[] privateKeyBytes = Files.readAllBytes(Path.of(cloudFrontPrivateKeyPath));

            // Create a private key specification directly from the DER bytes
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

            // Get a KeyFactory instance for RSA
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");

            // Generate the private key
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Create a CloudFront signer
            CloudFrontUrlSigner signer = new CloudFrontUrlSigner(cloudFrontKeyPairId, privateKey);

            // Generate the signed URL (valid for 1 hour)
            String resourcePath = "/" + objectKey;
            Instant expirationTime = Instant.now().plus(Duration.ofHours(1));

            return signer.generateSignedUrl(
                    "https://" + cloudFrontDomain + resourcePath,
                    expirationTime
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signed URL: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("File key cannot be null or empty");
        }

        try {
            // Check if file exists before deleting
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                throw new RuntimeException("File does not exist in S3");
            }

            // Delete the file
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    public void deleteMultipleFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            throw new IllegalArgumentException("File URLs list cannot be null or empty");
        }

        List<ObjectIdentifier> keys = new ArrayList<>();
        List<String> failedKeys = new ArrayList<>();

        for (String key : fileKeys) {
            try {
                keys.add(ObjectIdentifier.builder().key(key).build());
            } catch (Exception e) {
                failedKeys.add(key);
            }
        }

        if (!failedKeys.isEmpty()) {
            throw new IllegalArgumentException("Invalid URLs: " + String.join(", ", failedKeys));
        }

        try {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(keys).build())
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);

            if (!response.errors().isEmpty()) {
                List<String> errorMessages = response.errors().stream()
                        .map(error -> error.key() + ": " + error.message())
                        .toList();
                throw new RuntimeException("Failed to delete some files: " + String.join(", ", errorMessages));
            }
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete files from S3: " + e.getMessage(), e);
        }
    }

    public void cleanupBucketFolders() {
        try {
            List<String> foldersToClean = new ArrayList<>();
            for (ImageType imageType : ImageType.values()) {
                foldersToClean.add(imageType.getPath());
            }

            for (String folder : foldersToClean) {
                ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folder)
                        .build();

                ListObjectsV2Response listResponse;
                do {
                    listResponse = s3Client.listObjectsV2(listRequest);

                    if (!listResponse.contents().isEmpty()) {
                        List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                                .map(s3Object -> ObjectIdentifier.builder()
                                        .key(s3Object.key())
                                        .build())
                                .toList();

                        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                                .bucket(bucketName)
                                .delete(Delete.builder()
                                        .objects(objectsToDelete)
                                        .quiet(false)
                                        .build())
                                .build();

                        DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);

                        if (!deleteResponse.errors().isEmpty()) {
                            List<String> errorMessages = deleteResponse.errors().stream()
                                    .map(error -> error.key() + ": " + error.message())
                                    .toList();
                            System.err.println("Failed to delete some objects in folder " + folder + ": "
                                    + String.join(", ", errorMessages));
                        }
                    }

                    listRequest = ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .prefix(folder)
                            .continuationToken(listResponse.nextContinuationToken())
                            .build();

                } while (listResponse.isTruncated());
            }
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to cleanup S3 bucket folders: " + e.getMessage(), e);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null) {
            return System.currentTimeMillis() + "_file";
        }
        return System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    // Helper class for CloudFront URL signing
    private static class CloudFrontUrlSigner {
        private final String keyPairId;
        private final PrivateKey privateKey;

        public CloudFrontUrlSigner(String keyPairId, PrivateKey privateKey) {
            this.keyPairId = keyPairId;
            this.privateKey = privateKey;
        }

        public String generateSignedUrl(String url, Instant expiration) {
            try {
                String policy = createPolicy(url, expiration);
                // base64 encode the policy and replace special characters as per AWS specs
                String encodedPolicy = Base64.getEncoder().encodeToString(policy.getBytes())
                        .replace('+', '-')
                        .replace('=', '_')
                        .replace('/', '~');

                // Create signature
                java.security.Signature signer = java.security.Signature.getInstance("SHA1withRSA");
                signer.initSign(privateKey);
                signer.update(policy.getBytes());
                String signature = Base64.getEncoder().encodeToString(signer.sign())
                        .replace('+', '-')
                        .replace('=', '_')
                        .replace('/', '~');

                return url +
                        "?Policy=" + encodedPolicy +
                        "&Signature=" + signature +
                        "&Key-Pair-Id=" + keyPairId;
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate signed URL", e);
            }
        }

        private String createPolicy(String resourceUrl, Instant expiration) {
            return String.format("""
                {
                    "Statement": [{
                        "Resource": "%s",
                        "Condition": {
                            "DateLessThan": {
                                "AWS:EpochTime": %d
                            }
                        }
                    }]
                }
                """, resourceUrl, expiration.getEpochSecond());
        }
    }
}