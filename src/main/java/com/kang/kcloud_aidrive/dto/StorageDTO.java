package com.kang.kcloud_aidrive.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 存储信息表
 * </p>
 *
 * @author Kai Kang,
 * @since 2025-01-19
 */
@Getter
@Setter
@Schema(name = "StorageDTO", description = "存储信息")
public class StorageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(description = "Associated Account")
    private Long accountId;

    @Schema(description = "Storage Occupied Size")
    private Long usedSize;

    @Schema(description = "Total Capacity Size, Byte Storage")
    private Long totalSize;

    @Schema(description = "Created Time - EST")
    private Date estCreate;

    @Schema(description = "Modified Time - EST")
    private Date estModified;
}
