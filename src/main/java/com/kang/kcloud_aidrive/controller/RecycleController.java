package com.kang.kcloud_aidrive.controller;

import com.kang.kcloud_aidrive.controller.req.RecycleDeleteReq;
import com.kang.kcloud_aidrive.controller.req.RecycleRestoreReq;
import com.kang.kcloud_aidrive.dto.AccountFileDTO;
import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import com.kang.kcloud_aidrive.service.RecycleService;
import com.kang.kcloud_aidrive.util.JsonData;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Recycle Controller - Trash Bin
 *
 * @author Kai Kang
 */
@RestController
@RequestMapping("/api/recycle-bin/v1")
@Tag(name = "Recycle APIs")
public class RecycleController {
    private final RecycleService recycleService;

    public RecycleController(RecycleService recycleService) {
        this.recycleService = recycleService;
    }

    @GetMapping
    public ResponseEntity<JsonData> listRecycle() {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<AccountFileDTO> accountFileDTOList = recycleService.listRecycleFiles(accountId);
        return ResponseEntity.ok(JsonData.buildSuccess(accountFileDTOList));
    }

    @DeleteMapping
    public ResponseEntity<JsonData> deleteRecycle(@RequestBody RecycleDeleteReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        recycleService.deleteRecycleFiles(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }

    @PutMapping("restore")
    public ResponseEntity<JsonData> restoreRecycle(@RequestBody RecycleRestoreReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        recycleService.restoreRecycleFiles(req);
        return ResponseEntity.ok(JsonData.buildSuccess());
    }


}
