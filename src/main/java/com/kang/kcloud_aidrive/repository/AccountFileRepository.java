package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountFileRepository extends JpaRepository<AccountFileDAO, Long> {
    AccountFileDAO findByAccountIdAndParentId(Long accountId, Long parentId);

    AccountFileDAO findByIdAndAccountId(Long id, Long accountId);


    int countByAccountIdAndParentIdAndIsDirAndFileName(Long accountId, Long parentId, Integer isDir, String fileName);

    List<AccountFileDAO> findByAccountIdAndParentIdOrderByIsDirDescEstCreateDesc(Long accountId, Long parentId);

    AccountFileDAO findByFileIdAndAccountId(Long fileId, Long accountId);

    int countByAccountIdAndParentIdAndFileName(Long accountId, Long parentId, String fileName);

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

}
