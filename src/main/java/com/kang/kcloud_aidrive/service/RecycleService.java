package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.RecycleDeleteReq;
import com.kang.kcloud_aidrive.controller.req.RecycleRestoreReq;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;

import java.util.List;

/**
 * @author Kai Kang
 */
public interface RecycleService {
    List<AccountFileDTO> listRecycleFiles(Long accountId);

    void deleteRecycleFiles(RecycleDeleteReq req);

    void restoreRecycleFiles(RecycleRestoreReq req);
}
