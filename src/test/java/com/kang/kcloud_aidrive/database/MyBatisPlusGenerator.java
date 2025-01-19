package com.kang.kcloud_aidrive.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import org.apache.ibatis.type.JdbcType;

import java.util.Collections;

/**
 * uses MyBatis-Plus code generator to automate the generation of Java files
 * (like entities, mappers, and XML files) for interacting with a database.
 * Author: Kai Kang
 */
public class MyBatisPlusGenerator {

    public static void main(String[] args) {

        String userName = "root";
        String password = "XX"; // update to  real password
        String serverInfo = "54.163.61.180:3306"; // update to real ip
        String targetModuleNamePath = "/";
        String dbName = "xx"; // update to real database name

        // update to real table names
        String[] tables = {"account", "file", "account_file", "file_chunk", "file_suffix", "file_type", "share", "share_file", "storage"};


        // Using FastAutoGenerator class to generate code based on the provided database connection details and table names
        FastAutoGenerator.create("jdbc:mysql://" + serverInfo + "/" + dbName + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&tinyInt1isBit=true", userName, password) // connects to the target database
                .globalConfig(builder -> { // Configures global settings for code generation.
                    builder.author("Kai Kang,") // Set the author name for generated files.
                            .commentDate("yyyy-MM-dd") // Set the comment date format for generated files.
                            .enableSpringdoc() // Enable integration with Springdoc for documentation.
                            .disableOpenDir() // Prevent the output directory from opening automatically after generation.
                            .dateType(DateType.ONLY_DATE)   // Use `java.util.Date` for date fields instead of LocalDateTime.
                            .outputDir(System.getProperty("user.dir") + targetModuleNamePath + "/src/main/java"); // Specify the output directory for Java files.
                })
                .packageConfig(builder -> { // Defines the package structure and paths for generated files.
                    builder.parent("com.kang")     // Set the parent package name for generated files.
                            .entity("model")      // Specify `model` as the package name for Entity classes.
                            .mapper("mapper")     // Specify `mapper` as the package name for Mapper interfaces.
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + targetModuleNamePath + "/src/main/resources/mapper")); // Specify the directory for XML files.
                })
                .dataSourceConfig(builder -> { // Configures type conversion between database column types and Java types.
                    builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                        if (JdbcType.TINYINT == metaInfo.getJdbcType()) {
                            return DbColumnType.BOOLEAN; // Map TINYINT to Java Boolean.
                        }
                        return typeRegistry.getColumnType(metaInfo); // Use default type conversion for other types.
                    });
                })
                .strategyConfig(builder -> { // Configures strategies for generating code (e.g., entities, mappers, XML files).
                    builder.addInclude(tables)  // Specify the tables to generate code for.
                            .entityBuilder() // Start configuring Entity generation.
                            .enableFileOverride() // Allow overwriting existing Entity files.
                            .idType(IdType.ASSIGN_ID) // Use Snowflake algorithm for primary key generation.
                            .enableLombok() // Use Lombok annotations (like `@Data`, `@Builder`) in entities.
                            .logicDeleteColumnName("del") // Specify the column for logical deletion.
                            .enableTableFieldAnnotation() // Add annotations to fields (e.g., `@TableField`).
                            .formatFileName("%sDO") // Format Entity class names as `<TableName>DO`.

                            .controllerBuilder().disable() // Disable Controller generation.
                            .serviceBuilder().disable() // Disable Service and ServiceImpl generation.

                            .mapperBuilder() // Start configuring Mapper generation.
                            .enableFileOverride() // Allow overwriting existing Mapper files.
                            .formatMapperFileName("%sMapper") // Format Mapper interface names as `<TableName>Mapper`.
                            .superClass(BaseMapper.class) // Specify a parent class for Mapper interfaces.
                            .enableBaseResultMap() // Enable generation of MyBatis result maps.
                            .enableBaseColumnList() // Enable generation of SQL fragments for base columns.
                            .formatXmlFileName("%sMapper"); // Format XML file names as `<TableName>Mapper`.
                })
                .templateConfig(builder -> { // Disables specific templates for auto-generation to exclude unwanted layers.
                    builder.disable(TemplateType.CONTROLLER, TemplateType.SERVICE, TemplateType.SERVICE_IMPL); // // Disable templates for Controller, Service, and ServiceImpl.
                })
                .execute(); // Execute the code generation process.
    }


}