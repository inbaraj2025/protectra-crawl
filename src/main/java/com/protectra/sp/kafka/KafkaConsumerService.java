package com.protectra.sp.kafka;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.protectra.sp.GraphClientConfig;
import com.protectra.sp.SPSiteCrawlerService;
import com.protectra.sp.entity.Asset;
import com.protectra.sp.entity.BackupBatch;
import com.protectra.sp.repo.AssetRepository;
import com.protectra.sp.repo.BackupBatchRepository;

@Service
public class KafkaConsumerService {

    @Autowired
    private BackupBatchRepository backupBatchRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SPSiteCrawlerService spSiteCrawlerService;

    @KafkaListener(topics = "backup-batch", groupId = "backup-group")
    public void consume(String batchId) {
        // Fetch the BackupBatch from MongoDB using the batchId
        BackupBatch batch = backupBatchRepository.findById(batchId).orElse(null);

        if (batch != null) {
            // Update the status to SCANNING in MongoDB
            batch.setStatus("SCANNING");
            backupBatchRepository.save(batch);

            // Fetch the Asset (SharePoint Site) associated with this batch
            Asset asset = assetRepository.findById(batch.getAssetId()).orElse(null);

            if (asset != null) {
                // Retrieve the delta token for incremental backup
                String deltaToken = asset.getDeltaToken();

                // Fetch SharePoint files for this site using delta API
                spSiteCrawlerService.fetchSharePointFiles(asset.getSiteId());

                // Save the updated delta token in the Asset table for future use
             
           //     assetRepository.save(asset);

             

                // Send a message to Kafka for file processing completion
                kafkaTemplate.send("file-processing", batch.getId());
            } else {
                // If the Asset is not found, update the batch status to FAILED
                batch.setStatus("FAILED");
                backupBatchRepository.save(batch);
                System.out.println("Asset not found for batch: " + batch.getId());
            }
        } else {
            System.out.println("Backup batch not found: " + batchId);
        }
    }
}
