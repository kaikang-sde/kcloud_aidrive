package com.kang.kcloud_aidrive.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Account File Table
 * batch copy operation
 * copy means create new files and its relationship, and save to DB target folder
 * Need manually set id before save to DB and also persist this manually generated id to DB.
 * If ID with GeneratedValue is used, the DB will overwrite the id which will break the relationship since the parent id is changed.
 * <p>
 * Solution: create a separate entity class and repository to avoid auto generated id
 * </p>
 *
 * @author Kai Kang
 */
@Getter
@Setter
@Entity
@Table(name = "account_file")
@Schema(name = "AccountFileDAO", description = "user file table")
@Filter(name = "deletedFilter", condition = "del = :isDeleted")
public class AccountFileDAOWithoutAutoGenId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @Id
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
