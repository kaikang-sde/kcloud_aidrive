package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.FileChunkInitTaskReq;
import com.kang.kcloud_aidrive.controller.req.FileChunkMergeReq;
import com.kang.kcloud_aidrive.dto.FileChunkDTO;

/**
 * @author Kai Kang
 */
public interface FileChunkService {
    FileChunkDTO initiateChunkUpload(FileChunkInitTaskReq req);

    String getPresignedUploadUrl(Long accountId, String identifier, int partNumber);

    void mergeChunks(FileChunkMergeReq req);


    FileChunkDTO listFileChunk(Long accountId, String identifier);
}
