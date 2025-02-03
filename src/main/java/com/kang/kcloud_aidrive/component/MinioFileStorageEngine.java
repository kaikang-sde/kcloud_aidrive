package com.kang.kcloud_aidrive.component;


import com.kang.kcloud_aidrive.config.MinioConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.internal.http.HttpMethod;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
    private final S3Presigner s3Presigner;
    private final MinioConfig minioConfig;

    @Autowired
    public MinioFileStorageEngine(S3Client s3Client, S3Presigner s3Presigner, MinioConfig minioConfig) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
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

    @Override
    public ListPartsResponse listMultipart(String bucketName, String objectKey, String uploadId) {
        try {
            // Construct request to list parts
            ListPartsRequest request = ListPartsRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build();

            // Call AWS S3 to list uploaded parts
            return s3Client.listParts(request);
        } catch (Exception e) {
            log.error("Error listing multipart upload parts: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public CreateMultipartUploadResponse initMultipartUploadTask(String bucketName, String objectKey, Map<String, String> metadata) {
        try {
            // Build the CreateMultipartUploadRequest
            CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .metadata(metadata)  // Setting metadata
                    .acl(ObjectCannedACL.PUBLIC_READ) // Optional: Set object ACL
                    .build();

            // Execute the multipart upload initiation request
            return s3Client.createMultipartUpload(request);
        } catch (Exception e) {
            log.error("Error initiating multipart upload: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public URL genePreSignedUrl(String bucketName, String objectKey, HttpMethod httpMethod, Date expiration, Map<String, Object> params) {
        try {
            // Calculate duration from current time
            long expirationSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            // Create pre-signed request
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                    PutObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofSeconds(expirationSeconds))
                            .putObjectRequest(PutObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(objectKey)
                                    .build())
                            .build()
            );

            // Append query parameters manually
            URL signedUrl = presignedRequest.url();
            String finalUrl = appendQueryParameters(signedUrl, params);
            log.info("Generated Pre-Signed URL: {}", finalUrl);

            return URI.create(finalUrl).toURL();
        } catch (Exception e) {
            log.error("Error generating pre-signed URL", e);
            return null;
        }

    }

    @Override
    public CompleteMultipartUploadResponse mergeChunks(String bucketName, String objectKey, String uploadId, List<CompletedPart> partETags) {
        try {
            // Convert List<PartETag> to List<CompletedPart>
            List<CompletedPart> completedParts = partETags.stream()
                    .map(partETag -> CompletedPart.builder()
                            .partNumber(partETag.partNumber())
                            .eTag(partETag.eTag())
                            .build())
                    .collect(Collectors.toList());

            // Create CompletedMultipartUpload
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            // Create CompleteMultipartUploadRequest
            CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();

            // Complete the multipart upload
            CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(request);
            log.info("Multipart upload completed: {}", response.location());
            return response;
        } catch (S3Exception e) {
            log.error("Error completing multipart upload: {}", e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    /**
     * Manually appends query parameters to the pre-signed URL.
     *
     * @param url    The original URL
     * @param params Query parameters to append
     * @return Updated URL with parameters
     */
    private String appendQueryParameters(URL url, Map<String, Object> params) {
        String queryParams = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return url.toString() + (queryParams.isEmpty() ? "" : "?" + queryParams);
    }


}