package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

/**
 * @author Kai Kang
 */
@Data
public class ShareCheckReq {
    private Long shareId;
    private String shareCode;
}
