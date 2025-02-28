package com.protectra.sp.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deltaTokens")
public class DeltaTokenEntity {

    // Composite key (siteId:driveId)
    @Id
    private String id;
    private String siteId;
    private String driveId;
    private String deltaToken;
    private LocalDateTime updatedAt;
    private String driveName;
    private String siteName;

    public String getDriveName() {
		return driveName;
	}

	public void setDriveName(String driveName) {
		this.driveName = driveName;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DeltaTokenEntity() {
        // Default constructor for Spring Data
    }

    public DeltaTokenEntity(String siteId, String driveId, String deltaToken) {
        this.siteId = siteId;
        this.driveId = driveId;
        this.deltaToken = deltaToken;
        this.updatedAt = LocalDateTime.now();
        this.id = generateId(siteId, driveId);
    }

    public static String generateId(String siteId, String driveId) {
        return siteId + ":" + driveId;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
        this.id = generateId(siteId, driveId);
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
        this.id = generateId(siteId, driveId);
    }

    public String getDeltaToken() {
        return deltaToken;
    }

    public void setDeltaToken(String deltaToken) {
        this.deltaToken = deltaToken;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DeltaTokenEntity{" +
                "id='" + id + '\'' +
                ", siteId='" + siteId + '\'' +
                ", driveId='" + driveId + '\'' +
                ", deltaToken='" + deltaToken + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
