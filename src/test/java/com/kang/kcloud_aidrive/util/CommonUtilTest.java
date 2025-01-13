package com.kang.kcloud_aidrive.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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

    @Test
    void testGetFilePath_ValidFileName() {
        String fileName = "example.JPG";
        String result = CommonUtil.getFilePath(fileName);

        LocalDate mockDate = LocalDate.of(2025, 1, 13);
        String expectedDate = mockDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/";

        assertTrue(result.startsWith(expectedDate), "The file path should start with the correct date.");
        assertTrue(result.endsWith(".JPG"), "The file path should end with the correct suffix.");

        String uuidPart = result.substring(expectedDate.length(), result.lastIndexOf('.'));
        assertTrue(isValidUUID(uuidPart), "The UUID part of the file path should be valid.");
    }

    private boolean isValidUUID(String uuid) {
        String uuidRegex = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
        return Pattern.matches(uuidRegex, uuid);
    }
}
