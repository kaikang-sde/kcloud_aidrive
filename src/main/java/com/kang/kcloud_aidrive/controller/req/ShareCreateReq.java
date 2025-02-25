package com.kang.kcloud_aidrive.controller.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Kai Kang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShareCreateReq {
    private String shareName;
    // code, no code
    private String shareType;
    // 0 - permanent, 1 - 7 days, 2 - 30days
    private Integer shareDayType;
    private List<Long> fileIds;
    // shared person accountId
    private Long accountId;
}
