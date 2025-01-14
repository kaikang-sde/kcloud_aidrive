package com.kang.kcloud_aidrive.controller.simpletest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * test Minio operations via S3Client
 * Not unit test
 */
@SpringBootTest
@Slf4j
public class AmazonS3ClientOperationsTest {

    @Autowired
    private S3Client s3Client;

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




}
