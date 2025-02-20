package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

/**
 * @author Kai Kang
 */
@Data
public class FileUpdateReq {
    private Long accountId;
    private Long fileId;
    private String newFileName;
}
