package com.kang.kcloud_aidrive.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Account File Table
 * </p>
 *
 * @author Kai Kang
 */
@Getter
@Setter
@Entity
@Table(name = "account_file")
@SQLRestriction("del = 0")
// Automatically filter out deleted records, this is the global setting, when want to show deleted files in Recycle Bin, need to use native query
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
    @Column(name = "del", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean del = false;

    @Schema(description = "delete time")
    @Column(name = "del_time")
    private Date delTime;

    @Schema(description = "Created Time - EST")
    @Column(name = "est_create", insertable = false, updatable = false)
    private Date estCreate;

    @Schema(description = "Modified Time - EST")
    @Column(name = "est_modified", insertable = false)
    private Date estModified;

    @PrePersist
    public void prePersist() {
        if (del == null) {
            del = false;
        }
    }
}
