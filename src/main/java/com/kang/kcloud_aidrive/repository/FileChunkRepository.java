package com.kang.kcloud_aidrive.repository;

import com.kang.kcloud_aidrive.entity.FileChunkDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kai Kang
 */
@Repository
public interface FileChunkRepository extends JpaRepository<FileChunkDAO, Long> {
    FileChunkDAO findByAccountIdAndIdentifier(Long accountId, String identifier);
}
