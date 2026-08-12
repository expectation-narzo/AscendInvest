package com.ascend.invest.handlers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Random;

public class BrandingWorker extends Worker {
    public BrandingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String[] brandingMessages = {
                "Invest in your future with Ascend Invest.",
                "Check out today's top performing plans!",
                "Smart investing made simple. Keep growing with us.",
                "Your portfolio is waiting for you. Let's reach new heights.",
                "Financial freedom starts with one small step. Start today!"
        };

        String message = brandingMessages[new Random().nextInt(brandingMessages.length)];
        NotificationHelper.showNotification(
                getApplicationContext(),
                NotificationHelper.CHANNEL_BRANDING,
                1001,
                "Ascend Invest",
                message
        );

        return Result.success();
    }
}
