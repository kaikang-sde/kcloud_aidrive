package com.kang.kcloud_aidrive.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Kai Kang
 */
@Getter
@Setter
@Builder
public class AccountLoginReq {
    @Schema(name = "password", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(name = "phone", example = "12345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
