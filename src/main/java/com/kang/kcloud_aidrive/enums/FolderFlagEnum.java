package com.kang.kcloud_aidrive.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Kai Kang
 */
@AllArgsConstructor
@Getter
public enum FolderFlagEnum {
    NO(0),
    YES(1);

    private final Integer Code;
}
