package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

/**
 * @author Kai Kang,
 * @since 2025-01-19
 */
@Data
public class FileUpdateReq {
    private Long accountId;
    private Long fileId;
    private String newFileName;
}
