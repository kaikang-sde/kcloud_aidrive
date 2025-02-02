package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: Kai Kang
 */
@Repository
public interface AccountFileRepository extends JpaRepository<AccountFileDAO, Long> {

    List<AccountFileDAO> findByParentId(Long parentId);

    AccountFileDAO findByAccountIdAndParentId(Long accountId, Long parentId);

    List<AccountFileDAO> findAllByAccountIdAndParentId(Long accountId, Long parentId);

    AccountFileDAO findByIdAndAccountId(Long id, Long accountId);


    Long countByAccountIdAndParentIdAndIsDirAndFileName(Long accountId, Long parentId, Integer isDir, String fileName);


    List<AccountFileDAO> findByAccountIdAndParentIdOrderByIsDirDescEstCreateDesc(Long accountId, Long parentId);

    AccountFileDAO findByFileIdAndAccountId(Long fileId, Long accountId);

    Long countByAccountIdAndParentIdAndFileName(Long accountId, Long parentId, String fileName);

    /**
     * Custom Query: high performance - sends a raw UPDATE query directly to the database:
     *
     * @param id
     * @param fileName
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE account_file SET file_name = :fileName WHERE id = :id", nativeQuery = true)
    int updateFileNameByIdNative(@Param("id") Long id, @Param("fileName") String fileName);


    List<AccountFileDAO> findByAccountIdAndIsDir(Long accountId, Integer isDir);

    @Modifying
    @Transactional
    @Query("UPDATE AccountFileDAO af SET af.parentId = :targetParentId WHERE af.id IN :fileIds")
    int updateParentIdForFileIds(@Param("fileIds") List<Long> fileIds, @Param("targetParentId") Long targetParentId);


    List<AccountFileDAO> findByIdInAndAccountId(List<Long> fileIdList, Long accountId);


    AccountFileDAO findByIdAndIsDirAndAccountId(Long targetParentId, Integer code, Long accountId);
}
