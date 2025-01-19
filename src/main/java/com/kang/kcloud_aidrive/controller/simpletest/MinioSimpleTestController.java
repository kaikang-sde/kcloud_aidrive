package com.kang.kcloud_aidrive.controller.simpletest;

import com.kang.kcloud_aidrive.config.MinioConfig;
import com.kang.kcloud_aidrive.util.CommonUtil;
import com.kang.kcloud_aidrive.util.JsonData;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Minio simple test controller - Minio简单测试控制器
 * Native MinIO Integration Upload Test
 *
 * @author Kai Kang
 */
@RestController
@RequestMapping("/api/test/minio")
@Tag(name = "MinIO Simple Test API", description = "MinIO simple Test API")
public class MinioSimpleTestController {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public MinioSimpleTestController(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    @PostMapping("/upload")
    @Operation(
            summary = "Upload a file",
            description = "Uploads a file",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File uploaded successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JsonData.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "code": 0,
                                                        "data": {},
                                                        "msg": "File uploaded successfully",
                                                        "success": true
                                                    }
                                                    """
                                    ))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "File uploaded failed",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JsonData.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "code": -1,
                                                        "data": {},
                                                        "msg": "File uploaded failed",
                                                        "success": false
                                                    }
                                                    """
                                    ))
                    )
            }
    )
    public JsonData testUpload(@RequestParam("file")
                               @Parameter(
                                       description = "File to be uploaded",
                                       required = true,
                                       content = @Content(mediaType = "multipart/form-data")
                               ) MultipartFile file) {

        String filename = CommonUtil.getFilePath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            // Storage services like MinIO or AWS S3 require file content to be uploaded as a stream.
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filename)
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
        } catch (Exception e) {
            return JsonData.buildError(e.getMessage());
        }
        String url = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + filename;
        return JsonData.buildSuccess(url);
    }
}
