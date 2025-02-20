package com.kang.kcloud_aidrive.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author Kai Kang
 */
@Data
@Builder
public class AccountRegisterReq {
    @Schema(name = "username", example = "kai", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @Schema(name = "password", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    @Schema(name = "phone", example = "12345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
    @Schema(name = "avatarUrl", example = "https://example.com/avatar.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String avatarUrl;
}
