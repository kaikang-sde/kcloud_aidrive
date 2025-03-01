package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import com.kang.kcloud_aidrive.model.AccountFileDO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Kai Kang
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

    @Modifying
    @Transactional
    @Query("UPDATE AccountFileDAO af SET af.del = true, af.delTime = CURRENT_TIMESTAMP WHERE af.id IN :ids")
    void softDeleteAllByIdInBatch(@Param("ids") List<Long> ids);

    /**
     * Condition Handling:
     * •	It filters by accountId and del = 1 (only deleted files).
     * •	If fileIdList is not null, it applies the IN condition.
     * •	If fileIdList is null, it ignores the condition.
     * •	Overall, this query returns a list of deleted files for the specified account.
     *
     * @param accountId
     * @param fileIdList
     * @return
     */
    @Query(value = "SELECT * FROM account_file WHERE account_id = :accountId " +
            "AND del = 1 " +
            "AND (:fileIdList IS NULL OR id IN (:fileIdList))",
            nativeQuery = true)
    List<AccountFileDAO> findRecycleFilesByAccountId(
            @Param("accountId") Long accountId,
            @Param("fileIdList") List<Long> fileIdList);


    @Query(value = "SELECT * FROM account_file WHERE parent_id = :parentId AND del = 1 AND account_id = :accountId",
            nativeQuery = true)
    List<AccountFileDAO> selectRecycleChildFiles(@Param("parentId") Long parentId, @Param("accountId") Long accountId);


    @Modifying
    @Query(value = "DELETE FROM account_file WHERE del = 1 AND id IN (:recycleFileIds)", nativeQuery = true)
    void deleteRecycleFiles(List<Long> recycleFileIds);

    // 更新回收站
    @Modifying
    @Query(value = "UPDATE account_file SET file_name = :fileName WHERE id = :id AND del = 1", nativeQuery = true)
    Boolean updateRecycleFileNameByIdNative(@Param("id") Long id, @Param("fileName") String fileName);

    @Modifying
    @Query(value = "UPDATE account_file SET del = 0 WHERE id IN (:allFileIds)", nativeQuery = true)
    void restoreFilesByIds(@Param("allFileIds") List<Long> allFileIds);

    @Query(value = """
            SELECT * FROM account_file 
            WHERE account_id = :accountId 
            AND del = 1 
            AND (:fileIdList IS NULL OR FIND_IN_SET(id, :fileIdList) > 0)
            """, nativeQuery = true)
    List<AccountFileDAO> findRecycleFilesByAccountIdString(
            @Param("accountId") Long accountId,
            @Param("fileIdList") String fileIdList);

    @Query(value = "SELECT * FROM account_file " +
            "WHERE account_id = :accountId " +
            "AND file_name LIKE %:search% " +
            "ORDER BY is_dir DESC, est_create DESC " +
            "LIMIT 30", nativeQuery = true)
    List<AccountFileDAO> findFilesByAccountIdAndFileNameOrderByIsDirAndEstCreateNative(
            @Param("accountId") Long accountId,
            @Param("search") String search);

    List<AccountFileDAO> findByAccountIdAndIsDirAndIdIn(Long accountId, Integer code, List<Long> fileIds);

    @Query("SELECT f.objectKey FROM FileDAO f WHERE f.id = :fileId")
    String findObjectKeyById(Long fileId);
}
