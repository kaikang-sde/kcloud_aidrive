package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.AccountFileDAOWithoutAutoGenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kai Kang
 * More details within AccountFileDAOWithoutAutoGenId entity class
 */
@Repository
public interface AccountFileDAOWithoutAutoGenIdRepository extends JpaRepository<AccountFileDAOWithoutAutoGenId, Long> {
}
