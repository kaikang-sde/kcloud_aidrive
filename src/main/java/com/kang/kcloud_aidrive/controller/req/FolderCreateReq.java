package com.kang.kcloud_aidrive.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderCreateReq {
    @Schema(name = "folder name", example = "XX folder", requiredMode = Schema.RequiredMode.REQUIRED)
    private String folderName;
    @Schema(name = "parent id", example = "321", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentId;
    @Schema(name = "account id", example = "123")
    private Long accountId;

}
