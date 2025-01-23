package com.kang.kcloud_aidrive.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户文件表
 * </p>
 *
 * @author Kai Kang,
 * @since 2025-01-19
 */
@Getter
@Setter
@Table(name = "account_file")
@Schema(name = "AccountFileDAO", description = "user file table")
public class AccountFileDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.kang.kcloud_aidrive.config.SnowflakeConfig")
    private Long id;

    @Schema(description = "account ID")
    @Column(name = "account_id")
    private Long accountId;

    @Schema(description = "status - 0: not dir，1 - dir")
    @Column(name = "is_dir")
    private Integer isDir;

    @Schema(description = "parent ID, root dir is 0")
    @Column(name = "parent_id")
    private Long parentId;

    @Schema(description = "file ID，stored file")
    @Column(name = "file_id")
    private Long fileId;

    @Schema(description = "file name")
    @Column(name = "file_name")
    private String fileName;

    @Schema(description = "file Type：common 、compress 、  excel  、 word  、 pdf  、 txt  、 img  、audio  、video 、ppt 、code  、 csv")
    @Column(name = "file_type")
    private String fileType;

    @Schema(description = "file suffix")
    @Column(name = "file_suffix")
    private String fileSuffix;

    @Schema(description = "file size, byte")
    @Column(name = "file_size")
    private Long fileSize;

    @Schema(description = "Logical Deletion - 1 deleted, 0 not deleted")
    @Column(name = "del")
    @TableLogic
    private Boolean del;

    @Schema(description = "delete time")
    @Column(name = "del_time")
    private Date delTime;

    @Schema(description = "Modified Time - EST")
    @Column(name = "est_modified")
    private Date estModified;

    @Schema(description = "Created Time - EST")
    @Column(name = "est_create")
    private Date estCreate;
}
