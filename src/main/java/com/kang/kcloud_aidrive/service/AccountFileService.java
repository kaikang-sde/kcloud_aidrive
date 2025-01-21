package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.FolderCreateReq;

public interface AccountFileService {
    // create folder
    void createFolder(FolderCreateReq folderCreateReq);
}
