package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

import java.util.List;

/**
 * Batch/ single cancel
 * @author Kai Kang
 */
@Data
public class ShareCancelReq {
    private List<Long> shareIds;
    private Long accountId;

}
