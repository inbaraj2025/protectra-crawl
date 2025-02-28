package com.protectra.sp;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.protectra.sp.entity.Asset;
import com.protectra.sp.entity.BackupBatch;
import com.protectra.sp.kafka.KafkaProducerService;
import com.protectra.sp.repo.AssetRepository;
import com.protectra.sp.repo.BackupBatchRepository;
import com.safestack.backup.BackupEventDto;
import com.safestack.exception.SafeStackException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class SharePointQueueService {

    private static final Logger logger = LoggerFactory.getLogger(SharePointQueueService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    
    @Autowired
    private BackupBatchRepository backupBatchRepository;
    
    @Autowired
    private AssetRepository assetRepository; // MongoDB repository to fetch assets

    @Autowired
    private KafkaProducerService kafkaProducerService; // Kafka producer to send messages
    

    // Start the task after the Spring Bean has been initialized
    @PostConstruct
    public void startScheduledTask() {
    	  System.out.println("SharePointQueueService Scheduling the task..&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&.");
        // Schedule the SharePoint site job to run immediately and then every 5 minutes
    	logger.debug("************************************************************************");
        scheduler.scheduleAtFixedRate(
                this::crawlAndSaveSharePointSites,
                0,  // Initial delay: 0 means it runs immediately
                2,  // Repeat every 1minutes
                TimeUnit.MINUTES
        );
    }

    

    // Method to crawl and process SharePoint assets from MongoDB
    public void crawlAndSaveSharePointSites() {
        // Fetch all assets from MongoDB
        List<Asset> assets = assetRepository.findAll();

        // Process each asset concurrently using parallel streams
        assets.parallelStream().forEach(asset -> {
            try {
                // Process each asset to create a BackupBatch
            	
            	if(asset.getSiteName().equalsIgnoreCase("site1")) {
            		
            		

                    try {
                        BackupEventDto backupEventDto = new BackupEventDto();
                        backupEventDto.setDeviceUUID(asset.getDeviceUUID());
                       // backupEventDto.setHostName(hostName);
                        backupEventDto.setIsFullBackup(false);
                        backupService.saveBackupProcess(backupEventDto);
                        log.info("Backup initiated successfully for device UUID: {}", asset.getDeviceUUID());
                    } catch (SafeStackException e) {
                        log.error("Failed to process backup for device UUID: {}. Reason: {}", 
                                  asset.getDeviceUUID(), e.getMessage(), e);
                    } catch (Exception e) {
                        log.error("Unexpected error during backup for device UUID: {}", 
                        		asset.getDeviceUUID(), e);
                    }
                
            		
            		
                BackupBatch batch = new BackupBatch();
                batch.setAssetId(asset.getId());
                batch.setStatus("QUEUED"); // Set the status of the batch to QUEUED
                batch.setFileIds(Collections.emptyList()); // Placeholder for file IDs

                // Save the backup batch to MongoDB
                BackupBatch savedBackupBatch = backupBatchRepository.save(batch);
                logger.info("Created BackupBatch for Asset: {} (Batch ID: {})", asset.getSiteName(), savedBackupBatch.getId());

                // Send the batch ID to Kafka for further processing
                kafkaProducerService.sendMessage("backup-batch", savedBackupBatch.getId());

                // Log the progress of the backup batch
                logger.info("Processing Asset: {} (Batch ID: {})", asset.getSiteName(), savedBackupBatch.getId());
            	}
            } catch (Exception e) {
                // Log any exceptions encountered during processing
                logger.error("Error processing asset: {}, error: {}", asset.getSiteName(), e.getMessage());
            }
        });
    }
    
    @PreDestroy
    public void stopScheduledTask() {
        scheduler.shutdown();
    }
}
