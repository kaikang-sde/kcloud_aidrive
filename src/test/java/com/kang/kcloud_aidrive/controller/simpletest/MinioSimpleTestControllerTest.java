package com.kang.kcloud_aidrive.controller.simpletest;

import com.kang.kcloud_aidrive.config.MinioConfig;
import com.kang.kcloud_aidrive.util.JsonData;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MinioSimpleTestControllerTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfig minioConfig;

    @InjectMocks
    private MinioSimpleTestController minioSimpleTestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void testUpload_Success() throws Exception {
        String fileName = "test.txt";
        String bucketName = "test-bucket";
        String endpoint = "http://localhost:9000";
        String accessKey = "minio";
        String accessSecret = "minio123";

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                "This is a test file.".getBytes()
        );

        when(minioConfig.getBucketName()).thenReturn(bucketName);
        when(minioConfig.getAccessKey()).thenReturn(accessKey);
        when(minioConfig.getAccessSecret()).thenReturn(accessSecret);
        when(minioConfig.getEndpoint()).thenReturn(endpoint);

        JsonData response = minioSimpleTestController.testUpload(mockMultipartFile);
        System.out.println(response);

        verify(minioClient).putObject(any(PutObjectArgs.class));

    }

    @Test
    void testUpload_Failure() throws Exception {
        String fileName = "test.txt";
        String bucketName = "test-bucket";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                "This is a test file.".getBytes()
        );

        when(minioConfig.getBucketName()).thenReturn(bucketName);
        doThrow(new RuntimeException("Upload failed")).when(minioClient).putObject(any(PutObjectArgs.class));

        JsonData response = minioSimpleTestController.testUpload(mockMultipartFile);
        System.out.println(response);
        verify(minioClient).putObject(any(PutObjectArgs.class));
        assertEquals("Upload failed", response.getMsg());
    }
}
