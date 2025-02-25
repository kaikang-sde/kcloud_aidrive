package com.kang.kcloud_aidrive.component;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * strategy pattern
 * buckets operations
 * files operations
 * @author Kai Kang
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
     * @param bucketName     桶名称
     * @param remoteFileName 对象的名称
     * @param timeout        URL的有效时长
     * @param unit           URL有效时长的时间单位
     * @return 对象的下载URL
     */
    String getDownloadUrl(String bucketName, String remoteFileName, long timeout, TimeUnit unit);

    /**
     * Download the specified object to the HTTP response - 将指定对象下载到HTTP响应中
     *
     * @param bucketName 桶名称
     * @param objectKey  对象的名称
     * @param response   HTTP响应对象，用于输出下载的对象
     */
    void download2Response(String bucketName, String objectKey, HttpServletResponse response);


    /**
     * Query multipart upload parts
     *
     * @param bucketName Storage bucket name
     * @param objectKey  Object name
     * @param uploadId   Multipart upload ID
     * @return List of uploaded parts
     */
    ListPartsResponse listMultipart(String bucketName, String objectKey, String uploadId);


    /**
     * Initializes a multipart upload task to get an upload ID.
     * If an upload ID already exists, it means resuming an upload, so a new ID should not be created.
     * https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateMultipartUpload.html
     * https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/s3/S3Client.html#createMultipartUpload(software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest)
     *
     * @param bucketName  The name of the S3 bucket.
     * @param objectKey   The key of the object to upload.
     * @param contentType Metadata for the object (e.g., Content-Type).
     * @return Upload ID for multipart upload.
     */
    CreateMultipartUploadResponse initMultipartUploadTask(String bucketName, String objectKey, String contentType);

    URL genePreSignedUrl(String bucketName, String objectKey, Date expiration, int partNumber, String uploadId);

    /**
     * Completes a multipart upload by merging uploaded chunks.
     *
     * @param bucketName The name of the S3 bucket.
     * @param objectKey  The key of the object being uploaded.
     * @param uploadId   The upload ID identifying the multipart upload.
     * @param partETags  List of completed parts with ETags.
     * @return CompleteMultipartUploadResponse containing upload details.
     */
    CompleteMultipartUploadResponse mergeChunks(String bucketName, String objectKey, String uploadId, List<CompletedPart> partETags);


}
