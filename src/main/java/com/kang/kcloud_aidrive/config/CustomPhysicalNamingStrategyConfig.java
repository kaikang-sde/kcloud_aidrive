package com.kang.kcloud_aidrive.config;

import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Author: Kai Kang
 */
public class CustomPhysicalNamingStrategyConfig extends PhysicalNamingStrategyStandardImpl {
    public String toPhysicalColumnName(String name, JdbcEnvironment context) {
        return convertToUnderscore(name);
    }

    private String convertToUnderscore(String name) {
        // 自定义字段命名转换逻辑
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
