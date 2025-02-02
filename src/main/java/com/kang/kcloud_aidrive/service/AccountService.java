package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.AccountLoginReq;
import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.dto.AccountDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Author: Kai Kang
 */
public interface AccountService {
    void register(AccountRegisterReq req);
    String uploadAvatar(MultipartFile file);
    AccountDTO login(AccountLoginReq req);

    AccountDTO queryDetail(Long id);
}
