package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.FileDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: Kai Kang
 */
@Repository
public interface FileRepository extends JpaRepository<FileDAO, Long> {
    FileDAO findByIdentifier(String identifier);
}
