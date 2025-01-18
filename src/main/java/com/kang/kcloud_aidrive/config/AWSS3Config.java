package com.kang.kcloud_aidrive.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * creates and configures an S3Client bean for interacting with a MinIO object storage using the AWS SDK for Java (2.x).
 * Author: Kai Kang
 */
@Configuration
@Slf4j
public class AWSS3Config {

    @Autowired
    private MinioConfig minioConfig;

    private S3Client s3Client;

    @Bean(name = "S3Client")
    public S3Client getS3Client() {
        // Creates AWS credentials (access key and secret key) for authenticating requests to MinIO.
        AwsBasicCredentials minioCredentials = AwsBasicCredentials.create(minioConfig.getAccessKey(), minioConfig.getAccessSecret());

        s3Client = S3Client.builder()
                .endpointOverride(URI.create(minioConfig.getEndpoint())) // Set MinIO endpoint
                .credentialsProvider(StaticCredentialsProvider.create(minioCredentials)) // Set MinIO credentials
                .region(Region.US_EAST_1) // Region is required but irrelevant for MinIO
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // Enable path-style access for MinIO, required for MinIO
                        .build())
                .build();
        return s3Client;
    }

    // Ensure the S3Client is properly closed when the application shuts down
    @PreDestroy
    public void closeS3Client() {
        if (s3Client != null) {
            s3Client.close();
            log.info("MinIO S3 Client closed successfully.");
        }
    }

}
