package com.kang.kcloud_aidrive.service.impl;

import com.google.common.base.Objects;
import com.kang.kcloud_aidrive.controller.req.FileUpdateReq;
import com.kang.kcloud_aidrive.controller.req.FolderCreateReq;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.enums.FolderFlagEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.repository.AccountFileRepository;
import com.kang.kcloud_aidrive.repository.FileRepository;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccountFileServiceImpl implements AccountFileService {

    private final AccountFileRepository accountFileRepository;

    public AccountFileServiceImpl(AccountFileRepository accountFileRepository, FileRepository fileRepository) {
        this.accountFileRepository = accountFileRepository;
    }

    @Override
    public List<AccountFileDTO> listFile(Long accountId, Long parentId) {
        List<AccountFileDAO> accountFileDAOList = accountFileRepository.findByAccountIdAndParentIdOrderByIsDirDescEstCreateDesc(accountId, parentId);
        return SpringBeanUtil.copyProperties(accountFileDAOList, AccountFileDTO.class);
    }

    @Override
    @Transactional
    public void renameFile(FileUpdateReq req) {
        AccountFileDAO accountFileDAO = accountFileRepository.findByIdAndAccountId(req.getFileId(), req.getAccountId());
        if (accountFileDAO == null) {
            log.error("File not exists, {}", req);
            throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
        } else {
            // new file name and current file name cannot be the same
            if (Objects.equal(accountFileDAO.getFileName(), req.getNewFileName())) {
                log.error("File name is already exist, {}", req);
                throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
            } else {
                // Same dir, cannot have the same file name
                if (accountFileRepository.countByAccountIdAndParentIdAndFileName(req.getAccountId(), accountFileDAO.getParentId(), req.getNewFileName()) > 0) {
                    log.error("File name is already exist, {}", req);
                    throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
                } else {
                    accountFileDAO.setFileName(req.getNewFileName());
                    accountFileRepository.updateFileNameByIdNative(accountFileDAO.getId(), accountFileDAO.getFileName());
                }
            }
        }
    }

    @Override
    public Long createFolder(FolderCreateReq req) {
        AccountFileDTO accountFileDTO = AccountFileDTO
                .builder()
                .accountId(req.getAccountId())
                .parentId(req.getParentId())
                .fileName(req.getFolderName())
                .isDir(FolderFlagEnum.YES.getCode())
                .build();

        return saveAccountFile(accountFileDTO);

    }

    /**
     * Handle the relationship between User and File, and save the file or folder
     *
     * @param accountFileDTO
     * @return
     */
    private Long saveAccountFile(AccountFileDTO accountFileDTO) {
        checkParentFileId(accountFileDTO);
        AccountFileDAO accountFileDAO = SpringBeanUtil.copyProperties(accountFileDTO, AccountFileDAO.class);

        processDuplicatedFileName(accountFileDAO);

        accountFileRepository.save(accountFileDAO);
        return accountFileDAO.getId();

    }

    private void processDuplicatedFileName(AccountFileDAO accountFileDAO) {
        int selectedAccount = accountFileRepository.countByAccountIdAndParentIdAndIsDirAndFileName(
                accountFileDAO.getAccountId(),
                accountFileDAO.getParentId(),
                accountFileDAO.getIsDir(),
                accountFileDAO.getFileName()
        );
        if (selectedAccount > 0) {
            // duplicated folder
            if (Objects.equal(accountFileDAO.getIsDir(), FolderFlagEnum.YES.getCode())) {
                accountFileDAO.setFileName(accountFileDAO.getFileName() + "_" + System.currentTimeMillis());
            } else { // duplicated filename,
                String[] split = accountFileDAO.getFileName().split("\\.");
                accountFileDAO.setFileName(split[0] + "_" + System.currentTimeMillis() + "." + split[1]);
            }
        }
    }

    private void checkParentFileId(AccountFileDTO accountFileDTO) {
        // parent_id = 0 default, root folder
        if (accountFileDTO.getParentId() != 0) {
            AccountFileDAO accountFileDAO = accountFileRepository.findByIdAndAccountId(accountFileDTO.getParentId(), accountFileDTO.getAccountId());
            if (accountFileDAO == null) {
                throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
            }
        }
    }

}
