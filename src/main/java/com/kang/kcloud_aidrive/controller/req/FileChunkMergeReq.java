package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Kai Kang
 */
@Data
@Accessors(chain = true)
public class FileChunkMergeReq {
    private String identifier;
    private Long parentId;
    private Long accountId;

}
