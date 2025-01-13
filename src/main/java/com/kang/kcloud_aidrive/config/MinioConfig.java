package com.kang.kcloud_aidrive.config;

import io.minio.MinioClient;
import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    @Value("endpoint")
    private String endpoint;

    @Value("access-key")
    private String accessKey;

    @Value("access-secret")
    private String accessSecret;

    @Value("bucket-name")
    private String bucketName;

    // expiration time for a pre-signed URL (used to generate temporary access URLs for MinIO - 10 minutes).
    private Long PRE_SIGN_URL_EXPIRE = 60 * 10 * 1000L;

    // Create a minioClient with the MinIO server playground
    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, accessSecret).build();
    }
}
