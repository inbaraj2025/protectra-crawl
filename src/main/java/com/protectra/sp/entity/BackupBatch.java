package com.protectra.sp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "backupbatch")
public class BackupBatch {

    @Id
    private String id;
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String assetId;
    private String status;
    private List<String> fileIds; // Optional: You may store file IDs in the batch or use them separately
    private String assetInfo; // Example field for asset information (optional)

    // Constructors, getters, and setters
    public BackupBatch(String assetId, String status, List<String> fileIds) {
        this.assetId = assetId;
        this.status = status;
        this.fileIds = fileIds;
    }

    public BackupBatch() {}

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public String getAssetInfo() {
        return assetInfo;
    }

    public void setAssetInfo(String assetInfo) {
        this.assetInfo = assetInfo;
    }
}
