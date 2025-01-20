package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.component.StorageEngine;
import com.kang.kcloud_aidrive.config.AccountConfig;
import com.kang.kcloud_aidrive.config.MinioConfig;
import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.entity.AccountDAO;
import com.kang.kcloud_aidrive.enums.AccountRoleEnum;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.repository.AccountRepository;
import com.kang.kcloud_aidrive.service.AccountService;
import com.kang.kcloud_aidrive.util.CommonUtil;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final StorageEngine fileStorageEngine;
    private final AccountRepository accountRepository;
    private final MinioConfig minioConfig;

    public AccountServiceImpl(StorageEngine fileStorageEngine, AccountRepository accountRepository, MinioConfig minioConfig) {
        this.fileStorageEngine = fileStorageEngine;
        this.accountRepository = accountRepository;
        this.minioConfig = minioConfig;
    }


    @Override
    public void register(AccountRegisterReq req) {
        // check if phone number exists - via JPA
        List<AccountDAO> accountDAOs = accountRepository.findByPhone(req.getPhone());
        if (!accountDAOs.isEmpty()) {
            throw new BizException(BizCodeEnum.ACCOUNT_REPEAT);
        }

        AccountDAO accountDAO = SpringBeanUtil.copyProperties(req, AccountDAO.class);

        // encrypt password
        String digestAsHex = DigestUtils.md5DigestAsHex((AccountConfig.ACCOUNT_SALT + req.getPassword()).getBytes());
        accountDAO.setPassword(digestAsHex);
        accountDAO.setRole(AccountRoleEnum.COMMON.name());

        // insert account to db table
        accountRepository.save(accountDAO);

        // TODO: operations


    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        String filename = CommonUtil.getFilePath(file.getOriginalFilename());
        fileStorageEngine.upload(minioConfig.getAvatarBucketName(), filename, file);
        return minioConfig.getEndpoint() + "/" + minioConfig.getAvatarBucketName() + "/" + filename;
    }
}
