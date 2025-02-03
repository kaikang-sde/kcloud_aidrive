package com.kang.kcloud_aidrive.component;

import okhttp3.internal.http.HttpMethod;
import software.amazon.awssdk.services.s3.model.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;


import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Override
    public ListPartsResponse listMultipart(String bucketName, String objectKey, String uploadId) {
        return null;
    }

    @Override
    public CreateMultipartUploadResponse initMultipartUploadTask(String bucketName, String objectKey, Map<String, String> metadata) {
        return null;
    }

    @Override
    public URL genePreSignedUrl(String bucketName, String objectKey, HttpMethod httpMethod, Date expiration, Map<String, Object> params) {
        return null;
    }

    @Override
    public CompleteMultipartUploadResponse mergeChunks(String bucketName, String objectKey, String uploadId, List<CompletedPart> partETags) {
        return null;
    }
}
