package com.kang.kcloud_aidrive.component;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * strategy pattern
 * buckets operations
 * files operations
 * Author: Kai Kang
 */
public interface StorageEngine {

    boolean bucketExists(String bucketName);

    boolean removeBucket(String bucketName);

    void createBucket(String bucketName);

    List<Bucket> getAllBuckets();

    List<S3Object> listObjects(String bucketName);

    boolean doesObjectExist(String bucketName, String objectKey);

    boolean upload(String bucketName, String objectKey, String localFileName);

    boolean upload(String bucketName, String objectKey, MultipartFile file);

    boolean delete(String bucketName, String objectKey);

    /**
     * Get the download URL for a specified object - 获取指定对象的下载URL
     *
     * @param bucketName 桶名称
     * @param remoteFileName 对象的名称
     * @param timeout URL的有效时长
     * @param unit URL有效时长的时间单位
     * @return 对象的下载URL
     */
    String getDownloadUrl(String bucketName, String remoteFileName, long timeout, TimeUnit unit);

    /**
     * Download the specified object to the HTTP response - 将指定对象下载到HTTP响应中
     *
     * @param bucketName 桶名称
     * @param objectKey 对象的名称
     * @param response HTTP响应对象，用于输出下载的对象
     */
    void download2Response(String bucketName, String objectKey, HttpServletResponse response);

}
