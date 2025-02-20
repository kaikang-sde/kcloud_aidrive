package com.kang.kcloud_aidrive.entity;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

/**
 * <p>
 * storage table
 * </p>
 *
 * @author Kai Kang
 */
@Getter
@Setter
@Entity
@Table(name = "storage")
@Schema(name = "StorageDAO", description = "Storage Information Table")
public class StorageDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.kang.kcloud_aidrive.config.SnowflakeConfig")
    private Long id;

    @Schema(description = "Associated Account")
    @Column(name = "account_id")
    private Long accountId;

    @Schema(description = "Storage Occupied Size")
    @Column(name = "used_size")
    private Long usedSize;

    @Schema(description = "Total Capacity Size, Byte Storage")
    @Column(name = "total_size")
    private Long totalSize;

    @Schema(description = "Created Time - EST")
    @Column(name = "est_create", insertable = false, updatable = false)
    private Date estCreate;

    @Schema(description = "Modified Time - EST")
    @Column(name = "est_modified", insertable = false)
    private Date estModified;
}
