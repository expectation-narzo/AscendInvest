package com.ascend.invest.handlers;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class ReferralManager {

    private DatabaseReference mDatabase;
    private static final int MAX_LEVELS = 20;
    private static final String TAG = "ReferralSystem";

    public ReferralManager() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void distributeCommissions(String userId, double depositAmount) {
        Log.d(TAG, "Starting commission distribution for user: " + userId + " amount: " + depositAmount);
        findReferrerAndDistribute(userId, depositAmount, 1, userId);
    }

    private void findReferrerAndDistribute(String currentUserId, double depositAmount, int level, String originalDepositorId) {
        if (level > MAX_LEVELS) {
            Log.d(TAG, "Reached max levels (20). Stopping.");
            return;
        }

        UserHandler.getInstance().getUserData(currentUserId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String referrerUid = snapshot.child("referredBy").getValue(String.class);
                    Log.d(TAG, "Level " + level + ": Referrer found for " + currentUserId + " is " + referrerUid);
                    
                    if (referrerUid != null && !referrerUid.isEmpty()) {
                        final int currentLevel = level;
                        mDatabase.child("level/refer").child("level" + currentLevel).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot levelSnap) {
                                double commissionPercentage = 0.0;
                                if (levelSnap.exists()) {
                                    Object val = levelSnap.getValue();
                                    if (val instanceof Number) commissionPercentage = ((Number) val).doubleValue();
                                } else {
                                    commissionPercentage = getDefaultCommissionPercentage(currentLevel);
                                }

                                double commissionAmount = (depositAmount * commissionPercentage) / 100.0;

                                Log.d(TAG, "Calculating " + commissionPercentage + "% of " + depositAmount + " = " + commissionAmount);
                                if (commissionAmount > 0) addCommissionToUser(referrerUid, commissionAmount, currentLevel, originalDepositorId);

                                // Continue to the next level
                                findReferrerAndDistribute(referrerUid, depositAmount, currentLevel + 1, originalDepositorId);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                } else {
                    Log.d(TAG, "No referrer found for " + currentUserId + " at level " + level + ". Chain ends.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private double getDefaultCommissionPercentage(int level) {
        if (level == 1) return 10.0;
        if (level == 2) return 7.0;
        if (level == 3) return 5.0;
        if (level == 4) return 3.0;
        if (level >= 5 && level <= 10) return 2.0;
        if (level >= 11 && level <= 20) return 1.0;
        return 0.0;
    }

    private void addCommissionToUser(String uid, double amount, int level, String fromUserId) {
        UserHandler.getInstance().getUserData(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double curRefBal = 0, curWalBal = 0, curTotProf = 0, curUnlBal = 0;
                
                if (snapshot.child("referral_balance").exists()) curRefBal = getDouble(snapshot.child("referral_balance").getValue());
                if (snapshot.child("wallet_balance").exists()) curWalBal = getDouble(snapshot.child("wallet_balance").getValue());
                if (snapshot.child("total_profit").exists()) curTotProf = getDouble(snapshot.child("total_profit").getValue());
                if (snapshot.child("unlocked_balance").exists()) curUnlBal = getDouble(snapshot.child("unlocked_balance").getValue());

                Map<String, Object> updates = new HashMap<>();
                updates.put("referral_balance", curRefBal + amount);
                updates.put("wallet_balance", curWalBal + amount);
                updates.put("total_profit", curTotProf + amount);
                updates.put("unlocked_balance", curUnlBal + amount);
                
                String historyId = UserHandler.getInstance().getUserRef(uid).child("referral_history").push().getKey();
                Map<String, Object> historyData = new HashMap<>();
                historyData.put("amount", amount);
                historyData.put("level", level);
                historyData.put("fromUser", fromUserId);
                historyData.put("timestamp", System.currentTimeMillis());
                
                if (historyId != null) {
                    updates.put("referral_history/" + historyId, historyData);
                }

                // Add to profit transactions for charting
                String profitTxId = UserHandler.getInstance().getTransactionsRef(uid, "profit").push().getKey();
                if (profitTxId != null) {
                    Map<String, Object> profitTxData = new HashMap<>();
                    profitTxData.put("id", profitTxId);
                    profitTxData.put("title", "Referral Commission (Lvl " + level + ")");
                    profitTxData.put("amount", "+$" + String.format(java.util.Locale.US, "%.2f", amount));
                    profitTxData.put("status", "Success");
                    profitTxData.put("type", "profit");
                    profitTxData.put("timestamp", System.currentTimeMillis());
                    updates.put("transactions/profit/" + profitTxId, profitTxData);
                }
                
                UserHandler.getInstance().updateUserData(uid, updates, 
                        aVoid -> Log.d(TAG, "Successfully credited $" + amount + " to referrer " + uid), 
                        e -> Log.e(TAG, "Failed to credit commission: " + e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private double getDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) return Double.parseDouble((String) val);
        return 0.0;
    }
    
    public void setReferrer(String userId, String referralCode, ReferralSetCallback callback) {
        UserHandler.getInstance().getUserByReferralCode(referralCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String referrerUid = snapshot.getValue(String.class);
                    if (referrerUid != null && !referrerUid.equals(userId)) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("referredBy", referrerUid);
                        UserHandler.getInstance().updateUserData(userId, updates, 
                                aVoid -> callback.onSuccess(), 
                                e -> callback.onFailure(e.getMessage()));
                    } else {
                        callback.onFailure("Invalid referral code");
                    }
                } else {
                    callback.onFailure("Referral code does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }
    
    public interface ReferralSetCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
