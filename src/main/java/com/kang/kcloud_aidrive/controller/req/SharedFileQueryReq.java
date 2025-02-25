package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

/**
 * @author Kai Kang
 */
@Data
public class SharedFileQueryReq {
    private Long shareId;
    private Long parentId;

}
