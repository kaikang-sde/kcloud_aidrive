package com.kang.kcloud_aidrive.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.kang.kcloud_aidrive.component.StorageEngine;
import com.kang.kcloud_aidrive.config.MinioConfig;
import com.kang.kcloud_aidrive.controller.req.FileChunkInitTaskReq;
import com.kang.kcloud_aidrive.controller.req.FileChunkMergeReq;
import com.kang.kcloud_aidrive.controller.req.FileUploadReq;
import com.kang.kcloud_aidrive.dto.FileChunkDTO;
import com.kang.kcloud_aidrive.entity.FileChunkDAO;
import com.kang.kcloud_aidrive.entity.StorageDAO;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.repository.FileChunkRepository;
import com.kang.kcloud_aidrive.repository.StorageRepository;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.service.FileChunkService;
import com.kang.kcloud_aidrive.util.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Kai Kang
 */
@Service
@Slf4j
public class FileChunkServiceImpl implements FileChunkService {
    private final StorageRepository storageRepository;
    private final StorageEngine storageEngine;
    private final FileChunkRepository fileChunkRepository;
    private final MinioConfig minioConfig;
    private final AccountFileService accountFileService;
    private final OrderedFormContentFilter formContentFilter;

    public FileChunkServiceImpl(StorageRepository storageRepository, StorageEngine storageEngine, FileChunkRepository fileChunkRepository, MinioConfig minioConfig, AccountFileService accountFileService, OrderedFormContentFilter formContentFilter) {
        this.storageRepository = storageRepository;
        this.fileChunkRepository = fileChunkRepository;
        this.storageEngine = storageEngine;
        this.minioConfig = minioConfig;
        this.accountFileService = accountFileService;
        this.formContentFilter = formContentFilter;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public FileChunkDTO initiateChunkUpload(FileChunkInitTaskReq req) {
        StorageDAO storageDAO = storageRepository.findByAccountId(req.getAccountId());
        if (storageDAO.getUsedSize() + req.getTotalSize() > storageDAO.getTotalSize()) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }

        String objectKey = CommonUtil.getFilePath(req.getFilename());
        String contentType = MediaTypeFactory.getMediaType(objectKey).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();

        // Initiate file chunk task
        // CreateMultipartUploadResponse contains the uploadId which uniquely identifies the multipart upload. This ID is required to complete or abort the multipart upload later.
        CreateMultipartUploadResponse uploadResult = storageEngine.initMultipartUploadTask(minioConfig.getBucketName(), objectKey, contentType);
        String uploadId = uploadResult.uploadId();

        // calculate the number of chunks
        int chunkNum = (int) Math.ceil(req.getTotalSize() * 1.0 / req.getChunkSize());
        FileChunkDAO fileChunkDAO = new FileChunkDAO();
        fileChunkDAO.setBucketName(minioConfig.getBucketName())
                .setChunkSize(req.getChunkSize())
                .setChunkNum(chunkNum)
                .setFileName(req.getFilename())
                .setIdentifier(req.getIdentifier())
                .setAccountId(req.getAccountId())
                .setTotalSize(req.getTotalSize())
                .setUploadId(uploadId)
                .setObjectKey(objectKey);
        fileChunkRepository.save(fileChunkDAO);
        return new FileChunkDTO(fileChunkDAO).setFinished(false).setExistingPartList(new ArrayList<>());
    }

    /**
     * 获取临时文件上传地址
     *
     * @param accountId
     * @param identifier
     * @param partNumber
     * @return
     */
    @Override
    public String getPresignedUploadUrl(Long accountId, String identifier, int partNumber) {
        FileChunkDAO fileChunkDAO = fileChunkRepository.findByAccountIdAndIdentifier(accountId, identifier);

        if (fileChunkDAO == null) {
            throw new BizException(BizCodeEnum.FILE_CHUNK_TASK_NOT_EXISTS);
        }

        String objectKey = CommonUtil.getFilePath(fileChunkDAO.getFileName());
        String contentType = MediaTypeFactory.getMediaType(objectKey).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();


        // 配置预签名， 过期时间
        Date expireTime = DateUtil.offsetMillisecond(DateTime.now(), minioConfig.getPreSignURLExpire().intValue());
        URL presignedUrl = storageEngine.genePreSignedUrl(fileChunkDAO.getBucketName(), fileChunkDAO.getObjectKey(), expireTime, partNumber, fileChunkDAO.getUploadId(), contentType);

        log.info("Generated Pre-Signed URL: {}", presignedUrl);
        return presignedUrl.toString();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void mergeChunks(FileChunkMergeReq req) {
        FileChunkDAO fileChunkDAO = fileChunkRepository.findByAccountIdAndIdentifier(req.getAccountId(), req.getIdentifier());

        if (fileChunkDAO == null) {
            throw new BizException(BizCodeEnum.FILE_CHUNK_TASK_NOT_EXISTS);
        }

        ListPartsResponse listPartsResponse = storageEngine.listMultipart(fileChunkDAO.getBucketName(), fileChunkDAO.getObjectKey(), fileChunkDAO.getUploadId());
        List<Part> parts = listPartsResponse.parts();
        if (parts.size() != fileChunkDAO.getChunkNum()) {
            // upload not finished! Cannot merge
            throw new BizException(BizCodeEnum.FILE_CHUNK_NOT_ENOUGH);
        }

        StorageDAO storageDAO = storageRepository.findByAccountId(req.getAccountId());

        long realFileTotalSize = parts.stream().map(Part::size).mapToLong(Long::valueOf).sum();
        if (storageDAO.getUsedSize() + realFileTotalSize > storageDAO.getTotalSize()) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }
        storageDAO.setUsedSize(storageDAO.getUsedSize() + realFileTotalSize);
        storageRepository.updateUsedSizeById(storageDAO.getId(), realFileTotalSize);


        CompleteMultipartUploadResponse mergeResult = storageEngine.mergeChunks(minioConfig.getBucketName(),
                fileChunkDAO.getObjectKey(),
                fileChunkDAO.getUploadId(),
                // Convert List<Part> to List<CompletedPart> which can then be used in CompleteMultipartUploadRequest
                parts.stream().map(part -> CompletedPart.builder().partNumber(part.partNumber()).eTag(part.eTag()).build()).collect(Collectors.toList()));

        if (mergeResult.eTag() != null) {
            FileUploadReq fileUploadReq = new FileUploadReq();
            fileUploadReq.setAccountId(req.getAccountId())
                    .setFileName(fileChunkDAO.getFileName())
                    .setIdentifier(req.getIdentifier())
                    .setParentId(req.getParentId())
                    .setFileSize(realFileTotalSize)
                    .setFile(null);

            // 存储文件和关联信息到数据库
            accountFileService.saveFileAndAccountFile(fileUploadReq, fileChunkDAO.getObjectKey());

            // 删除临时文件 任务记录
            fileChunkRepository.deleteById(fileChunkDAO.getId());

            log.info("File {} merged successfully.", fileChunkDAO.getObjectKey());
        }
    }

    @Override
    public FileChunkDTO listFileChunk(Long accountId, String identifier) {
        FileChunkDAO fileChunkDAO = fileChunkRepository.findByAccountIdAndIdentifier(accountId, identifier);
        if (fileChunkDAO == null) {
            throw new BizException(BizCodeEnum.FILE_CHUNK_TASK_NOT_EXISTS);
        }
        FileChunkDTO fileChunkDTO = new FileChunkDTO(fileChunkDAO);

        // 判断任务是否在文件服务器存在
        boolean doesObjectExist = storageEngine.doesObjectExist(fileChunkDAO.getBucketName(), fileChunkDAO.getObjectKey());
        if (!doesObjectExist) {
            // 任务在文件服务器不存在，说明未完成上传，返回已经上传的分片概述
            ListPartsResponse listPartsResponse = storageEngine.listMultipart(fileChunkDAO.getBucketName(), fileChunkDAO.getObjectKey(), fileChunkDAO.getUploadId());
            if (listPartsResponse.parts().size() == fileChunkDAO.getChunkNum()) {
                // 上传完成，可以合并
                fileChunkDTO.setFinished(true)
                        .setExistingPartList(
                                listPartsResponse.parts().stream()
                                        .map(part -> CompletedPart.builder()
                                                .partNumber(part.partNumber())
                                                .eTag(part.eTag())
                                                .build())
                                        .collect(Collectors.toList())
                        );
            } else {
                // 未完成上传，还不能合并
                fileChunkDTO.setFinished(false)
                        .setExistingPartList(
                                listPartsResponse.parts().stream()
                                        .map(part -> CompletedPart.builder()
                                                .partNumber(part.partNumber())
                                                .eTag(part.eTag())
                                                .build())
                                        .collect(Collectors.toList())
                        );
            }
        }

        return fileChunkDTO;
    }
}
