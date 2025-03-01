package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.*;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.dto.FileDownloadUrlDTO;
import com.kang.kcloud_aidrive.dto.FolderTreeNodeDTO;
import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import com.kang.kcloud_aidrive.entity.AccountFileDAOWithoutAutoGenId;

import java.util.List;

/**
 * @author Kai Kang
 */
public interface AccountFileService {
    // create folder
    Long createFolder(FolderCreateReq folderCreateReq);

    List<AccountFileDTO> listFile(Long accountId, Long parentId);

    void renameFile(FileUpdateReq req);

    List<FolderTreeNodeDTO> folderTreeV1(Long accountId);

    List<FolderTreeNodeDTO> folderTreeV2(Long accountId);

    void uploadFile(FileUploadReq req);

    void batchMove(FileBatchReq req);

    void batchDeleteFiles(FileDeletionReq req);

    void batchCopyFiles(FileBatchReq req);

    boolean instantUpload(FileInstantUploadReq req);

    void saveFileAndAccountFile(FileUploadReq req, String storeFileObjectKey);

    List<AccountFileDAO> validateFileId(List<Long> fileIds, Long accountId);

    void findAllAccountFileDAOByRecursion(List<AccountFileDAO> allAccountFileDAOList, List<AccountFileDAO> preparedAccountFileDAOList, boolean onlyFolder);

    List<AccountFileDAOWithoutAutoGenId> findBatchCopyFilesRecursion(List<AccountFileDAO> toBeCopiedAccountFileDAOList, Long targetParentId);

    boolean checkAndUpdateStorageCapacity(Long accountId, Long fileSize);

    Long processDuplicatedFileName(AccountFileDAO accountFileDAO, Long parentId);

    List<AccountFileDTO> search(Long accountId, String searchKey);

    List<FileDownloadUrlDTO> getDownloadUrls(FileDownloadUrlReq req);
}
