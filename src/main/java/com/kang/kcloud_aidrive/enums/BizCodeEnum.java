package com.kang.kcloud_aidrive.enums;

import lombok.Getter;

/**
 * Enumerated status codes - 枚举状态码
 *
 * @author Kai Kang
 */
public enum BizCodeEnum {
    /**
     * accounts related
     */
    ACCOUNT_REPEAT(250001, "The account already exists."),
    ACCOUNT_UNREGISTER(250002, "The account does not exist."),
    ACCOUNT_PWD_ERROR(250003, "Incorrect account or password."),
    ACCOUNT_UNLOGIN(250004, "The account is not logged in."),

    /**
     * files related
     */
    FILE_NOT_EXISTS(220404, "The file does not exist."),
    FILE_RENAME_REPEAT(220405, "Duplicate file name."),
    FILE_DEL_BATCH_ILLEGAL(220406, "File deletion parameter error."),
    FILE_TYPE_ERROR(220407, "Incorrect file type."),
    FILE_CHUNK_TASK_NOT_EXISTS(230408, "The shard task does not exist."),
    FILE_CHUNK_NOT_ENOUGH(230409, "The number of shards does not match; merge incomplete."),
    FILE_STORAGE_NOT_ENOUGH(240403, "Insufficient storage space."),
    FILE_TARGET_PARENT_ILLEGAL(250403, "The target parent directory is invalid."),
    SHARE_CANCEL_ILLEGAL(260403, "Failed to cancel sharing, invalid parameter."),
    SHARE_CODE_ILLEGAL(260404, "Invalid share code."),
    SHARE_NOT_EXIST(260405, "The share does not exist."),
    SHARE_CANCEL(260406, "The share has been canceled."),
    SHARE_EXPIRED(260407, "The share has expired."),
    SHARE_FILE_ILLEGAL(260408, "The shared file is non-compliant.");

    @Getter
    private String message;

    @Getter
    private int code;

    private BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
