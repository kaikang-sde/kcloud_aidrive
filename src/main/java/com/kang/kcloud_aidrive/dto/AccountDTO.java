package com.kang.kcloud_aidrive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Kai Kang,
 * @since 2025-01-19
 */
@Getter
@Setter
@Schema(name = "AccountDO", description = "User Account Table")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Avatar URL")
    private String avatarUrl;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "UserRole - COMMON, ADMIN")
    private String role;

    @Schema(description = "Logical Deletion - 1 deleted, 0 not deleted")
    private Boolean del = false;

    @Schema(description = "Created Time - EST")
    private Date estCreate;

    @Schema(description = "Modified Time - EST")
    private Date estModified;

    // root dir
    private Long rootFileId;
    private String rootFileName;
    private StorageDTO storageDTO;

}
