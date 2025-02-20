package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.StorageDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * @author Kai Kang
 */
@Repository
public interface StorageRepository extends JpaRepository<StorageDAO, Long> {
    StorageDAO findByAccountId(Long accountId);

    @Modifying
    @Query("UPDATE StorageDAO s SET s.usedSize = s.usedSize + :fileSize WHERE s.accountId = :accountId")
    void updateUsedSizeByAccountId(@Param("accountId") Long accountId, @Param("fileSize") Long fileSize);

    @Modifying
    @Query("UPDATE StorageDAO s SET s.usedSize = s.usedSize + :fileSize WHERE s.id = :id")
    void updateUsedSizeById(@Param("id") Long id, @Param("fileSize") Long fileSize);

}
