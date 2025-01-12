package com.kang.kcloud_aidrive.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Common tool - 通用工具
 *
 * @author Kai Kang
 */

@Slf4j
public class CommonUtil {

    /**
     * Respond with JSON data to the frontend
     *
     * @param response
     * @param obj
     */
    public static void sendJsonMessage(HttpServletResponse response, Object obj) {

        response.setContentType("application/json; charset=utf-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.print(JsonUtil.obj2Json(obj));
            response.flushBuffer();

        } catch (IOException e) {
            log.warn("Exception when returning JSON data to the frontend:{}", e.getMessage());
        }
    }

    /**
     * Retrieve file suffix from file name
     * 根据文件名称获取文件后缀
     */
    public static String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    /**
     * Generate file storage path based on file suffix: year/month/day/uuid.suffix format
     * 根据文件后缀，生成文件存储：年/月/日/uuid.suffix 格式
     */
    public static String getFilePath(String fileName) {
        String suffix = getFileSuffix(fileName);
        // Generate a unique key for the file in the storage bucket/生成文件在存储桶中的唯一键
        return StrUtil.format("{}/{}/{}/{}.{}", DateUtil.thisYear(), DateUtil.thisMonth() + 1, DateUtil.thisDayOfMonth(), IdUtil.randomUUID(), suffix);
    }

    /**
     * same as getFilePath method without Hutool package
     *
     * @param fileName
     * @return
     */
    public static String getFilePathWithoutHutool(String fileName) {
        String suffix = getFileSuffix(fileName);

        // Get current date
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        // Generate a unique file path
        String filePath = String.format("%d/%02d/%02d/%s.%s", year, month, day, UUID.randomUUID(), suffix);
        return filePath;
    }
}