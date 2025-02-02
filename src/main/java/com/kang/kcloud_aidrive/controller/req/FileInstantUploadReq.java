package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Kai Kang
 */
@Data
@Accessors(chain = true)
public class FileInstantUploadReq {
    private String fileName;
    // 文件唯一标识 md5
    private String identifier;
    private Long accountId;
    private Long parentId;

    /**
     * Beside Identifier
     * More mete data can decrease hashing conflicts
     *     private Long fileSize;
     *     private MultipartFile file;
     */

}
