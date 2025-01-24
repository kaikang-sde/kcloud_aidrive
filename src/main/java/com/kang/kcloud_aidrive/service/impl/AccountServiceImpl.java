package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.component.StorageEngine;
import com.kang.kcloud_aidrive.config.AccountConfig;
import com.kang.kcloud_aidrive.config.MinioConfig;
import com.kang.kcloud_aidrive.controller.req.AccountLoginReq;
import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.controller.req.FolderCreateReq;
import com.kang.kcloud_aidrive.dto.AccountDTO;
import com.kang.kcloud_aidrive.dto.StorageDTO;
import com.kang.kcloud_aidrive.entity.AccountDAO;
import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import com.kang.kcloud_aidrive.entity.StorageDAO;
import com.kang.kcloud_aidrive.enums.AccountRoleEnum;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.repository.AccountFileRepository;
import com.kang.kcloud_aidrive.repository.AccountRepository;
import com.kang.kcloud_aidrive.repository.StorageRepository;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.service.AccountService;
import com.kang.kcloud_aidrive.util.CommonUtil;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final StorageEngine fileStorageEngine;
    private final AccountRepository accountRepository;
    private final MinioConfig minioConfig;
    private final StorageRepository storageRepository;
    private final AccountFileRepository accountFileRepository;
    private final AccountFileService accountFileService;

    public AccountServiceImpl(StorageEngine fileStorageEngine, AccountRepository accountRepository, MinioConfig minioConfig, StorageRepository storageRepository, AccountFileRepository accountFileRepository, AccountFileService accountFileService) {
        this.fileStorageEngine = fileStorageEngine;
        this.accountRepository = accountRepository;
        this.minioConfig = minioConfig;
        this.storageRepository = storageRepository;
        this.accountFileRepository = accountFileRepository;
        this.accountFileService = accountFileService;
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
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

        // create default storage capacity
        StorageDAO storageDAO = new StorageDAO();
        storageDAO.setAccountId(accountDAO.getId());
        storageDAO.setUsedSize(0L);
        storageDAO.setTotalSize(AccountConfig.DEFAULT_STORAGE_SIZE);
        storageRepository.save(storageDAO);

        // Initialize root folder
        FolderCreateReq rootFolderReq = FolderCreateReq.builder()
                .accountId(accountDAO.getId())
                .parentId(AccountConfig.ROOT_PARENT_ID)
                .folderName(AccountConfig.ROOT_FOLDER_NAME)
                .build();

        accountFileService.createFolder(rootFolderReq);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        String filename = CommonUtil.getFilePath(file.getOriginalFilename());
        fileStorageEngine.upload(minioConfig.getAvatarBucketName(), filename, file);
        return minioConfig.getEndpoint() + "/" + minioConfig.getAvatarBucketName() + "/" + filename;
    }

    @Override
    public AccountDTO login(AccountLoginReq req) {
        // salt password
        String digestAsHex = DigestUtils.md5DigestAsHex((AccountConfig.ACCOUNT_SALT + req.getPassword()).getBytes());
        AccountDAO accountDAO = accountRepository.findByPhoneAndPassword(req.getPhone(), digestAsHex);
        if (accountDAO == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_PWD_ERROR);
        }
        return SpringBeanUtil.copyProperties(accountDAO, AccountDTO.class);
    }

    @Override
    public AccountDTO queryDetail(Long id) {
        // account detail
        Optional<AccountDAO> accountDAO = accountRepository.findById(id);
        AccountDTO accountDTO = SpringBeanUtil.copyProperties(accountDAO, AccountDTO.class);

        // storage detail
        StorageDAO storageDAO = storageRepository.findByAccountId(id);
        accountDTO.setStorageDTO(SpringBeanUtil.copyProperties(storageDAO, StorageDTO.class));

        // file detail
        AccountFileDAO accountFileDAO = accountFileRepository.findByAccountIdAndParentId(id, AccountConfig.ROOT_PARENT_ID);
        accountDTO.setRootFileId(accountFileDAO.getId());
        accountDTO.setRootFileName(accountFileDAO.getFileName());

        return accountDTO;

    }
}
