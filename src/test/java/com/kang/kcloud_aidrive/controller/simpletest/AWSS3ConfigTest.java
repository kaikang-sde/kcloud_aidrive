package com.kang.kcloud_aidrive.controller.simpletest;

import com.kang.kcloud_aidrive.config.AWSS3Config;
import com.kang.kcloud_aidrive.config.MinioConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AWSS3ConfigTest {

    @Mock
    private MinioConfig minioConfig;

    @InjectMocks
    private AWSS3Config awsS3Config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetS3Client_Success() {
        when(minioConfig.getAccessKey()).thenReturn("testAccessKey");
        when(minioConfig.getAccessSecret()).thenReturn("testSecretKey");
        when(minioConfig.getEndpoint()).thenReturn("http://localhost:9000");

        S3Client s3Client = awsS3Config.getS3Client();
        System.out.println(s3Client);

        assertNotNull(s3Client, "S3Client should not be null");
        assertEquals(URI.create("http://localhost:9000"), s3Client.serviceClientConfiguration().endpointOverride().get());

        verify(minioConfig, times(1)).getAccessKey();
        verify(minioConfig, times(1)).getAccessSecret();
        verify(minioConfig, times(1)).getEndpoint();
    }

    @Test
    void testGetS3Client_Failure() {
        when(minioConfig.getAccessKey()).thenReturn(null);
        when(minioConfig.getAccessSecret()).thenReturn(null);
        when(minioConfig.getEndpoint()).thenReturn(null);

        assertThrows(Exception.class, () -> awsS3Config.getS3Client());
    }
}
