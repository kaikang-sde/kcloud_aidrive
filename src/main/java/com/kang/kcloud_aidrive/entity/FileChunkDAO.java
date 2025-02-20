package com.kang.kcloud_aidrive.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 文件分片信息表
 * </p>
 *
 * @author Kai Kang
 */
@Getter
@Setter
@Entity
@Table(name = "file_chunk")
@Schema(name = "FileChunkDAO", description = "文件分片信息表")
@Accessors(chain = true)
public class FileChunkDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.kang.kcloud_aidrive.config.SnowflakeConfig")
    private Long id;

    @Schema(description = "文件唯一标识（md5）")
    @Column(name = "identifier")
    private String identifier;

    @Schema(description = "分片上传ID")
    @Column(name = "upload_id")
    private String uploadId;

    @Schema(description = "文件名")
    @Column(name = "file_name")
    private String fileName;

    @Schema(description = "所属桶名")
    @Column(name = "bucket_name")
    private String bucketName;

    @Schema(description = "文件的key")
    @Column(name = "object_key")
    private String objectKey;

    @Schema(description = "总文件大小（byte）")
    @Column(name = "total_size")
    private Long totalSize;

    @Schema(description = "每个分片大小（byte）")
    @Column(name = "chunk_size")
    private Long chunkSize;

    @Schema(description = "分片数量")
    @Column(name = "chunk_num")
    private Integer chunkNum;

    @Schema(description = "用户ID")
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "gmt_create")
    private Date gmtCreate;

    @Column(name = "gmt_modified")
    private Date gmtModified;
}
