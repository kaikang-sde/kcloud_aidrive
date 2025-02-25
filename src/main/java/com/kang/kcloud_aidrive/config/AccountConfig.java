package com.kang.kcloud_aidrive.config;


/**
 * @author Kai Kang
 */
public class AccountConfig {
    public static final String ACCOUNT_SALT = "com.kang.kcloud_aidrive";

    // default storage size - 100MB
    public static final Long DEFAULT_STORAGE_SIZE = 100L * 1024L * 1024L;

    public static final String ROOT_FOLDER_NAME = "Root Folder";

    public static final Long ROOT_PARENT_ID = 0L;

    // front end address
    public static final String KCLOUD_AIDRIVE_FRONT_DOMAIN_SHARE_API = "127.0.0.1:9999/shares/";


}
