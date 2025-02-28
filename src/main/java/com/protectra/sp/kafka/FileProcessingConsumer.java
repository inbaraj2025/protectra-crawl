package com.protectra.sp.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.protectra.sp.entity.Backup;
import com.protectra.sp.entity.BackupBatch;
import com.protectra.sp.repo.BackupBatchRepository;
import com.protectra.sp.repo.BackupRepository;

@Service
public class FileProcessingConsumer {

    @Autowired
    private BackupBatchRepository backupBatchRepository;

    @Autowired
    private BackupRepository backupRepository;

    @KafkaListener(topics = "backup-file", groupId = "file-processing-group")
    public void processFile(String fileId) {
        // Simulate processing of the file
        Backup backup = new Backup();
        backup.setFileId(fileId);
        backup.setStatus("BACKED_UP");  // Example status for file

        // Save the backup entry for the file
        backupRepository.save(backup);

        // Log the file backup
        System.out.println("Backing up file with ID: " + fileId);
    }

    @KafkaListener(topics = "file-processing", groupId = "file-processing-group")
    public void finalizeBatch(String batchId) {
        // Fetch the BackupBatch from MongoDB
        BackupBatch batch = backupBatchRepository.findById(batchId).orElse(null);

        if (batch != null) {
            // Mark the BackupBatch as COMPLETED after all files are processed
            batch.setStatus("COMPLETED");
            backupBatchRepository.save(batch);
            System.out.println("Backup completed for batch: " + batch.getId());
        }
    }
}
