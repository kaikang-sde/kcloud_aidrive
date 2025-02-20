package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Kai Kang
 */
@Data
@Accessors(chain = true)
public class FileUploadReq {
    private String fileName;
    // 文件唯一标识 md5
    private String identifier;
    private Long accountId;
    private Long parentId;
    private Long fileSize;
    private MultipartFile file;
}
