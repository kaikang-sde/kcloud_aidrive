package com.kang.kcloud_aidrive.controller;

import com.kang.kcloud_aidrive.annotation.ShareCodeCheck;
import com.kang.kcloud_aidrive.aspect.ShareCodeAspect;
import com.kang.kcloud_aidrive.controller.req.*;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.dto.ShareDTO;
import com.kang.kcloud_aidrive.dto.ShareDetailDTO;
import com.kang.kcloud_aidrive.dto.ShareSimpleDTO;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import com.kang.kcloud_aidrive.service.ShareService;
import com.kang.kcloud_aidrive.util.JsonData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * File Sharing Controller
 *
 * @author Kai Kang
 */
@RestController
@RequestMapping("/api/shares/v1")
@Tag(name = "File Sharing APIs")
public class ShareController {
    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    /**
     * Get my sharing list
     */
    @GetMapping
    @Operation(summary = "list my shares")
    public ResponseEntity<JsonData> listShares() {
        List<ShareDTO> shareDTOList = shareService.listShares();
        return ResponseEntity.ok(JsonData.buildSuccess(shareDTOList));
    }

    @PostMapping
    @Operation(summary = "create a share")
    public ResponseEntity<JsonData> createShare(@RequestBody ShareCreateReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        ShareDTO shareDTO = shareService.createShare(req);
        return ResponseEntity.ok(JsonData.buildSuccess(shareDTO));
    }

    @DeleteMapping
    @Operation(summary = "cancel shares")
    public ResponseEntity<JsonData> cancelShares(@RequestBody ShareCancelReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        shareService.cancelShares(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }

    /**
     * /api/shares/v1/shared?shareId={shareId}
     *
     * @param shareId
     * @return
     */
    @GetMapping("shared")
    @Operation(summary = "Visit shares, return basic sharing information - /api/shares/v1/shared?shareId=xxx")
    public ResponseEntity<JsonData> visitShares(@RequestParam(value = "shareId") Long shareId) {
        ShareSimpleDTO shareSimpleDTO = shareService.getSharesSimpleDetail(shareId);
        return ResponseEntity.ok(JsonData.buildSuccess(shareSimpleDTO));
    }

    @PostMapping("shared/code")
    @Operation(summary = "Verify the share code and return a temporary token.")
    public ResponseEntity<JsonData> visitShares(@RequestParam ShareCheckReq req) {
        String shareToken = shareService.checkSharesCode(req);
        if (shareToken == null) {
            return ResponseEntity.badRequest().body(JsonData.buildResult(BizCodeEnum.SHARE_NOT_EXIST));
        }
        return ResponseEntity.ok(JsonData.buildSuccess(shareToken));
    }

    @GetMapping("shared/detail")
    @ShareCodeCheck
    @Operation(summary = "Visit shares details")
    public ResponseEntity<JsonData> visitSharesDetail() {
        // 拦截方法，获取token，解密得到SharedId
        ShareDetailDTO shareDetailDTO = shareService.getSharesDetail(ShareCodeAspect.get());
        return ResponseEntity.ok(JsonData.buildSuccess(shareDetailDTO));
    }

    /**
     * 前端传过来parentId，表示想要进入哪一个文件夹的id
     *
     * @param req
     * @return
     */
    @PostMapping("shared/files")
    @ShareCodeCheck
    @Operation(summary = "List shared files")
    public ResponseEntity<JsonData> listSharedFile(@RequestBody SharedFileQueryReq req) {
        req.setShareId(ShareCodeAspect.get());
        List<AccountFileDTO> accountFileDTOList = shareService.listSharedFiles(req);
        return ResponseEntity.ok(JsonData.buildSuccess(accountFileDTOList));
    }

    @PostMapping("shared/transferring")
    @ShareCodeCheck
    @Operation(summary = "Transfer files")
    public ResponseEntity<JsonData> transferFile(@RequestBody SharedFileTransferReq req) {
        req.setShareId(ShareCodeAspect.get());
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        shareService.transferFile(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }

}
