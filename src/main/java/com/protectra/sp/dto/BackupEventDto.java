package com.protectra.sp.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class BackupEventDto {

    @NotBlank(message = "Device UUID is mandatory")
    private String deviceUUID;

    private Boolean isFullBackup = false;

    @NotBlank(message = "Host Name is mandatory")
    private String hostName;
    
    private String errorCode;
}