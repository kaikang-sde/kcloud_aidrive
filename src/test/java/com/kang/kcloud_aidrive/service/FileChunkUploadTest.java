package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.FileChunkInitTaskReq;
import com.kang.kcloud_aidrive.controller.req.FileChunkMergeReq;
import com.kang.kcloud_aidrive.dto.FileChunkDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mocking big files uploading (chunk file, pre signed url, merge chunks, get chunks upload progress)
 * @author Kai Kang
 */
@SpringBootTest
@Slf4j
public class FileChunkUploadTest {
    @Autowired
    public FileChunkService fileChunkService;

    private Long accountId = 486486988967514112L;

    private String identifier = "kllklkml";

    /**
     * 存储分片后端的文件路径和名称
     */
    private final List<String> chunkFilePaths = new ArrayList<>();

    /**
     * 存储分片上传地址/预签名地址
     */
    private final List<String> chunkUploadUrls = new ArrayList<>();

    /**
     * 上传ID
     */
    private String uploadId;

    /**
     * 分片大小，5MB
     */
    private final long chunkSize = 5 * 1024 * 1024;

    /**
     * Splits a large file into multiple smaller files. Mocking chunk initialization
     * Uses FileInputStream to read the original file in chunks.
     * Uses FileOutputStream to write each chunk to a separate file.
     */
    @Test
    public void testCreateChunkFiles() {

        // 将文件分片存储 File Initialization
        String filePath = "/Users/kai/Desktop/testing/es_note.pdf";
        File file = new File(filePath);
        long fileSize = file.length();

        // Calculate Number of Chunks based on chunk size - 5MB
        int chunkCount = (int) Math.ceil(fileSize * 1.0 / chunkSize);
        log.info("the number of chunks: {}", chunkCount);


        // Open the File for Reading - try block automatically closes the stream after reading.
        try (FileInputStream fis = new FileInputStream(file)) {
            // Read and Write Chunks
            // - A byte array (buffer) is created to store each chunk’s data.
            // - A loop runs chunkCount times, processing each chunk.
            byte[] buffer = new byte[(int) chunkSize];
            for (int i = 0; i < chunkCount; i++) {
                // Process Each Chunk
                // - Creates a chunk file name like: es_note.pdf.part1
                // - Opens a FileOutputStream (fos) to write chunk data.
                String chunkFileName = filePath + ".part" + (i + 1);
                try (FileOutputStream fos = new FileOutputStream(chunkFileName)) {
                    // Reads chunkSize bytes from the original file into buffer. Returns actual number of bytes read (may be less for the last chunk).
                    int bytesRead = fis.read(buffer);
                    // Writes bytesRead bytes from buffer to the chunk file.
                    fos.write(buffer, 0, bytesRead);
                    log.info("Chunked file name: {} ({} bytes)", chunkFileName, bytesRead);
                    chunkFilePaths.add(chunkFileName);
                    log.info("chunkFilePaths:{}", chunkFilePaths);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 1. 创建分片上传任务
        testInitFileChunkTask();
    }

    private void testInitFileChunkTask() {
        FileChunkInitTaskReq req = new FileChunkInitTaskReq();
        req.setAccountId(accountId)
                .setFilename("es_note.pdf")
                .setIdentifier(identifier)
                .setTotalSize((long) 11452689)
                .setChunkSize(chunkSize);
        FileChunkDTO fileChunkDTO = fileChunkService.initiateChunkUpload(req);
        log.info("file chunk upload task:{}", fileChunkDTO);

        uploadId = fileChunkDTO.getUploadId();

        // 2. 获取分片临时上传地址
        testGetFileChunkUploadUrl();

    }

    private void testGetFileChunkUploadUrl() {
        for (int i = 1; i <= chunkFilePaths.size(); i++) {
            String chunkUploadUrl = fileChunkService.getPresignedUploadUrl(accountId, identifier, i);
            log.info("chunkUploadUrl:{}", chunkUploadUrl);
            chunkUploadUrls.add(chunkUploadUrl);
        }

        // 3. 上传分片文件到MinIO， 模拟前端上传
        testUploadChunk();
    }

    @SneakyThrows
    private void testUploadChunk() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            for (int i = 0; i < chunkUploadUrls.size(); i++) {
                String uploadUrl = chunkUploadUrls.get(i);
                HttpPut httpPut = new HttpPut(uploadUrl);
                httpPut.setHeader("Content-Type", "application/octet-stream");
                httpPut.setHeader("Connection", "keep-alive");

                File chunkFile = new File(chunkFilePaths.get(i));
                FileEntity chunkFileEntity = new FileEntity(chunkFile);
                httpPut.setEntity(chunkFileEntity);

                // Use try-with-resources to ensure response is closed properly
                try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                    log.info("Chunk upload status: {}", response.getStatusLine());
                }
            }
        } catch (IOException e) {
            log.error("Error uploading chunks", e);
        }
    }


    // 大文件合并
    @Test
    public void testMergeChunk() {
        FileChunkMergeReq req = new FileChunkMergeReq();
        req.setAccountId(accountId)
                .setIdentifier(identifier)
                .setParentId(486486989051400192L);
        fileChunkService.mergeChunks(req);
    }

    // 查询文件上传进度
    @Test
    public void testChunkUploadProgress() {
        FileChunkDTO fileChunkDTO = fileChunkService.listFileChunk(accountId, identifier);
        log.info("file chunk upload progress:{}", fileChunkDTO);
    }

}
