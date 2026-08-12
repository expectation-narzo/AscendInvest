package com.ascend.invest.handlers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class P2POrderWorker extends Worker {
    public P2POrderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return Result.success();

        try {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            // Check the dedicated notifications node for this user
            DataSnapshot notificationSnap = Tasks.await(mDatabase.child("p2p_notifications").child(userId).get());
            
            if (notificationSnap.exists()) {
                Map<String, Object> cleanupUpdates = new HashMap<>();
                
                for (DataSnapshot ds : notificationSnap.getChildren()) {
                    String key = ds.getKey();
                    if (key == null) continue;

                    // The key is either orderId or orderId_paid, orderId_accepted, orderId_released, orderId_disputed, orderId_declined
                    String orderId = key.replace("_paid", "").replace("_accepted", "").replace("_released", "").replace("_disputed", "").replace("_declined", "");
                    DataSnapshot orderSnap = Tasks.await(mDatabase.child("p2p_orders").child(orderId).get());
                    P2POrder order = orderSnap.getValue(P2POrder.class);

                    if (order != null) {
                        if (key.endsWith("_paid")) {
                            if ("PAID".equals(order.status)) {
                                NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        NotificationHelper.CHANNEL_PROFIT,
                                        order.id.hashCode(),
                                        "Payment Received",
                                        order.buyerName + " has marked the trade as PAID. Please verify and release."
                                );
                            }
                        } else if (key.endsWith("_accepted")) {
                            if ("PENDING".equals(order.status)) {
                                NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        NotificationHelper.CHANNEL_PROFIT,
                                        order.id.hashCode(),
                                        "Trade Accepted",
                                        order.sellerName + " has accepted your trade. You can now make the payment."
                                );
                            }
                        } else if (key.endsWith("_released")) {
                            if ("COMPLETED".equals(order.status)) {
                                NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        NotificationHelper.CHANNEL_PROFIT,
                                        order.id.hashCode(),
                                        "USDT Released",
                                        order.sellerName + " has released " + order.amount + " USDT to your wallet."
                                );
                            }
                        } else if (key.endsWith("_disputed")) {
                            if ("DISPUTED".equals(order.status)) {
                                NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        NotificationHelper.CHANNEL_PROFIT,
                                        order.id.hashCode(),
                                        "Trade Disputed",
                                        "The seller claims they haven't received payment. Please file a complaint."
                                );
                            }
                        } else if (key.endsWith("_declined")) {
                            if ("CANCELLED".equals(order.status)) {
                                NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        NotificationHelper.CHANNEL_PROFIT,
                                        order.id.hashCode(),
                                        "Trade Declined",
                                        "The seller " + order.sellerName + " has declined your trade request."
                                );
                            }
                        } else {
                            if ("WAITING_FOR_SELLER".equals(order.status)) {
                                NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        NotificationHelper.CHANNEL_PROFIT,
                                        order.id.hashCode(),
                                        "P2P Trade Waiting",
                                        order.buyerName + " is waiting for you to accept a trade for " + order.amount + " USDT"
                                );
                            }
                        }
                    }
                    // Remove the notification flag after processing
                    cleanupUpdates.put("p2p_notifications/" + userId + "/" + key, null);
                }
                
                if (!cleanupUpdates.isEmpty()) {
                    Tasks.await(mDatabase.updateChildren(cleanupUpdates));
                }
            }
        } catch (Exception e) {
            Log.e("P2POrderWorker", "Error checking orders", e);
            return Result.retry();
        }

        return Result.success();
    }
}
