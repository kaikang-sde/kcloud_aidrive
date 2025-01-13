package com.kang.kcloud_aidrive.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonUtilTest {

    @Test
    void testGetFileSuffix_ValidFileName() {
        assertEquals("txt", CommonUtil.getFileSuffix("document.txt"));
        assertEquals("jpg", CommonUtil.getFileSuffix("image.jpg"));
        assertEquals("png", CommonUtil.getFileSuffix("photo.png"));
    }

    @Test
    void testGetFileSuffix_MultipleDots() {
        assertEquals("log", CommonUtil.getFileSuffix("server.log.backup.log"));
        assertEquals("gz", CommonUtil.getFileSuffix("archive.tar.gz"));
    }

    @Test
    void testGetFileSuffix_NoExtension() {
        // File names without an extension
        assertEquals("", CommonUtil.getFileSuffix("filename"));
        assertEquals("", CommonUtil.getFileSuffix("file."));
    }


}
