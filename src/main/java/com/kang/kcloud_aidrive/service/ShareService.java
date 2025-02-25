package com.kang.kcloud_aidrive.service;

import com.kang.kcloud_aidrive.controller.req.*;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.dto.ShareDTO;
import com.kang.kcloud_aidrive.dto.ShareDetailDTO;
import com.kang.kcloud_aidrive.dto.ShareSimpleDTO;

import java.util.List;

/**
 * @author Kai Kang
 */
public interface ShareService {
    List<ShareDTO> listShares();

    ShareDTO createShare(ShareCreateReq req);

    void cancelShares(ShareCancelReq req);

    ShareSimpleDTO getSharesSimpleDetail(Long shareId);

    String checkSharesCode(ShareCheckReq req);

    ShareDetailDTO getSharesDetail(Long shareId);

    List<AccountFileDTO> listSharedFiles(SharedFileQueryReq req);

    void transferFile(SharedFileTransferReq req);
}
