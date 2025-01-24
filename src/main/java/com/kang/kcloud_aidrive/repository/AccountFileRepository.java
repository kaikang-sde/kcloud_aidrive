package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.AccountFileDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountFileRepository extends JpaRepository<AccountFileDAO, Long> {
    AccountFileDAO findByAccountIdAndParentId(Long accountId, Long parentId);
}
