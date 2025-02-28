package com.protectra.sp.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.protectra.sp.entity.BackupBatch;

public interface BackupBatchRepository extends MongoRepository<BackupBatch, String> {
    // You can add custom queries here if needed, such as:
    // List<BackupBatch> findByStatus(String status);
}
