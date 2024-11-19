package com.projects.cafe_winchester_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client,
                     S3Presigner s3Presigner,
                     @Value("${aws.s3.bucket}") String bucketName,
                     @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = region;
        this.s3Presigner = s3Presigner;
    }


    // For organizing objects in folders in S3
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
                            file.getSize())); // AWS SDK can pre-allocate resources

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    public String generatePreSignedUrl(String objectKey) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toExternalForm();

    }

    public void deleteFile(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("File key cannot be null or empty");
        }

        try {

            // Check if file exists before deleting, as this returns object metadata without downloading the object
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

            // Check for any failed deletions
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
            // List all image type folders we want to clean
            List<String> foldersToClean = new ArrayList<>();
            for (ImageType imageType : ImageType.values()) {
                foldersToClean.add(imageType.getPath());
            }

            for (String folder : foldersToClean) {
                // List objects in the folder
                ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folder)
                        .build();

                ListObjectsV2Response listResponse;
                do {
                    listResponse = s3Client.listObjectsV2(listRequest);

                    if (!listResponse.contents().isEmpty()) {
                        // Prepare batch delete request
                        List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                                .map(s3Object -> ObjectIdentifier.builder()
                                        .key(s3Object.key())
                                        .build())
                                .toList();

                        // Delete the batch of objects
                        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                                .bucket(bucketName)
                                .delete(Delete.builder()
                                        .objects(objectsToDelete)
                                        .quiet(false) // Set to true if deletion details are not needed
                                        .build())
                                .build();

                        DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);

                        // Check for errors
                        if (!deleteResponse.errors().isEmpty()) {
                            List<String> errorMessages = deleteResponse.errors().stream()
                                    .map(error -> error.key() + ": " + error.message())
                                    .toList();
                            System.err.println("Failed to delete some objects in folder " + folder + ": "
                                    + String.join(", ", errorMessages));
                        } else {
                            System.out.println("Successfully deleted " + objectsToDelete.size()
                                    + " objects from folder: " + folder);
                        }
                    } else {
                        System.out.println("No objects found in folder: " + folder);
                    }

                    // Update the list request with the continuation token to get the next batch
                    listRequest = ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .prefix(folder)
                            .continuationToken(listResponse.nextContinuationToken())    // We do this because the objects are returned paginated, hence we check if there are more objects using the token AWS sends in the response if there are more objects, or else the token is null
                            .build();

                } while (listResponse.isTruncated()); // Continue if there are more objects.
            }

            System.out.println("Bucket cleanup completed successfully");

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to cleanup S3 bucket folders: " + e.getMessage(), e);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null) {
            return System.currentTimeMillis() + "_file";
        }
        return System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        /*
        Replaces any character that is NOT (that's what ^ means):

        a-z: lowercase letters
        A-Z: uppercase letters
        0-9: numbers
        .: period
        -: hyphen

        Replaces these special characters with underscore _
         */
    }
}
