package com.protectra.sp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "asset")
public class Asset {

    @Id
    private String id;
    private String siteId;
    private String siteName;
    private String deltaToken; // New field to store the delta token

    // Constructors, getters, and setters
    public Asset() {}

    public Asset(String siteId, String siteName, String deltaToken) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.deltaToken = deltaToken;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getDeltaToken() {
        return deltaToken;
    }

    public void setDeltaToken(String deltaToken) {
        this.deltaToken = deltaToken;
    }
}
