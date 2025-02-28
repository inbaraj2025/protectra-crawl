package com.protectra.sp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "backup")
public class Backup {

    @Id
    private String id; // MongoDB will auto-generate this ID
    private String fileId; // ID of the file being backed up
    private String status; // Status of the backup for this file (BACKED_UP, FAILED)
    private String batchId; // The associated BackupBatch ID

    // Constructors, getters, and setters
    public Backup() {}

    public Backup(String fileId, String status, String batchId) {
        this.fileId = fileId;
        this.status = status;
        this.batchId = batchId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
