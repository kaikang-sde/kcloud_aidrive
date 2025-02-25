package com.kang.kcloud_aidrive.enums;

import lombok.Getter;

/**
 * 分享状态  used正常, expired已失效,  cancelled 取消
 *
 * @author Kai Kang
 */
@Getter
public enum ShareStatusEnum {
    USED,
    EXPIRED,
    CANCELLED;
}
