package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Kai Kang
 */
@Data
@Accessors(chain = true)
public class FileChunkInitTaskReq {
    private Long accountId;
    private String filename;
    private String identifier;
    private Long totalSize;
    private Long chunkSize;

}
