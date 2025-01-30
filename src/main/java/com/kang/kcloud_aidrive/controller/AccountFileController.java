package com.kang.kcloud_aidrive.controller;

import com.kang.kcloud_aidrive.controller.req.FileUpdateReq;
import com.kang.kcloud_aidrive.controller.req.FileUploadReq;
import com.kang.kcloud_aidrive.controller.req.FolderCreateReq;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.dto.FolderTreeNodeDTO;
import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.util.JsonData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * File Controller
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
    @Operation(
            summary = "List files based on current directory(parent ID)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful response",
                            content = @Content(
                                    schema = @Schema(implementation = JsonData.class)
                            )
                    )
            }
    )
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
    @Operation(
            summary = "Create a folder for the current directory(parent ID) and Account ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful response",
                            content = @Content(
                                    schema = @Schema(implementation = JsonData.class)
                            )
                    )
            }
    )
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
    @GetMapping("folder_tree")
    @Operation(summary = "get all folders in the current Account ID")
    public ResponseEntity<JsonData> folderTree() {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<FolderTreeNodeDTO> list = accountFileService.folderTreeV1(accountId);
        // List<FolderTreeNodeDTO> list = accountFileService.folderTreeV2(accountId);
        return ResponseEntity.ok(JsonData.buildSuccess(list));
    }

    /**
     * small file upload
     */
    @PostMapping("small_file")
    @Operation(summary = "small file upload")
    public ResponseEntity<JsonData> upload(FileUploadReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        accountFileService.uploadFile(req);
        return ResponseEntity.ok(JsonData.buildSuccess("File uploaded successfully"));
    }
}