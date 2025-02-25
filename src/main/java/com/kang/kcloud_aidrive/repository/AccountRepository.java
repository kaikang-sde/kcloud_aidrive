package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.AccountDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Kai Kang
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountDAO, Long> {
    List<AccountDAO> findByPhone(String phone);

    AccountDAO findByPhoneAndPassword(String phone, String password);

}
