package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.StorageDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<StorageDAO, Long> {

}
