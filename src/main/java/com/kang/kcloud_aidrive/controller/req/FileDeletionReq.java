package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

import java.util.List;

/**
 * @author Kai Kang
 */
@Data
public class FileDeletionReq {
    private Long accountId;
    private List<Long> fileIds;

}
