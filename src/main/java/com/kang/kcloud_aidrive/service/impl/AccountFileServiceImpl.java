package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.component.StorageEngine;
import com.kang.kcloud_aidrive.config.MinioConfig;
import com.kang.kcloud_aidrive.config.SnowflakeConfig;
import com.kang.kcloud_aidrive.controller.req.*;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.dto.FileDownloadUrlDTO;
import com.kang.kcloud_aidrive.dto.FolderTreeNodeDTO;
import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import com.kang.kcloud_aidrive.entity.AccountFileDAOWithoutAutoGenId;
import com.kang.kcloud_aidrive.entity.FileDAO;
import com.kang.kcloud_aidrive.entity.StorageDAO;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.enums.FileTypeEnum;
import com.kang.kcloud_aidrive.enums.FolderFlagEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.repository.AccountFileDAOWithoutAutoGenIdRepository;
import com.kang.kcloud_aidrive.repository.AccountFileRepository;
import com.kang.kcloud_aidrive.repository.FileRepository;
import com.kang.kcloud_aidrive.repository.StorageRepository;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.util.CommonUtil;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Kai Kang
 */
@Service
@Slf4j
public class AccountFileServiceImpl implements AccountFileService {

    private final AccountFileRepository accountFileRepository;
    private final StorageEngine fileStorageEngine;
    private final MinioConfig minioConfig;
    private final FileRepository fileRepository;
    private final StorageRepository storageRepository;
    private final SnowflakeConfig snowflakeConfig;
    private final AccountFileDAOWithoutAutoGenIdRepository accountFileDAOWithoutAutoGenIdRepository;

    public AccountFileServiceImpl(AccountFileRepository accountFileRepository, StorageEngine fileStorageEngine, MinioConfig minioConfig, FileRepository fileRepository, StorageRepository storageRepository, SnowflakeConfig snowflakeConfig, AccountFileDAOWithoutAutoGenIdRepository accountFileDAOWithoutAutoGenIdRepository) {
        this.accountFileRepository = accountFileRepository;
        this.fileStorageEngine = fileStorageEngine;
        this.minioConfig = minioConfig;
        this.fileRepository = fileRepository;
        this.storageRepository = storageRepository;
        this.snowflakeConfig = snowflakeConfig;
        this.accountFileDAOWithoutAutoGenIdRepository = accountFileDAOWithoutAutoGenIdRepository;
    }

    @Override
    public List<AccountFileDTO> listFile(Long accountId, Long parentId) {
        List<AccountFileDAO> accountFileDAOList = accountFileRepository.findByAccountIdAndParentIdOrderByIsDirDescEstCreateDesc(accountId, parentId);
        return SpringBeanUtil.copyProperties(accountFileDAOList, AccountFileDTO.class);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void renameFile(FileUpdateReq req) {
        AccountFileDAO accountFileDAO = accountFileRepository.findByIdAndAccountId(req.getFileId(), req.getAccountId());

        if (accountFileDAO == null) {
            log.error("File not exists, {}", req);
            throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
        } else {
            // new file name and current file name cannot be the same
            if (Objects.equals(accountFileDAO.getFileName(), req.getNewFileName())) {
                log.error("File name is already exist, {}", req);
                throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
            } else {
                // Same dir, cannot have the same file name
                Long selectedCount = accountFileRepository.countByAccountIdAndParentIdAndFileName(req.getAccountId(), accountFileDAO.getParentId(), req.getNewFileName());
                if (selectedCount > 0) {
                    log.error("File name is already exist, {}", req);
                    throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
                } else {
                    accountFileDAO.setFileName(req.getNewFileName());
                    accountFileRepository.updateFileNameByIdNative(accountFileDAO.getId(), accountFileDAO.getFileName());
                }
            }
        }
    }

    // 非递归的方式
    @Override
    public List<FolderTreeNodeDTO> folderTreeV1(Long accountId) {
        // 查询当前用户的所有文件夹
        List<AccountFileDAO> folderList = accountFileRepository.findByAccountIdAndIsDir(accountId, FolderFlagEnum.YES.getCode());

        // 拼装文件夹树列表
        if (CollectionUtils.isEmpty(folderList)) {
            return List.of();
        }

        // 构建一个Map/数据源， 避免数据库查找，key为文件夹ID，value为FolderTreeNodeDTO对象
        Map<Long, FolderTreeNodeDTO> folderMap = folderList.stream()
                .collect(Collectors.toMap(
                        AccountFileDAO::getId,
                        AccountFileDAO -> FolderTreeNodeDTO.builder()
                                .id(AccountFileDAO.getId())
                                .parentId(AccountFileDAO.getParentId())
                                .label(AccountFileDAO.getFileName())
                                .children(new ArrayList<>())
                                .build()));


        // 构建文件夹树，遍历文件夹映射，为每个文件夹找到其子文件夹
        for (FolderTreeNodeDTO node : folderMap.values()) {
            // 获取当前文件夹的父ID
            Long parentId = node.getParentId();
            // 如果父ID不为空且父ID在文件夹映射中存在，则将当前文件夹添加到其父文件夹的子文件夹列表中
            if (parentId != null && folderMap.containsKey(parentId)) {
                // 获取父文件夹
                FolderTreeNodeDTO parentNode = folderMap.get(parentId);
                // 获取父文件夹的子文件夹列表
                List<FolderTreeNodeDTO> children = parentNode.getChildren();
                // 将当前文件夹添加到子文件夹列表中
                children.add(node);
            }
        }

        // 返回根节点（parentId为0的节点）过滤出根文件夹即可,里面包括多个
        List<FolderTreeNodeDTO> rootFolderList = folderMap.values().stream()
                .filter(node -> Objects.equals(node.getParentId(), 0L))
                .collect(Collectors.toList());
        return rootFolderList;

    }

    // 分组的方式 - 也是非递归的
    @Override
    public List<FolderTreeNodeDTO> folderTreeV2(Long accountId) {
        // 查询当前用户的所有文件夹
        List<AccountFileDAO> folderList = accountFileRepository.findByAccountIdAndIsDir(accountId, FolderFlagEnum.YES.getCode());

        // 拼装文件夹树列表
        if (CollectionUtils.isEmpty(folderList)) {
            return List.of();
        }

        // 构建一个Map/数据源， 避免数据库查找，key为parentId，value为当前文件夹下的所有子文件夹
        List<FolderTreeNodeDTO> folderTreeNodeDTOList = folderList.stream().map(file -> {
            return FolderTreeNodeDTO.builder()
                    .id(file.getId())
                    .parentId(file.getParentId())
                    .label(file.getFileName())
                    .children(new ArrayList<>())
                    .build();
        }).toList();

        // 根据父文件夹进行分组 key是当前文件夹ID，value是当前文件夹下的所有子文件夹
        Map<Long, List<FolderTreeNodeDTO>> folderMap = folderTreeNodeDTOList.stream()
                .collect(Collectors.groupingBy(FolderTreeNodeDTO::getParentId));

        for (FolderTreeNodeDTO node : folderTreeNodeDTOList) {
            List<FolderTreeNodeDTO> children = folderMap.get(node.getId());
            //判断列表是否为空, 不为空，挂载
            if (!CollectionUtils.isEmpty(children)) {
                node.getChildren().addAll(children);
            }
        }

        // 返回根节点（parentId为0的节点）过滤出根文件夹即可,里面包括多个
        return folderTreeNodeDTOList.stream().filter(node -> Objects.equals(node.getParentId(), 0L)).collect(Collectors.toList());

    }

    /**
     * small, normal file upload
     *
     * @param req
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void uploadFile(FileUploadReq req) {
        long fileSize = req.getFile().getSize(); // Get file size in bytes
        // 1. check storage capacity
        boolean isEnough = checkAndUpdateStorageCapacity(req.getAccountId(), fileSize);
        if (!isEnough) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }
        // 2. upload to MinIO via S3Client
        String storeFileObjectKey = storeFile(req);

        // 3. save file relationship + save relationship between user and file
        saveFileAndAccountFile(req, storeFileObjectKey);
    }

    @Override
    public boolean checkAndUpdateStorageCapacity(Long accountId, Long fileSize) {
        StorageDAO storageDAO = storageRepository.findByAccountId(accountId);

        Long newSize = storageDAO.getUsedSize() + fileSize;
        if (newSize > storageDAO.getTotalSize()) {
            return false;
        }

        storageRepository.updateUsedSizeByAccountId(accountId, fileSize);
        return true;
    }

    /**
     * 1. 检查被转移的文件ID是否合法
     * 2. 检查目标文件夹ID是否合法,需要包括子文件夹
     * 3. 批量转移文件到目标文件夹 (处理重复文件名)
     * 4. 更新文件或文件夹的parentId为目标文件夹ID
     *
     * @param req
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchMove(FileBatchReq req) {
        // Check whether the file IDs to be transferred are valid.
        List<AccountFileDAO> toBeTransferredAccountFileDAOList = validateFileId(req.getFileIds(), req.getAccountId());

        // Validate the target folder ID, ensuring it includes subfolders.
        validateTargetParentId(req);

        // Batch move files to the target folder (handle duplicate file names).
//        toBeTransferredAccountFileDAOList.forEach(file -> processDuplicatedFileName(file, req.getTargetParentId()));
//
//        // Update the parentId of files or folders to the target folder ID.
//        int updatedRows = accountFileRepository.updateParentIdForFileIds(req.getFileIds(), req.getTargetParentId());
//
//        if (updatedRows != req.getFileIds().size()) {
//            throw new BizException(BizCodeEnum.FILE_BATCH_UPDATE_ERROR);
//        }

        // improvement: if duplicate, DB return > 0, then update
        toBeTransferredAccountFileDAOList.forEach(accountFileDAO -> {
            Long selectedCount = processDuplicatedFileName(accountFileDAO, accountFileDAO.getParentId());
            if (selectedCount > 0) {
                accountFileRepository.updateParentIdForFileIds(req.getFileIds(), req.getTargetParentId());
            }
        });
    }

    /**
     * 步骤一：检查是否满足：1、文件ID数量是否合法，2、文件是否属于当前用户
     * 步骤二：判断文件是否是文件夹，文件夹的话需要递归获取里面子文件ID，然后进行批量删除
     * 步骤三：需要更新账号存储空间使用情况
     * 步骤四：批量删除账号映射文件，考虑回收站如何设计
     *
     * @param req
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchDeleteFiles(FileDeletionReq req) {
        List<AccountFileDAO> toBeDeletedAccountFileDAOList = validateFileId(req.getFileIds(), req.getAccountId());

        List<AccountFileDAO> storedAccountFileDAOList = new ArrayList<>();
        // false - files and folders
        findAllAccountFileDAOByRecursion(storedAccountFileDAOList, toBeDeletedAccountFileDAOList, false);

        List<Long> allFileOrFolderIdList = storedAccountFileDAOList.stream().map(AccountFileDAO::getId).collect(Collectors.toList());

        // update storage usage
        // TODO: improvement, distributed lock - redission(key: accountId)
        Long usedStorageSize = storedAccountFileDAOList.stream()
                .filter(file -> file.getIsDir().equals(FolderFlagEnum.NO.getCode()))
                .mapToLong(AccountFileDAO::getFileSize).sum();

        storageRepository.updateUsedSizeByAccountId(req.getAccountId(), -usedStorageSize);

        accountFileRepository.softDeleteAllByIdInBatch(allFileOrFolderIdList);

    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchCopyFiles(FileBatchReq req) {
        // check if the file ids are valid
        List<AccountFileDAO> toBeCopiedAccountFileDAOList = validateFileId(req.getFileIds(), req.getAccountId());

        // check if the target parent id is valid
        validateTargetParentId(req);

        // copy files, recursion, generate new file ids
        List<AccountFileDAOWithoutAutoGenId> copiedAccountFileDAOList = findBatchCopyFilesRecursion(toBeCopiedAccountFileDAOList, req.getTargetParentId());

        // calculate storage capacity
        Long totalFileSize = copiedAccountFileDAOList.stream()
                .filter(file -> file.getIsDir().equals(FolderFlagEnum.NO.getCode()))
                .mapToLong(AccountFileDAOWithoutAutoGenId::getFileSize).sum();

        if (!checkAndUpdateStorageCapacity(req.getAccountId(), totalFileSize)) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }

        accountFileDAOWithoutAutoGenIdRepository.saveAll(copiedAccountFileDAOList);
    }

    /**
     * Rapid upload
     * 1. File existing check
     * 2. Storage capacity check
     * 3. build the relationship between the file and account file
     *
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean instantUpload(FileInstantUploadReq req) {
        FileDAO fileDAO = fileRepository.findByIdentifier(req.getIdentifier());
        if (fileDAO != null && checkAndUpdateStorageCapacity(req.getAccountId(), fileDAO.getFileSize())) {
            AccountFileDTO accountFileDTO = new AccountFileDTO();
            accountFileDTO.setAccountId(req.getAccountId());
            accountFileDTO.setFileId(fileDAO.getId());
            accountFileDTO.setParentId(req.getParentId());
            accountFileDTO.setFileName(req.getFileName());
            accountFileDTO.setFileSize(fileDAO.getFileSize());
            accountFileDTO.setDel(false);
            accountFileDTO.setIsDir(FolderFlagEnum.NO.getCode());

            saveAccountFile(accountFileDTO);
            return true;
        }
        return false;
    }

    @Override
    public List<AccountFileDAOWithoutAutoGenId> findBatchCopyFilesRecursion(List<AccountFileDAO> toBeCopiedAccountFileDAOList, Long targetParentId) {
        List<AccountFileDAOWithoutAutoGenId> copiedAccountFileDAOList = new ArrayList<>();

        // check if the file or folder, from the first level
        toBeCopiedAccountFileDAOList.forEach(accountFileDAO -> doCopyChildRecord(copiedAccountFileDAOList, accountFileDAO, targetParentId));
        return copiedAccountFileDAOList;
    }

    private void doCopyChildRecord(List<AccountFileDAOWithoutAutoGenId> copiedAccountFileDAOList, AccountFileDAO accountFileDAO, Long targetParentId) {
        //Hibernate expects the ID to never change once set, create a new accountFileDAO
        AccountFileDAOWithoutAutoGenId newAccountFileDAO = deepCopyAccountFileDAO(accountFileDAO, targetParentId);

        processDuplicatedFileName(newAccountFileDAO, targetParentId);

        copiedAccountFileDAOList.add(newAccountFileDAO);

        // if still a folder, recursion to get next level file / folder based original file Id
        if (accountFileDAO.getIsDir().equals(FolderFlagEnum.YES.getCode())) {
            List<AccountFileDAO> childAccountFileDAOList = findChildAccountFile(accountFileDAO.getAccountId(), accountFileDAO.getId());
            if (CollectionUtils.isEmpty(childAccountFileDAOList)) {
                return;
            }
            childAccountFileDAOList
                    .forEach(childAccountFileDAO -> doCopyChildRecord(copiedAccountFileDAOList, childAccountFileDAO, newAccountFileDAO.getId()));
        }
    }

    private AccountFileDAOWithoutAutoGenId deepCopyAccountFileDAO(AccountFileDAO accountFileDAO, Long targetParentId) {
        AccountFileDAOWithoutAutoGenId newAccountFileDAO = new AccountFileDAOWithoutAutoGenId();
        newAccountFileDAO.setId(snowflakeConfig.generateId());
        newAccountFileDAO.setAccountId(accountFileDAO.getAccountId());
        newAccountFileDAO.setIsDir(accountFileDAO.getIsDir());
        newAccountFileDAO.setParentId(targetParentId);
        newAccountFileDAO.setFileId(accountFileDAO.getFileId());
        newAccountFileDAO.setFileName(accountFileDAO.getFileName());
        newAccountFileDAO.setFileType(accountFileDAO.getFileType());
        newAccountFileDAO.setFileSuffix(accountFileDAO.getFileSuffix());
        newAccountFileDAO.setFileSize(accountFileDAO.getFileSize());
        return newAccountFileDAO;
    }

    /**
     * find child account file
     *
     * @param accountId
     * @param parentId
     * @return
     */
    private List<AccountFileDAO> findChildAccountFile(Long accountId, Long parentId) {
        return accountFileRepository.findAllByAccountIdAndParentId(accountId, parentId);
    }


    private void validateTargetParentId(FileBatchReq req) {
        // target fileId cannot be a file, must be folder
        AccountFileDAO targetAccountFileDAO = accountFileRepository.
                findByIdAndIsDirAndAccountId(
                        req.getTargetParentId(),
                        FolderFlagEnum.YES.getCode(),
                        req.getAccountId());

        if (targetAccountFileDAO == null) {
            log.error("The target file id is not a folder, cannot move, targetParentId = {}", req.getTargetParentId());
            throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
        }

        List<AccountFileDAO> preparedAccountFileDAOList = accountFileRepository.findByIdInAndAccountId(req.getFileIds(), req.getAccountId());

        List<AccountFileDAO> allAccountFileDAOList = new ArrayList<>();
        findAllAccountFileDAOByRecursion(allAccountFileDAOList, preparedAccountFileDAOList, false);
        if (allAccountFileDAOList.stream().anyMatch(accountFileDAO -> Objects.equals(accountFileDAO.getId(), req.getTargetParentId()))) {
            log.error("The target file id is not a folder, cannot move, targetParentId = {}", req.getTargetParentId());
        }
    }

    @Override
    public void findAllAccountFileDAOByRecursion(List<AccountFileDAO> allAccountFileDAOList, List<AccountFileDAO> preparedAccountFileDAOList, boolean onlyFolder) {
        for (AccountFileDAO accountFileDAO : preparedAccountFileDAOList) {
            if (Objects.equals(accountFileDAO.getIsDir(), FolderFlagEnum.YES.getCode())) {
                // 文件夹，递归获取子文件ID
                List<AccountFileDAO> childrenAccountFileDAOList = accountFileRepository.findByParentId(accountFileDAO.getId());
                findAllAccountFileDAOByRecursion(allAccountFileDAOList, childrenAccountFileDAOList, onlyFolder);
            }

            // not folder, store files
            // 如果通过onlyFolder是true 只存储文件夹到allAccountFileDOList，否则都存储到allAccountFileDOList
            if (!onlyFolder || Objects.equals(accountFileDAO.getIsDir(), FolderFlagEnum.YES.getCode())) {
                allAccountFileDAOList.add(accountFileDAO);
            }
        }
    }

    @Override
    public List<AccountFileDAO> validateFileId(List<Long> fileIds, Long accountId) {
        List<AccountFileDAO> accountFileDAOList = accountFileRepository.findByIdInAndAccountId(fileIds, accountId);
        if (accountFileDAOList.size() != fileIds.size()) {
            log.error("The number of files to be moved does not match the number of valid File id - fildIds: {}, accountId: {}", fileIds, accountId);
            throw new BizException(BizCodeEnum.FILE_BATCH_UPDATE_ERROR);
        }
        // TODO: improvement - set to remove duplicate files

        return accountFileDAOList;
    }

    // save the relationship between file and account file to DB
    @Override
    public void saveFileAndAccountFile(FileUploadReq req, String storeFileObjectKey) {
        FileDAO fileDAO = saveFile(req, storeFileObjectKey);

        AccountFileDTO accountFileDTO = AccountFileDTO.builder()
                .accountId(req.getAccountId())
                .parentId(req.getParentId())
                .fileId(fileDAO.getId())
                .fileName(fileDAO.getFileName())
                .isDir(FolderFlagEnum.NO.getCode())
                .fileSuffix(fileDAO.getFileSuffix())
                .fileSize(req.getFileSize())
                .fileType(FileTypeEnum.fromExtension(fileDAO.getFileSuffix()).name())
                .build();
        saveAccountFile(accountFileDTO);
    }

    private FileDAO saveFile(FileUploadReq req, String storeFileObjectKey) {
        FileDAO fileDAO = new FileDAO();
        fileDAO.setAccountId(req.getAccountId());
        fileDAO.setFileName(req.getFileName());
        fileDAO.setFileSize(req.getFile() != null ? req.getFile().getSize() : req.getFileSize());
        fileDAO.setFileSuffix(CommonUtil.getFileSuffix(req.getFileName()));
        fileDAO.setIdentifier(req.getIdentifier());
        fileDAO.setObjectKey(storeFileObjectKey);
        fileRepository.save(fileDAO);
        return fileDAO;

    }

    private String storeFile(FileUploadReq req) {
        String fileName = CommonUtil.getFilePath(req.getFileName());
        fileStorageEngine.upload(minioConfig.getBucketName(), fileName, req.getFile());
        return fileName;
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

        processDuplicatedFileName(accountFileDAO, null);

        accountFileRepository.save(accountFileDAO);
        return accountFileDAO.getId();
    }

    @Override
    public Long processDuplicatedFileName(AccountFileDAO accountFileDAO, Long parentId) {
        if (parentId == null) {
            parentId = accountFileDAO.getParentId();
        }
        Long selectCount = accountFileRepository.countByAccountIdAndParentIdAndIsDirAndFileName(
                accountFileDAO.getAccountId(),
                parentId,
                accountFileDAO.getIsDir(),
                accountFileDAO.getFileName()
        );

        if (selectCount > 0) {
            // duplicated folder
            if (Objects.equals(accountFileDAO.getIsDir(), FolderFlagEnum.YES.getCode())) {
                accountFileDAO.setFileName(accountFileDAO.getFileName() + "_" + System.currentTimeMillis());
            } else { // duplicated filename,
                String[] split = accountFileDAO.getFileName().split("\\.");
                accountFileDAO.setFileName(split[0] + "_" + System.currentTimeMillis() + "." + split[1]);
            }
        }
        return selectCount;
    }

    @Override
    public List<AccountFileDTO> search(Long accountId, String searchKey) {
        List<AccountFileDAO> accountFileDAOList = accountFileRepository.findFilesByAccountIdAndFileNameOrderByIsDirAndEstCreateNative(accountId, searchKey);
        return SpringBeanUtil.copyProperties(accountFileDAOList, AccountFileDTO.class);
    }

    @Override
    public List<FileDownloadUrlDTO> getDownloadUrls(FileDownloadUrlReq req) {
        List<AccountFileDAO> accountFileDAOList = accountFileRepository.findByAccountIdAndIsDirAndIdIn(req.getAccountId(), FolderFlagEnum.NO.getCode(), req.getFileIds());

        List<FileDownloadUrlDTO> fileDownloadUrlDTOList = new ArrayList<>();
        for (AccountFileDAO accountFileDAO : accountFileDAOList) {
            String objectKey = accountFileRepository.findObjectKeyById(accountFileDAO.getFileId());
            String downloadUrl = fileStorageEngine.getDownloadUrl(
                    minioConfig.getBucketName(),
                    objectKey,
                    minioConfig.getPreSignURLExpire(),
                    TimeUnit.MILLISECONDS
            );
            FileDownloadUrlDTO fileDownloadUrlDTO = new FileDownloadUrlDTO(accountFileDAO.getFileName(), downloadUrl);
            fileDownloadUrlDTOList.add(fileDownloadUrlDTO);
        }

        return fileDownloadUrlDTOList;
    }

    private void processDuplicatedFileName(AccountFileDAOWithoutAutoGenId accountFileDAO, Long parentId) {
        if (parentId == null) {
            parentId = accountFileDAO.getParentId();
        }
        Long selectCount = accountFileRepository.countByAccountIdAndParentIdAndIsDirAndFileName(
                accountFileDAO.getAccountId(),
                parentId,
                accountFileDAO.getIsDir(),
                accountFileDAO.getFileName()
        );

        if (selectCount > 0) {
            // duplicated folder
            if (Objects.equals(accountFileDAO.getIsDir(), FolderFlagEnum.YES.getCode())) {
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
                throw new BizException(BizCodeEnum.PARENT_DIR_NOT_EXISTS);
            }
        }
    }

}
