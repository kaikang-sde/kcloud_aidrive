package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.controller.req.FolderCreateReq;
import com.kang.kcloud_aidrive.repository.AccountFileRepository;
import com.kang.kcloud_aidrive.repository.AccountRepository;
import com.kang.kcloud_aidrive.repository.FileRepository;
import com.kang.kcloud_aidrive.service.AccountFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountFileServiceImpl implements AccountFileService {
    private final AccountFileRepository accountFileRepository;
    private final FileRepository fileRepository;

    public AccountFileServiceImpl(AccountFileRepository accountFileRepository, FileRepository fileRepository) {
        this.accountFileRepository = accountFileRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    public void createFolder(FolderCreateReq folderCreateReq) {

    }
}
