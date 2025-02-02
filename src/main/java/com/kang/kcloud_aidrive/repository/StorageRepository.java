package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.StorageDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Author: Kai Kang
 */
@Repository
public interface StorageRepository extends JpaRepository<StorageDAO, Long> {
    StorageDAO findByAccountId(Long accountId);

    @Modifying
    @Query("UPDATE StorageDAO s SET s.usedSize = s.usedSize + :fileSize WHERE s.accountId = :accountId")
    void updateUsedSizeByAccountId(@Param("accountId") Long accountId, @Param("fileSize") Long fileSize);

}
