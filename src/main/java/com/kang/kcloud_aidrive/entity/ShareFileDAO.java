package com.kang.kcloud_aidrive.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 文件分享表
 * </p>
 *
 * @author Kai Kang
 */
@Getter
@Setter
@Entity
@Table(name = "share_file")
@Schema(name = "ShareFileDAO", description = "文件分享表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareFileDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.kang.kcloud_aidrive.config.SnowflakeConfig")
    private Long id;

    @Schema(description = "分享id")
    @Column(name = "share_id")
    private Long shareId;

    @Schema(description = "用户文件的ID")
    @Column(name = "account_file_id")
    private Long accountFileId;

    @Schema(description = "创建者id")
    @Column(name = "account_id")
    private Long accountId;

    @Schema(description = "Created Time - EST")
    @Column(name = "est_create", insertable = false, updatable = false)
    private Date estCreate;

    @Schema(description = "Modified Time - EST")
    @Column(name = "est_modified", insertable = false)
    private Date estModified;
}
