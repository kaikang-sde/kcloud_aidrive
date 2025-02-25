package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.config.AccountConfig;
import com.kang.kcloud_aidrive.config.SnowflakeConfig;
import com.kang.kcloud_aidrive.controller.req.*;
import com.kang.kcloud_aidrive.dto.*;
import com.kang.kcloud_aidrive.entity.*;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.enums.ShareDayTypeEnum;
import com.kang.kcloud_aidrive.enums.ShareStatusEnum;
import com.kang.kcloud_aidrive.enums.ShareTypeEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import com.kang.kcloud_aidrive.repository.*;
import com.kang.kcloud_aidrive.service.AccountFileService;
import com.kang.kcloud_aidrive.service.ShareService;
import com.kang.kcloud_aidrive.util.JwtUtil;
import com.kang.kcloud_aidrive.util.SpringBeanUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Kai Kang
 */
@Service
@Slf4j
public class ShareServiceImpl implements ShareService {
    private final ShareRepository shareRepository;
    private final ShareFileRepository shareFileRepository;
    private final AccountFileService accountFileService;
    private final SnowflakeConfig snowflakeConfig;
    private final AccountRepository accountRepository;
    private final AccountFileRepository accountFileRepository;
    private final AccountFileDAOWithoutAutoGenIdRepository accountFileDAOWithoutAutoGenIdRepository;


    public ShareServiceImpl(ShareRepository shareRepository, ShareFileRepository shareFileRepository, AccountFileService accountFileService, SnowflakeConfig snowflakeConfig, AccountRepository accountRepository, AccountFileRepository accountFileRepository, AccountFileDAOWithoutAutoGenIdRepository accountFileDAOWithoutAutoGenIdRepository) {
        this.shareFileRepository = shareFileRepository;
        this.shareRepository = shareRepository;
        this.accountFileService = accountFileService;
        this.snowflakeConfig = snowflakeConfig;
        this.accountRepository = accountRepository;
        this.accountFileRepository = accountFileRepository;
        this.accountFileDAOWithoutAutoGenIdRepository = accountFileDAOWithoutAutoGenIdRepository;
    }

    @Override
    public List<ShareDTO> listShares() {
        AccountDTO accountDTO = LoginInterceptor.threadLocal.get();
        List<ShareDAO> shareDAOList = shareRepository.findByAccountIdOrderByEstCreateDesc(accountDTO.getId());
        return SpringBeanUtil.copyProperties(shareDAOList, ShareDTO.class);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ShareDTO createShare(ShareCreateReq req) {
        List<Long> fileIds = req.getFileIds();
        accountFileService.validateFileId(fileIds, req.getAccountId());

        // generate sharing link
        Integer dayType = req.getShareDayType();
        Integer shareDays = ShareDayTypeEnum.getDaysByType(dayType);
        Long shareId = snowflakeConfig.generateId();
        String shareUrl = AccountConfig.KCLOUD_AIDRIVE_FRONT_DOMAIN_SHARE_API + shareId;
        log.info("ShareUrl : {}", shareUrl);

        ShareDAO shareDAO = ShareDAO.builder()
                .id(shareId)
                .shareName(req.getShareName())
                .shareType(ShareTypeEnum.valueOf(req.getShareType()).name())
                .shareDayType(dayType)
                .shareDay(shareDays)
                .shareUrl(shareUrl)
                .shareStatus(ShareStatusEnum.USED.name())
                .accountId(req.getAccountId())
                .build();

        // set share end time based on day type
        if (ShareDayTypeEnum.PERMANENT.getDayType().equals(dayType)) {
            shareDAO.setShareEndTime(Date.from(LocalDate.of(9999, 12, 31)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            shareDAO.setShareEndTime(new Date(System.currentTimeMillis() + shareDays * 24 * 3600 * 1000L));
        }

        // set share code if need code
        if (ShareTypeEnum.NEED_CODE.name().equalsIgnoreCase(req.getShareType())) {
            // generate file retrieving code
            String shareCode = RandomStringUtils.randomAlphabetic(6).toUpperCase();
            shareDAO.setShareCode(shareCode);
        }
        shareRepository.save(shareDAO);

        List<ShareFileDAO> shareFileDAOList = new ArrayList<>();
        fileIds.forEach(fileId -> {
            ShareFileDAO shareFileDAO = ShareFileDAO.builder()
                    .shareId(shareId)
                    .accountFileId(fileId)
                    .accountId(req.getAccountId())
                    .build();
            shareFileDAOList.add(shareFileDAO);
        });

        shareFileRepository.saveAll(shareFileDAOList);
        return SpringBeanUtil.copyProperties(shareDAO, ShareDTO.class);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void cancelShares(ShareCancelReq req) {
        List<ShareDAO> shareDAOList = shareRepository.findByIdInAndAccountId(req.getShareIds(), req.getAccountId());
        if (shareDAOList.size() != req.getShareIds().size()) {
            log.error("cancel shares, sharedIds: {}", req.getShareIds());
            throw new BizException(BizCodeEnum.SHARE_CANCEL_ILLEGAL);
        }

        // delete shares link
        shareRepository.deleteAllById(req.getShareIds());

        // delete shares details
        shareFileRepository.deleteByShareIdIn(req.getShareIds());
    }

    @Override
    public ShareSimpleDTO getSharesSimpleDetail(Long shareId) {
        ShareDAO shareDAO = checkSharesStatus(shareId);

        ShareSimpleDTO shareSimpleDTO = SpringBeanUtil.copyProperties(shareDAO, ShareSimpleDTO.class);

        ShareAccountDTO shareAccountDTO = getSharedAccount(shareDAO.getAccountId());

        shareSimpleDTO.setShareAccountDTO(shareAccountDTO);

        // 不需要校验码， 生成临时token
        if (ShareTypeEnum.NO_CODE.name().equalsIgnoreCase(shareDAO.getShareType())) {
            shareSimpleDTO.setShareToken(JwtUtil.geneShareJWT(shareDAO.getId()));
        }

        return shareSimpleDTO;
    }

    @Override
    public String checkSharesCode(ShareCheckReq req) {
        ShareDAO shareDAO = shareRepository.findByIdAndShareCodeAndShareStatus(req.getShareId(), req.getShareCode(), ShareStatusEnum.USED.name());
        if (shareDAO != null) {
            if (shareDAO.getShareEndTime().getTime() > System.currentTimeMillis()) {
                return JwtUtil.geneShareJWT(shareDAO.getId());
            } else {
                log.error("Shared link is expired: sharedId:{}", req.getShareId());
                throw new BizException(BizCodeEnum.SHARE_EXPIRED);
            }
        }
        return null;
    }

    @Override
    public ShareDetailDTO getSharesDetail(Long shareId) {
        // 查询分享记录
        ShareDAO shareDAO = checkSharesStatus(shareId);
        ShareDetailDTO shareDetailDTO = SpringBeanUtil.copyProperties(shareDAO, ShareDetailDTO.class);

        // 查询分享文件信息
        List<AccountFileDAO> accountFileDAOList = getSharesFileInfo(shareId);
        List<AccountFileDTO> accountFileDTOList = SpringBeanUtil.copyProperties(accountFileDAOList, AccountFileDTO.class);
        shareDetailDTO.setAccountFileDTOList(accountFileDTOList);

        // 查询分享着信息
        ShareAccountDTO shareAccountDTO = getSharedAccount(shareDAO.getAccountId());
        shareDetailDTO.setShareAccountDTO(shareAccountDTO);

        return shareDetailDTO;
    }

    @Override
    public List<AccountFileDTO> listSharedFiles(SharedFileQueryReq req) {
        // 检查分享链接状态是否正常 ShareStatusEnum.USED 为正常
        ShareDAO shareDAO = checkSharesStatus(req.getShareId());

        // 你想要进去的文件夹是否在分享列表中：查询分享ID是否在分享的文件列表中（需要获取分享文件列表的全部文件夹和子文件夹）
        List<AccountFileDAO> accountFileDAOList = checkSharedFileIdOnStatus(shareDAO.getId(), List.of(req.getParentId()));
        List<AccountFileDTO> accountFileDTOList = SpringBeanUtil.copyProperties(accountFileDAOList, AccountFileDTO.class);

        // 根据parentId分组
        Map<Long, List<AccountFileDTO>> fileListMap = accountFileDTOList.stream()
                .collect(Collectors.groupingBy(AccountFileDTO::getParentId));

        // 根据父文件ID获取子文件
        List<AccountFileDTO> childFileList = fileListMap.get(req.getParentId());
        if (CollectionUtils.isEmpty(childFileList)) {
            return List.of();
        }

        return childFileList;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void transferFile(SharedFileTransferReq req) {
        // 检查分享链接状态是否正常
        ShareDAO shareDAO = checkSharesStatus(req.getShareId());

        // 转存的文件是否是分享链接里面的文件
        checkInShareFiles(req.getFileIds(), req.getShareId());

        // 目标文件夹是否是当前用户的
        AccountFileDAO currentAccountDAO = accountFileRepository.findByIdAndAccountId(req.getParentId(), req.getAccountId());
        if (currentAccountDAO == null) {
            log.error("Parent file is not in current user: parentId:{}", req.getParentId());
            throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
        }

        // 获取转存的文件 - 第一层
        List<AccountFileDAO> sharedAccountFileDAOList = accountFileRepository.findAllById(req.getFileIds());

        // 递归找分享链接中的全部子文件 - 递归内层
        List<AccountFileDAOWithoutAutoGenId> batchTransferFileList = accountFileService.findBatchCopyFilesRecursion(sharedAccountFileDAOList, req.getParentId());

        // 同步更新所有文件的accountId为当前用户的id
        batchTransferFileList.forEach(accountFileDAOWithoutAutoGenId -> accountFileDAOWithoutAutoGenId.setAccountId(req.getAccountId()));

        // 计算存储空间大小，检查是否足够
        if (!accountFileService.checkAndUpdateStorageCapacity(req.getAccountId(),
                batchTransferFileList.stream()
                        .map(AccountFileDAOWithoutAutoGenId -> AccountFileDAOWithoutAutoGenId.getFileSize() == null ? 0 : AccountFileDAOWithoutAutoGenId.getFileSize())
                        .mapToLong(Long::valueOf).sum())) {
            log.error("Not enough space to transfer files: accountId:{}", req.getAccountId());
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }

        // 更新关联对象信息，存储文件映射关系
        accountFileDAOWithoutAutoGenIdRepository.saveAll(batchTransferFileList);
    }

    private void checkInShareFiles(List<Long> fileIds, Long shareId) {
        List<Long> shareFileIds = shareFileRepository.findAccountFileIdByShareId(shareId);

        // 找文件实体 - 第一层
        List<AccountFileDAO> sharedAccountFileDAOList = accountFileRepository.findAllById(shareFileIds);

        // 递归找分享链接中的全部子文件 - 递归内层
        List<AccountFileDAO> allSharedFiles = new ArrayList<>();
        accountFileService.findAllAccountFileDAOByRecursion(allSharedFiles, sharedAccountFileDAOList, false);

        // 提取全部文件的Id
        List<Long> allSharedFileIds = allSharedFiles.stream().map(AccountFileDAO::getId).toList();

        // 前端传过来的文件集合fileIds， 和当前所有的分享链接的匹配一下是否合规
        for (Long fileId : fileIds) {
            if (!allSharedFileIds.contains(fileId)) {
                log.error("File is not in share: fileId:{}", fileId);
                throw new BizException(BizCodeEnum.SHARE_FILE_ILLEGAL);
            }
        }

    }

    /**
     * 返回分享的文件列表，包括子文件
     *
     * @param shareId
     * @param fileIdList
     * @return
     */
    private List<AccountFileDAO> checkSharedFileIdOnStatus(Long shareId, List<Long> fileIdList) {
        // 获取分享文件列表的全部文件和子文件内容
        List<AccountFileDAO> sharesFileInfoList = getSharesFileInfo(shareId);

        List<AccountFileDAO> allAccountFileDAOList = new ArrayList<>();

        // 递归获取子全部文件
        accountFileService.findAllAccountFileDAOByRecursion(allAccountFileDAOList, sharesFileInfoList, false);
        if (CollectionUtils.isEmpty(allAccountFileDAOList)) {
            return List.of();
        }

        Set<Long> allFileIdSet = allAccountFileDAOList.stream().map(AccountFileDAO::getId).collect(Collectors.toSet());
        if (!allFileIdSet.containsAll(fileIdList)) {
            log.error("Target ids are not in the shared file list. fileIdList: {}", fileIdList);
            throw new BizException(BizCodeEnum.SHARE_FILE_ILLEGAL);
        }

        return allAccountFileDAOList;
    }


    private List<AccountFileDAO> getSharesFileInfo(Long shareId) {
        // 找分享文件列表
        List<Long> shareFileIdList = shareFileRepository.findAccountFileIdByShareId(shareId);

        // 找文件对象
        return accountFileRepository.findAllById(shareFileIdList);
    }


    private ShareAccountDTO getSharedAccount(Long accountId) {
        if (accountId == null) {
            log.error("account id is null");
            throw new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        AccountDAO accountDAO = accountRepository.findById(accountId).orElse(null);
        if (accountDAO == null) {
            log.error("account not exist, accountId: {}", accountId);
            throw new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return SpringBeanUtil.copyProperties(accountDAO, ShareAccountDTO.class);
    }

    private ShareDAO checkSharesStatus(Long shareId) {
        ShareDAO shareDAO = shareRepository.findById(shareId).orElse(null);
        if (shareDAO == null) {
            log.error("Shares not exist, shareId: {}", shareId);
            throw new BizException(BizCodeEnum.SHARE_NOT_EXIST);
        }
        // 暂时未用，直接物理删除
        if (ShareStatusEnum.CANCELLED.name().equalsIgnoreCase(shareDAO.getShareStatus())) {
            log.error("Shares are cancelled, shareId: {}", shareId);
            throw new BizException(BizCodeEnum.SHARE_CANCELLED);
        }
        // 判断分享是否过期
        if (shareDAO.getShareEndTime().getTime() < System.currentTimeMillis()) {
            log.error("Shared link is expired: sharedId:{}", shareId);
            throw new BizException(BizCodeEnum.SHARE_EXPIRED);
        }

        return shareDAO;
    }
}
