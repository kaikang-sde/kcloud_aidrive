package com.kang.kcloud_aidrive.component;


import com.kang.kcloud_aidrive.config.MinioConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * MinioFileStorageEngine implements the StorageEngine interface to provide
 * S3-compatible object storage functionality using MinIO.
 *
 * @author Kai Kang
 */

@Component
@Slf4j
public class MinioFileStorageEngine implements StorageEngine {

    private final S3Client s3Client;
    private final MinioConfig minioConfig;

    @Autowired
    public MinioFileStorageEngine(S3Client s3Client, MinioConfig minioConfig) {
        this.s3Client = s3Client;
        this.minioConfig = minioConfig;
    }

    /**
     * Validates if a bucket name is not null or empty
     */
    private void validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
    }

    /**
     * Whether the specified bucket exists.
     *
     * @param bucketName The name of the bucket
     * @return Whether the bucket exists
     */

    @Override
    public boolean bucketExists(String bucketName) {
        validateBucketName(bucketName);
        try {
            return s3Client.listBuckets().buckets().stream().anyMatch(bucket -> bucket.name().equals(bucketName));
        } catch (Exception e) {
            log.error("Failed to list buckets", e);
            return false;
        }
    }

    /**
     * Delete the specified bucket.
     *
     * @param bucketName The name of the bucket
     * @return Whether the bucket was successfully deleted
     */
    @Override
    public boolean removeBucket(String bucketName) {
        validateBucketName(bucketName);
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build();
        try {
            s3Client.deleteBucket(deleteBucketRequest);
            log.info("Bucket {} deleted", bucketName);
        } catch (Exception e) {
            log.error("Failed to delete {} bucket - {}", bucketName, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Create a new bucket with the specified name.
     *
     * @param bucketName The name of the bucket
     */
    @Override
    public void createBucket(String bucketName) {
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName).build();
        try {
            s3Client.createBucket(createBucketRequest);
            log.info("Bucket {} created", bucketName);
        } catch (Exception e) {
            log.error("Failed to create bucket {}", bucketName, e);
        }
    }

    /**
     * Get a list of all buckets.
     *
     * @return A list of all buckets
     */
    @Override
    public List<Bucket> getAllBuckets() {
        try {
            return s3Client.listBuckets().buckets();
        } catch (Exception e) {
            log.error("Failed to list buckets", e);
            return List.of();
        }
    }

    /**
     * Get a list of all objects in the specified bucket.
     *
     * @param bucketName The name of the bucket
     * @return A list of all objects in the specified bucket
     */
    @Override
    public List<S3Object> listObjects(String bucketName) {
        if (bucketExists(bucketName)) {
            try {
                return s3Client.listObjectsV2(r -> r.bucket(bucketName)).contents();
            } catch (Exception e) {
                log.error("Failed to list objects in bucket {}", bucketName, e);
                return List.of();
            }
        }
        return List.of();
    }

    /**
     * Check if the specified object exists in the specified bucket.
     *
     * @param bucketName The name of the bucket
     * @param objectKey  The key of the object
     * @return Whether the object exists
     */
    @Override
    public boolean doesObjectExist(String bucketName, String objectKey) {
        if (bucketExists(bucketName)) {
            try {
                return s3Client.getObject(r -> r.bucket(bucketName).key(objectKey)) != null;
            } catch (Exception e) {
                log.error("Failed to check object existence", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Upload the specified local file to the specified bucket with the specified object key.
     *
     * @param bucketName    The name of the bucket
     * @param objectKey     The key of the object
     * @param localFileName The name of the local file
     * @return Whether the upload was successful
     */
    @Override
    public boolean upload(String bucketName, String objectKey, String localFileName) {
        if (bucketExists(bucketName)) {
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();
                s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(localFileName)));
                log.info("File {} uploaded to bucket {}", localFileName, bucketName);
                return true;
            } catch (Exception e) {
                log.error("Failed to upload file {} to bucket {}", localFileName, bucketName, e);
                return false;
            }
        }
        return false;
    }

    /**
     * Upload the specified MultipartFile to the specified bucket with the specified object key.
     *
     * @param bucketName The name of the bucket
     * @param objectKey  The key of the object
     * @param file       The MultipartFile to upload
     * @return Whether the upload was successful
     */
    @Override
    public boolean upload(String bucketName, String objectKey, MultipartFile file) {
        if (bucketExists(bucketName)) {
            try {
                Path tempFile = Files.createTempFile("upload-", Objects.requireNonNull(file.getOriginalFilename()));
                file.transferTo(tempFile.toFile());

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build();
                PutObjectResponse response = s3Client.putObject(putObjectRequest, tempFile);
                Files.delete(tempFile);
                log.info("File {} uploaded to bucket {}", file.getOriginalFilename(), bucketName);
                return response.sdkHttpResponse().isSuccessful();
            } catch (Exception e) {
                log.error("Failed to upload file {} to bucket {}", file.getOriginalFilename(), bucketName, e);
                return false;
            }
        }
        return false;
    }

    /**
     * Delete the specified object from the specified bucket.
     *
     * @param bucketName The name of the bucket
     * @param objectKey  The key of the object
     * @return Whether the deletion was successful
     */
    @Override
    public boolean delete(String bucketName, String objectKey) {
        if (bucketExists(bucketName)) {
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
                log.info("File {} deleted from bucket {}", objectKey, bucketName);
                return true;
            } catch (Exception e) {
                log.error("Failed to delete file {} from bucket {}", objectKey, bucketName, e);
                return false;
            }
        }
        return false;
    }

    /**
     * Generate a presigned URL for downloading the specified object from the specified bucket.
     *
     * @param bucketName The name of the bucket
     * @param objectKey  The key of the object
     * @param timeout    The expiration time of the presigned URL
     * @param unit       The time unit of the expiration time
     * @return The presigned URL
     */
    @Override
    public String getDownloadUrl(String bucketName, String objectKey, long timeout, TimeUnit unit) {
        Duration duration = Duration.ofMillis(unit.toMillis(timeout));
        AwsBasicCredentials minioCredentials = AwsBasicCredentials.create(minioConfig.getAccessKey(), minioConfig.getAccessSecret());

        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.US_EAST_1) // Set your region
                .credentialsProvider(StaticCredentialsProvider.create(minioCredentials)) // Update with your credential provider
                .build()) {

            // Create the GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Create the GetObjectPresignRequest
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(duration) // Set expiration duration
                    .build();

            // Generate the presigned URL
            PresignedGetObjectRequest presignedGetObjectRequest =
                    presigner.presignGetObject(presignRequest);

            return presignedGetObjectRequest.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL", e);
            return null;
        }
    }


    /**
     * Download the specified object from the specified bucket and write it to the HTTP response output stream.
     *
     * @param bucketName The name of the bucket
     * @param objectKey  The key of the object
     * @param response   The HTTP response
     */
    @Override
    public void download2Response(String bucketName, String objectKey, HttpServletResponse response) {
        try (ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build()
        )) {
            // Set response headers
            response.setHeader("Content-Disposition", "attachment;filename=" + objectKey.substring(objectKey.lastIndexOf("/") + 1));
            response.setContentType("application/force-download");
            response.setCharacterEncoding("UTF-8");

            // Copy S3 object content to HTTP response output stream
            IOUtils.copy(s3ObjectInputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file: " + e.getMessage(), e);
        }
    }
}