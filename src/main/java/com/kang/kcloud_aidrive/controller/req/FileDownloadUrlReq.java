package com.kang.kcloud_aidrive.controller.req;

import lombok.Data;

import java.util.List;

@Data
public class FileDownloadUrlReq {
    private Long accountId;
    private List<Long> fileIds;
}
