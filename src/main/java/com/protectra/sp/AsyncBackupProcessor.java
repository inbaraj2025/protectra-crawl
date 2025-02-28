package com.protectra.sp;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.protectra.sp.entity.Asset;

@Service
public class AsyncBackupProcessor {

    @Async
    public void processAssetBackup(Asset asset) {
        // Processing logic for the asset backup asynchronously
    }
}
