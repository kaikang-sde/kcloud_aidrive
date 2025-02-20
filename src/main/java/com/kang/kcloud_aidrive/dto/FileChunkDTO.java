package com.kang.kcloud_aidrive.dto;

import com.kang.kcloud_aidrive.entity.FileChunkDAO;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;

/**
 * @author Kai Kang
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FileChunkDTO {

    public FileChunkDTO(FileChunkDAO fileChunkDAO) {
        SpringBeanUtil.copyProperties(fileChunkDAO, this);
    }

    private boolean isFinished;
    private List<CompletedPart> existingPartList;

    private Long id;

    @Schema(description = "文件唯一标识（md5）")
    private String identifier;

    @Schema(description = "分片上传ID")
    private String uploadId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "所属桶名")
    private String bucketName;

    @Schema(description = "文件的key")
    private String objectKey;

    @Schema(description = "总文件大小（byte）")
    private Long totalSize;

    @Schema(description = "每个分片大小（byte）")
    private Long chunkSize;

    @Schema(description = "分片数量")
    private Integer chunkNum;

    @Schema(description = "用户ID")
    private Long accountId;
}
