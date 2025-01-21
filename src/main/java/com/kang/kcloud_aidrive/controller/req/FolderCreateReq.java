package com.kang.kcloud_aidrive.controller.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderCreateReq {
    private String folderName;
    private Long parentId;
    private Long accountId;

}
