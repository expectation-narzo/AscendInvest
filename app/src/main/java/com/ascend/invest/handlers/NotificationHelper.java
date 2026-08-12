package com.ascend.invest.handlers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ascend.invest.MainActivity;
import com.ascend.invest.R;

public class NotificationHelper {
    public static final String CHANNEL_BRANDING = "branding_channel";
    public static final String CHANNEL_PROFIT = "profit_channel";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel brandingChannel = new NotificationChannel(
                    CHANNEL_BRANDING,
                    "App Branding",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            brandingChannel.setDescription("Occasional updates and tips from Ascend Invest");

            NotificationChannel profitChannel = new NotificationChannel(
                    CHANNEL_PROFIT,
                    "Profit Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            profitChannel.setDescription("Get notified when your investment profits are ready to claim");
            profitChannel.enableVibration(true);
            profitChannel.setVibrationPattern(new long[]{0, 250, 250, 250});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(brandingChannel);
                manager.createNotificationChannel(profitChannel);
            }
        }
    }

    public static void showNotification(Context context, String channelId, int notificationId, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_logo_without_bg)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // Handle lack of POST_NOTIFICATIONS permission
        }
    }
}
