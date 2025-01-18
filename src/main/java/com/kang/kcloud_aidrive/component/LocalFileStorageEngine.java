package com.kang.kcloud_aidrive.component;

import software.amazon.awssdk.services.s3.model.Bucket;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Object;


import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Not implemented - No local file needed
 */
public class LocalFileStorageEngine implements StorageEngine {
    @Override
    public boolean bucketExists(String bucketName) {
        return false;
    }

    @Override
    public boolean removeBucket(String bucketName) {
        return false;
    }

    @Override
    public void createBucket(String bucketName) {

    }

    @Override
    public List<Bucket> getAllBuckets() {
        return List.of();
    }

    @Override
    public List<S3Object> listObjects(String bucketName) {
        return List.of();
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectKey) {
        return false;
    }

    @Override
    public boolean upload(String bucketName, String objectKey, String localFileName) {
        return false;
    }

    @Override
    public boolean upload(String bucketName, String objectKey, MultipartFile file) {
        return false;
    }

    @Override
    public boolean delete(String bucketName, String objectKey) {
        return false;
    }

    @Override
    public String getDownloadUrl(String bucketName, String remoteFileName, long timeout, TimeUnit unit) {
        return "";
    }

    @Override
    public void download2Response(String bucketName, String objectKey, HttpServletResponse response) {

    }
}
