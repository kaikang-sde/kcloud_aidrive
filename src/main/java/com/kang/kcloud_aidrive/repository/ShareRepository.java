package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.ShareDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Kai Kang
 */
@Repository
public interface ShareRepository extends JpaRepository<ShareDAO, Long> {
    List<ShareDAO> findByAccountIdOrderByEstCreateDesc(Long accountId);

    // Equivalent to: SELECT * FROM share WHERE id IN (...) AND account_id = ...;
    List<ShareDAO> findByIdInAndAccountId(List<Long> fileIdList, Long accountId);

    ShareDAO findByIdAndShareCodeAndShareStatus(Long shareId, String shareCode, String name);
}

