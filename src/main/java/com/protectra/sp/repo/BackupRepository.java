package com.protectra.sp.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.protectra.sp.entity.Backup;

public interface BackupRepository extends MongoRepository<Backup, String> {
    // Additional custom query methods can be added here if needed
}
