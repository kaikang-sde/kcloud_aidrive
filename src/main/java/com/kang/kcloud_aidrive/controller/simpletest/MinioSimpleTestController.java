package com.kang.kcloud_aidrive.controller.simpletest;

import com.kang.kcloud_aidrive.config.MinioConfig;
import com.kang.kcloud_aidrive.util.CommonUtil;
import com.kang.kcloud_aidrive.util.JsonData;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Minio simple test controller - Minio简单测试控制器
 * Native MinIO Integration Upload Test
 * @author Kai Kang
 */
@RestController
@RequestMapping("/api/test/minio")
public class MinioSimpleTestController {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @PostMapping("/upload")
    public JsonData testUpload(@RequestParam("file") MultipartFile file) {
        // 获取上传文件名
        String filename = CommonUtil.getFilePath(file.getOriginalFilename());
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filename)
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + filename;
        return JsonData.buildSuccess(url);
    }

}
