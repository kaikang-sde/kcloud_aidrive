package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

import java.util.List;

/**
 * @author Kai Kang,
 * @since 2025-01-19
 */
@Data
public class FileBatchReq {
    private List<Long> fileIds;
    private Long targetParentId;
    private Long accountId;

}
