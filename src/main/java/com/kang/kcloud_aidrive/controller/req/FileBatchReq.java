package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

import java.util.List;

/**
 * @author Kai Kang
 */
@Data
public class FileBatchReq {
    private List<Long> fileIds;
    private Long targetParentId;
    private Long accountId;

}
