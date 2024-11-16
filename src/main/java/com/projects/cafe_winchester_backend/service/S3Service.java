package com.projects.cafe_winchester_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3Service(S3Client s3Client,
                     @Value("${aws.s3.bucket}") String bucketName,
                     @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = region;
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

            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName,
                    region,
                    fileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("File URL cannot be null or empty");
        }

        try {
            String key = extractKeyFromUrl(fileUrl);

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

    public void deleteMultipleFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            throw new IllegalArgumentException("File URLs list cannot be null or empty");
        }

        List<ObjectIdentifier> keys = new ArrayList<>();
        List<String> failedUrls = new ArrayList<>();

        for (String url : fileUrls) {
            try {
                String key = extractKeyFromUrl(url);
                keys.add(ObjectIdentifier.builder().key(key).build());
            } catch (Exception e) {
                failedUrls.add(url);
            }
        }

        if (!failedUrls.isEmpty()) {
            throw new IllegalArgumentException("Invalid URLs: " + String.join(", ", failedUrls));
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

    private String extractKeyFromUrl(String fileUrl) {
        // URL format: https://bucket-name.s3.region.amazonaws.com/folder/filename
        try {
            // Remove the base URL part to get the key
            String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
            if (!fileUrl.startsWith(baseUrl)) {
                throw new IllegalArgumentException("Invalid S3 URL format");
            }
            return fileUrl.substring(baseUrl.length());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract key from URL: " + e.getMessage());
        }
    }
}
