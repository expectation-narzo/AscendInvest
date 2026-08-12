package com.ascend.invest.handlers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

public class ProfitCheckWorker extends Worker {
    public ProfitCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return Result.success();

        try {
            DataSnapshot snapshot = Tasks.await(UserHandler.getInstance().getUserRef(userId).get());
            if (snapshot.exists()) {
                DataSnapshot activePlans = snapshot.child("active_plans");
                long currentTime = System.currentTimeMillis();
                long oneDayMillis = 24L * 60L * 60L * 1000L;
                int claimableCount = 0;

                for (DataSnapshot planSnap : activePlans.getChildren()) {
                    Long lastClaim = planSnap.child("lastProfitClaim").getValue(Long.class);
                    if (lastClaim != null && currentTime - lastClaim >= oneDayMillis) {
                        claimableCount++;
                    }
                }

                if (claimableCount > 0) {
                    NotificationHelper.showNotification(
                            getApplicationContext(),
                            NotificationHelper.CHANNEL_PROFIT,
                            2001,
                            "Profit Ready!",
                            "You have " + claimableCount + " plan(s) with profit ready to be claimed!"
                    );
                }
            }
        } catch (Exception e) {
            return Result.retry();
        }

        return Result.success();
    }
}
