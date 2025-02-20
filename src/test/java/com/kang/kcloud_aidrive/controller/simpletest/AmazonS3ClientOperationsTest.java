package com.kang.kcloud_aidrive.controller.simpletest;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


/**
 * test Minio operations via S3Client (AWS SDK for Java 2.X)
 * Author: Kai Kang
 */
@SpringBootTest
@Slf4j
public class AmazonS3ClientOperationsTest {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Test
    public void testBucketExistsViaS3() {
        boolean bucketExist = s3Client.listBuckets().buckets().stream().anyMatch(bucket -> bucket.name().equals("kcloud-aidrive"));
        assertTrue(bucketExist); // Assert that the bucket exists(bucketExist
    }

    @Test
    public void testBucketNotExistsViaS3() {
        boolean bucketExist = s3Client.listBuckets().buckets().stream().anyMatch(bucket -> bucket.name().equals("test"));
        assertFalse(bucketExist);
    }

//    @Test
//    public void testBucketCreation() {
//        String bucketName = "test-bucket";
//        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
//                .bucket(bucketName)
//                .build();
//        CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest);
//        boolean bucketExist = s3Client.listBuckets().buckets().stream().anyMatch(bucket -> bucket.name().equals(bucketName));
//        assertTrue(bucketExist);
//    }

    @Test
    public void testListBuckets() {
        List<String> buckets = s3Client.listBuckets().buckets().stream().map(Bucket::name).toList();
        System.out.println(buckets);
        assertEquals(buckets.size(), 2);

    }

    /**
     * Big Files - Chunked Upload Process
     * 1. Initialize the chunked upload task and obtain uploadId
     * If an uploadId already exists during initialization, it indicates resumable upload, and a new uploadId should not be generated.
     */
    @Test
    public void testInitiateMultiPartUpload() {
        String bucketName = "kcloud-aidrive";
        String objectKey = "/aa/bb/cc/666.txt";

        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("text/plain")
                .build();

        CreateMultipartUploadResponse uploadResponse = s3Client.createMultipartUpload(createRequest);
        String uploadId = uploadResponse.uploadId();  // Store this for later use
        log.info("Upload ID: {}", uploadId);

    }

    /**
     * Big Files - Chunked Upload Process
     * 2. generating pre-signed URLs for multipart upload, return to frontend
     */
    @Test
    public void testGeneratePreSignedUrl() {
        String bucketName = "kcloud-aidrive";
        String objectKey = "aa/bb/cc/666.txt";
        int chunkCount = 4;  // Number of chucked parts
        String uploadId = "MjljYmU4NzMtNmQ1Ni00OGFkLTkzMjctMzY4OWYxMTA0YjAyLmNlMGI1Yzk2LWUwMTYtNDQ1My1hMzczLWMxYzQ2M2FiMjMxYg";

        List<String> preSignedUrls = new ArrayList<>();

        for (int i = 1; i <= chunkCount; i++) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1)) // URL expires in 1 hour
                    .putObjectRequest(putObjectRequest)
                    .build();

            URL presignedUrl = s3Presigner.presignPutObject(presignRequest).url();

            // Append uploadId and partNumber manually
            String finalUrl = appendQueryParameters(presignedUrl, uploadId, i);
            // temp pre-signed URL, return to frontend
            preSignedUrls.add(finalUrl);
            log.info("Pre-signed URL for part {}: {}", i, finalUrl);
        }
    }

    private String appendQueryParameters(URL url, String uploadId, int partNumber) {
        return url.toString() + "&uploadId=" + uploadId + "&partNumber=" + partNumber;
    }


    /**
     * Big Files - Chunked Upload Process
     * 3. Merge chunks
     * Use uploadId to identify the specific chunk upload session.
     * Check whether the number of uploaded chunks matches the expected count.
     * The server merges all chunks to reconstruct the complete file.
     * <p>
     * Note: cannot test because of no chunks are uploaded.
     */
    @Test
    public void testMergeChunk() {
        String bucketName = "kcloud-aidrive";
        String objectKey = "aa/bb/cc/666.txt";
        int chunkCount = 4;  // Number of chucked parts
        String uploadId = "MjljYmU4NzMtNmQ1Ni00OGFkLTkzMjctMzY4OWYxMTA0YjAyLmNlMGI1Yzk2LWUwMTYtNDQ1My1hMzczLWMxYzQ2M2FiMjMxYg";

        // List uploaded parts
        ListPartsRequest listPartsRequest = ListPartsRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .uploadId(uploadId)
                .build();

        ListPartsResponse listPartsResponse = s3Client.listParts(listPartsRequest);
        List<CompletedPart> completedParts = listPartsResponse.parts().stream()
                .map(part -> CompletedPart.builder()
                        .partNumber(part.partNumber())
                        .eTag(part.eTag())
                        .build())
                .collect(Collectors.toList());

        // Ensure all parts are uploaded
        if (completedParts.size() != chunkCount) {
            throw new RuntimeException("Multipart upload failed: uploaded part count does not match expected chunk count.");
        }

        // Complete multipart upload
        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                .build();

        CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(completeRequest);
        log.info("Multipart Upload Completed: {}", response.location());
    }

    /**
     * Large File Upload API: Testing Upload Progress in Practice
     */
    @Test
    public void testUploadProgress() {
        String bucketName = "kcloud-aidrive";
        String objectKey = "aa/bb/cc/666.txt";
        int chunkCount = 4;  // Number of chucked parts
        String uploadId = "MjljYmU4NzMtNmQ1Ni00OGFkLTkzMjctMzY4OWYxMTA0YjAyLmNlMGI1Yzk2LWUwMTYtNDQ1My1hMzczLWMxYzQ2M2FiMjMxYg";

        try {
            // Check if the object exists in the specified bucket
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            log.info("Object {} exists in bucket {}", objectKey, bucketName);
        } catch (S3Exception e) {
            // If the object does not exist, list the uploaded parts
            log.info("Object does not exist. Fetching uploaded parts for incomplete upload.");

            ListPartsRequest listPartsRequest = ListPartsRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build();

            ListPartsResponse listPartsResponse = s3Client.listParts(listPartsRequest);
            List<Part> partList = listPartsResponse.parts();

            // Store upload status and part list in a result map
            Map<String, Object> result = new HashMap<>();
            result.put("finished", false);
            result.put("exitPartList", partList);

            // frontend will know if it needs to invoke merge interface
            log.info("Upload status: {}", result);

            // Iterate and log each part's information
            for (Part part : partList) {
                log.info("PartNumber: {}, ETag: {}, Size: {}, LastModified: {}",
                        part.partNumber(), part.eTag(), part.size(), part.lastModified());
            }
        }
    }
}
