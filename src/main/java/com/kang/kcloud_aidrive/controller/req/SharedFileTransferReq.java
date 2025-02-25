package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

import java.util.List;

/**
 * @author Kai Kang
 */
@Data
public class SharedFileTransferReq {
    private Long shareId;
    private Long accountId;
    private Long parentId;
    private List<Long> fileIds;
}
