package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.ShareFileDAO;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Kai Kang
 */
@Repository
public interface ShareFileRepository extends JpaRepository<ShareFileDAO, Long> {
    void deleteByShareIdIn(List<Long> shareIds);

    @Query("SELECT s.accountFileId FROM ShareFileDAO s WHERE s.shareId = :shareId")
    List<Long> findAccountFileIdByShareId(@Param("shareId") Long shareId);

}
