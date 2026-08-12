package com.ascend.invest.handlers;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ascend.invest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class P2PNotificationService extends Service {
    private DatabaseReference mDatabase;
    private ValueEventListener mListener;
    private String mUserId;
    private static final int SERVICE_ID = 1005;

    @Override
    public void onCreate() {
        super.onCreate();
        
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserId = FirebaseAuth.getInstance().getUid();
        
        Notification notification = createServiceNotification();
        startForeground(SERVICE_ID, notification);

        if (mUserId != null) {
            setupListener();
        }
    }

    private Notification createServiceNotification() {
        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_BRANDING)
                .setContentTitle("P2P Trade Monitor")
                .setContentText("Listening for trade requests...")
                .setSmallIcon(R.drawable.ic_logo_without_bg)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    private void setupListener() {
        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        if (key == null) continue;

                        final String orderId = key.contains("_") ? key.substring(0, key.lastIndexOf("_")) : key;
                        
                        // Check ongoing first, then history
                        mDatabase.child("ongoing_p2p_orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot orderSnap) {
                                if (orderSnap.exists()) {
                                    handleOrderNotification(orderSnap, key);
                                } else {
                                    mDatabase.child("p2p_order_history").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot histSnap) {
                                            handleOrderNotification(histSnap, key);
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                                }
                                mDatabase.child("p2p_notifications").child(mUserId).child(key).removeValue();
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("p2p_notifications").child(mUserId).addValueEventListener(mListener);
    }

    private void handleOrderNotification(DataSnapshot orderSnap, String key) {
        P2POrder order = orderSnap.getValue(P2POrder.class);
        if (order == null) return;

        int notificationId = Math.abs(order.id.hashCode());

        if (key.endsWith("_paid")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "Payment Received", order.buyerName + " has marked the trade as PAID. Please verify and release.");
        } else if (key.endsWith("_accepted")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "Trade Accepted", order.sellerName + " has accepted your trade. You can now make the payment.");
        } else if (key.endsWith("_released")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "USDT Released", order.sellerName + " has released " + order.amount + " USDT to your wallet.");
        } else if (key.endsWith("_disputed")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "Trade Disputed", "A dispute has been raised for trade #" + order.id.substring(0, 5) + ". Assets are locked.");
        } else if (key.endsWith("_missing")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "Payment Not Received", "The seller claims they haven't received your payment for trade #" + order.id.substring(0, 5));
        } else if (key.endsWith("_declined")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "Trade Declined", "The seller " + order.sellerName + " has declined your trade request.");
        } else if (key.endsWith("_cancelled")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "Trade Cancelled", "The buyer has cancelled the trade for " + order.amount + " USDT.");
        } else if (key.endsWith("_waiting")) {
            NotificationHelper.showNotification(getApplicationContext(), NotificationHelper.CHANNEL_PROFIT, notificationId,
                    "P2P Trade Waiting", order.buyerName + " is waiting for you to accept a trade for " + order.amount + " USDT");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String newUid = FirebaseAuth.getInstance().getUid();
        if (newUid != null) {
            if (!newUid.equals(mUserId)) {
                if (mUserId != null && mListener != null) {
                    mDatabase.child("p2p_notifications").child(mUserId).removeEventListener(mListener);
                }
                mUserId = newUid;
                setupListener();
            } else if (mListener == null) {
                // Same UID but listener wasn't set up yet
                setupListener();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mUserId != null && mListener != null) {
            mDatabase.child("p2p_notifications").child(mUserId).removeEventListener(mListener);
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // When the user swipes away the app from recent tasks
        if (mUserId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mUserId);
            userRef.child("status").setValue("offline");
            userRef.child("lastSeen").setValue(com.google.firebase.database.ServerValue.TIMESTAMP);
        }
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
