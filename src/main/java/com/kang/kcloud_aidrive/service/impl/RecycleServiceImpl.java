package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.controller.req.RecycleDeleteReq;
import com.kang.kcloud_aidrive.controller.req.RecycleRestoreReq;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.enums.FolderFlagEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.repository.AccountFileRepository;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.service.RecycleService;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Kai Kang
 */
@Service
@Slf4j
public class RecycleServiceImpl implements RecycleService {
    private final AccountFileRepository accountFileRepository;
    private final AccountFileService accountFileService;

    public RecycleServiceImpl(AccountFileRepository accountFileRepository, AccountFileService accountFileService) {
        this.accountFileRepository = accountFileRepository;
        this.accountFileService = accountFileService;
    }

    @Override
    public List<AccountFileDTO> listRecycleFiles(Long accountId) {
        List<AccountFileDAO> recycleList = accountFileRepository.findRecycleFilesByAccountId(accountId, null);
        //如果是文件夹，就只显示文件夹，不显示文件夹里面的文件和文件夹
        List<Long> fileIds = recycleList.stream()
                .map(AccountFileDAO::getId) // 提取每个 AccountFileDO 对象的 id
                .toList();

        // 需要提取全部删除文件的ID，然后过滤下，如果某个文件的parentID在这个文件ID集合里面，则表示该文件为子文件， 过滤掉，只返回最外层文件
        recycleList = recycleList.stream()
                .filter(accountFileDO -> !fileIds.contains(accountFileDO.getParentId()))
                .collect(Collectors.toList());

        return SpringBeanUtil.copyProperties(recycleList, AccountFileDTO.class);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteRecycleFiles(RecycleDeleteReq req) {
        List<AccountFileDAO> records = accountFileRepository.findRecycleFilesByAccountId(req.getAccountId(), req.getFileIds());
        if (records.size() != req.getFileIds().size()) {
            throw new BizException(BizCodeEnum.FILE_DEL_BATCH_ILLEGAL);
        }

        List<AccountFileDAO> allRecords = new ArrayList<>();

        // 需要单独写查询文件夹和子文件的递归方法，因为@SQLRestriction("del = 0")是global setting，需要重写方法并使用 native query
        findAllAccountFileDAOWithRecursion(allRecords, records, false);

        List<Long> recycleFileIds = allRecords.stream().map(AccountFileDAO::getId).toList();

        // 批量删除回收站文件,不清空物理存储内容，方便做分析和后续有人重新上传
        accountFileRepository.deleteRecycleFiles(recycleFileIds);

        // 批量还原文件
        accountFileRepository.restoreFilesByIds(recycleFileIds);

    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void restoreRecycleFiles(RecycleRestoreReq req) {
        List<Long> fileIds = req.getFileIds();
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        String fileIdsString = fileIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        //判断文件ID和用户ID是否合法
        List<AccountFileDAO> accountFileDAOList = accountFileRepository.findRecycleFilesByAccountIdString(req.getAccountId(), fileIdsString);
        if (accountFileDAOList.size() != req.getFileIds().size()) {
            throw new BizException(BizCodeEnum.File_RECYCLE_ILLEGAL);
        }

        //还原前的父文件和当前文件夹是否有重复名称的文件和文件夹
        accountFileDAOList.forEach(accountFileDAO -> {
            Long selectedCount = accountFileService.processDuplicatedFileName(accountFileDAO, accountFileDAO.getParentId());
            if (selectedCount > 0) {
                accountFileRepository.updateRecycleFileNameByIdNative(accountFileDAO.getId(), accountFileDAO.getFileName());
            }
        });

        // 判断文件是否是文件夹，文件夹的话需要递归获取里面子文件ID，然后进行批量还原
        List<AccountFileDAO> allAccountFileDOList = new ArrayList<>();
        findAllAccountFileDAOWithRecursion(allAccountFileDOList, accountFileDAOList, false);
        List<Long> allFileIds = allAccountFileDOList.stream().map(AccountFileDAO::getId).toList();

        // 检查存储空间是否足够
        if (!accountFileService.checkAndUpdateStorageCapacity(req.getAccountId(), allAccountFileDOList.stream()
                .map(accountFileDAO -> accountFileDAO.getFileSize() == null ? 0 : accountFileDAO.getFileSize())
                .mapToLong(Long::valueOf)
                .sum())) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }
        // 批量还原文件
        accountFileRepository.restoreFilesByIds(allFileIds);


    }

    private void findAllAccountFileDAOWithRecursion(List<AccountFileDAO> allRecords, List<AccountFileDAO> records, boolean onlyFolder) {
        for (AccountFileDAO accountFileDAO : records) {
            if (Objects.equals(accountFileDAO.getIsDir(), FolderFlagEnum.YES.getCode())) {
                // 文件夹，递归获取子文件ID del = 1
                List<AccountFileDAO> childrenAccountFileDAOList = accountFileRepository.selectRecycleChildFiles(accountFileDAO.getId(), accountFileDAO.getAccountId());
                findAllAccountFileDAOWithRecursion(allRecords, childrenAccountFileDAOList, onlyFolder);
            }

            // not folder, store files
            // 如果通过onlyFolder是true 只存储文件夹到allAccountFileDOList，否则都存储到allAccountFileDOList
            if (!onlyFolder || Objects.equals(accountFileDAO.getIsDir(), FolderFlagEnum.YES.getCode())) {
                allRecords.add(accountFileDAO);
            }
        }

    }
}
