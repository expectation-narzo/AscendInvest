package com.ascend.invest;

import android.app.Application;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ascend.invest.handlers.BrandingWorker;
import com.ascend.invest.handlers.NotificationHelper;
import com.ascend.invest.handlers.ProfitCheckWorker;
import com.ascend.invest.handlers.P2POrderWorker;
import com.ascend.invest.handlers.P2PNotificationService;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class AscendInvestApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Disable Firebase Offline Persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        NotificationHelper.createNotificationChannels(this);
        scheduleWorkers();
        startP2PService();
    }

    private void startP2PService() {
        Intent intent = new Intent(this, P2PNotificationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void scheduleWorkers() {
        WorkManager workManager = WorkManager.getInstance(this);
        // Schedule Branding Worker - every 6 hours
        PeriodicWorkRequest brandingRequest = new PeriodicWorkRequest.Builder(
                BrandingWorker.class, 6, TimeUnit.HOURS)
                .build();
        workManager.enqueueUniquePeriodicWork(
                "BrandingWork",
                ExistingPeriodicWorkPolicy.KEEP,
                brandingRequest
        );

        // Schedule Profit Check Worker - every 4 hours
        PeriodicWorkRequest profitCheckRequest = new PeriodicWorkRequest.Builder(
                ProfitCheckWorker.class, 4, TimeUnit.HOURS)
                .build();

        workManager.enqueueUniquePeriodicWork(
                "ProfitCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                profitCheckRequest
        );

        // Schedule P2P Order Check - every 15 minutes (Minimum allowed by WorkManager)
        PeriodicWorkRequest p2pRequest = new PeriodicWorkRequest.Builder(
                P2POrderWorker.class, 15, TimeUnit.MINUTES)
                .addTag("P2P_WORK")
                .build();

        workManager.enqueueUniquePeriodicWork(
                "P2POrderWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                p2pRequest
        );
    }
}
