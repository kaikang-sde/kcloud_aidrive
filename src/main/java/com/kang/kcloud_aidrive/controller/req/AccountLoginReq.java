package com.kang.kcloud_aidrive.controller.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountLoginReq {
    private String password;
    private String phone;
}
