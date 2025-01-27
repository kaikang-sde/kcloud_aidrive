package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.FileUpdateReq;
import com.kang.kcloud_aidrive.controller.req.FolderCreateReq;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;

import java.util.List;

public interface AccountFileService {
    // create folder
    Long createFolder(FolderCreateReq folderCreateReq);
    List<AccountFileDTO> listFile(Long accountId, Long parentId);

    void renameFile(FileUpdateReq req);
}
