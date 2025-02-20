package com.kang.kcloud_aidrive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: Kai Kang
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AccountFileDAO", description = "user file table")
public class AccountFileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "account ID")
    private Long accountId;

    @Schema(description = "status - 0: not dir，1 - dir")
    private Integer isDir;

    @Schema(description = "parent ID, root dir is 0")
    private Long parentId;

    @Schema(description = "file ID，stored file")
    private Long fileId;

    @Schema(description = "file name")
    private String fileName;

    @Schema(description = "file Type：common 、compress 、  excel  、 word  、 pdf  、 txt  、 img  、audio  、video 、ppt 、code  、 csv")
    private String fileType;

    @Schema(description = "file suffix")
    private String fileSuffix;

    @Schema(description = "file size, byte")
    private Long fileSize;

    @Schema(description = "Logical Deletion - 1 deleted, 0 not deleted")
    private Boolean del;

    @Schema(description = "delete time")
    private Date delTime;

    @Schema(description = "Modified Time - EST")
    private Date estModified;

    @Schema(description = "Created Time - EST")
    private Date estCreate;
}