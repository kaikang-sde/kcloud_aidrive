package com.kang.kcloud_aidrive.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * File Table
 * </p>
 *
 * @author Kai Kang
 */
@Getter
@Setter
@Entity
@Table(name = "file")
@Filter(name = "deletedFilter", condition = "del = :isDeleted")
@Schema(name = "FileDAO", description = "user file table")
public class FileDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "file id")
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.kang.kcloud_aidrive.config.SnowflakeConfig")
    private Long id;

    @Schema(description = "account id，是哪个用户初次上传的")
    @Column(name = "account_id")
    private Long accountId;

    @Schema(description = "文件名称，秒传需要用到，冗余存储")
    @Column(name = "file_name")
    private String fileName;

    @Schema(description = "文件的后缀拓展名，冗余存储")
    @Column(name = "file_suffix")
    private String fileSuffix;

    @Schema(description = "文件大小，字节，冗余存储")
    @Column(name = "file_size")
    private Long fileSize;

    @Schema(description = "文件的key, 格式 日期/md5.拓展名，比如 2024-11-13/921674fd-cdaf-459a-be7b-109469e7050d.png")
    @Column(name = "object_key")
    private String objectKey;

    @Schema(description = "唯一标识，文件MD5")
    @Column(name = "identifier")
    private String identifier;

    @Schema(description = "Logical Deletion - 1 deleted, 0 not deleted")
    @Column(name = "del", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean del = false;

    @Schema(description = "更新时间")
    @Column(name = "est_create", insertable = false, updatable = false)
    private Date estModified;

    @Schema(description = "创建时间")
    @Column(name = "est_modified", insertable = false)
    private Date estCreate;

    @PrePersist
    public void prePersist() {
        if (del == null) {
            del = false;
        }
    }
}
