package com.kang.kcloud_aidrive.controller;

import com.kang.kcloud_aidrive.controller.req.*;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.dto.FolderTreeNodeDTO;
import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.util.JsonData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Account File Controller
 * Author: Kai Kang
 */
@RestController
@RequestMapping("/api/files/v1")
@Tag(name = "File Management APIs")
public class AccountFileController {

    private final AccountFileService accountFileService;

    public AccountFileController(AccountFileService accountFileService) {
        this.accountFileService = accountFileService;
    }

    /**
     * /api/files/v1/{parentId}
     *
     * @param parentId
     * @return
     */
    @GetMapping
    @Operation(summary = "List files based on current directory(parent ID)")
    public ResponseEntity<JsonData> list(
            @Parameter(
                    description = "Parent ID of the files to list",
                    required = true
            )
            @RequestParam("parent_id") Long parentId
    ) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<AccountFileDTO> accountFileDTOList = accountFileService.listFile(accountId, parentId);

        return ResponseEntity.ok(JsonData.buildSuccess(accountFileDTOList));
    }

    /**
     * /api/files/v1/folder
     *
     * @param req
     * @return
     */
    @PostMapping("folder")
    @Operation(summary = "Create a folder for the current directory(parent ID) and Account ID")
    public ResponseEntity<JsonData> createFolder(@RequestBody FolderCreateReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        accountFileService.createFolder(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }

    /**
     * file or folder rename
     * /api/files/v1/{fileId}
     */
    @PostMapping
    @Operation(summary = "rename file - /api/files/v1/{fileId}")
    public ResponseEntity<JsonData> rename(@RequestParam("file_id") Long fileId, @RequestBody FileUpdateReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setFileId(fileId);
        req.setAccountId(accountId);
        accountFileService.renameFile(req);
        return ResponseEntity.ok(JsonData.buildSuccess("File renamed successfully with new name: " + req.getNewFileName()));
    }

    /**
     * query fileTree
     */
    @GetMapping("folder-tree")
    @Operation(summary = "get all folders in the current Account ID")
    public ResponseEntity<JsonData> folderTree() {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<FolderTreeNodeDTO> list = accountFileService.folderTreeV1(accountId);
        // List<FolderTreeNodeDTO> list = accountFileService.folderTreeV2(accountId);
        return ResponseEntity.ok(JsonData.buildSuccess(list));
    }

    /**
     * small file upload
     * SpringBoot Data Binding default behavior - No annotation is needed if parameters are part of form-data
     * FileUploadReq is passed as a method parameter without @RequestBody,
     * Spring treats it as a form data object rather than a JSON request body.
     * If the request is a multipart/form-data or application/x-www-form-urlencoded,
     * Spring will map the request parameters to the fields of FileUploadReq automatically.
     */
    @PostMapping("uploads/small-file")
    @Operation(summary = "small file upload")
    public ResponseEntity<JsonData> upload(FileUploadReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        accountFileService.uploadFile(req);
        return ResponseEntity.ok(JsonData.buildSuccess("File uploaded successfully"));
    }

    @PostMapping("batch/move")
    @Operation(summary = "file batch move operations")
    public ResponseEntity<JsonData> batchMove(@RequestBody FileBatchReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        accountFileService.batchMove(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }

    @DeleteMapping("batch")
    @Operation(summary = "Deletes multiple files based on their IDs.")
    public ResponseEntity<JsonData> delete(@RequestBody FileDeletionReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        accountFileService.batchDeleteFiles(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }

    @PostMapping("batch/copy")
    @Operation(summary = "file batch copy operations")
    public ResponseEntity<JsonData> batchCopy(@RequestBody FileBatchReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        accountFileService.batchCopyFiles(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }

    /**
     * Instant upload
     * true: Instant upload success - The server skip actual file transfer if the file already exists (e.g., via hash comparison).
     * false: instant upload failed, need to invoke normal upload interface
     */
    @PostMapping("uploads/instant")
    @Operation(summary = "instant upload")
    public ResponseEntity<JsonData> instantUpload(@RequestBody FileInstantUploadReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        boolean canRapidUpload = accountFileService.instantUpload(req);
        return ResponseEntity.ok(JsonData.buildSuccess(canRapidUpload));
    }

}