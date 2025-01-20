package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {
    void register(AccountRegisterReq req);
    String uploadAvatar(MultipartFile file);
}
