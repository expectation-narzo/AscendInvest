package com.ascend.invest.handlers;

import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.ascend.invest.databinding.SectionWithdrawBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WithdrawalHandler {

    private final DatabaseReference mDatabase;

    public WithdrawalHandler() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void setupWithdrawListeners(SectionWithdrawBinding binding, String userId) {
        if (binding.withdrawButton == null) return;

        binding.withdrawButton.setOnClickListener(v -> {
            if (binding.withdrawAmount == null || binding.userWithdrawalAddress == null) return;

            String amount = binding.withdrawAmount.getText().toString().trim();
            String userAddress = binding.userWithdrawalAddress.getText().toString().trim();

            if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(userAddress)) {
                Toast.makeText(binding.getRoot().getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            createWithdrawalRequest(userId, amount, userAddress, new WithdrawalCallback() {
                @Override
                public void onSuccess(String requestId) {
                    Toast.makeText(binding.getRoot().getContext(), "Withdrawal request submitted successfully", Toast.LENGTH_SHORT).show();
                    binding.withdrawAmount.setText("");
                    binding.userWithdrawalAddress.setText("");
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(binding.getRoot().getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public void createWithdrawalRequest(String userId, String amount, String userWalletAddress, WithdrawalCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure("User ID is required");
            return;
        }

        if (amount == null || amount.isEmpty()) {
            callback.onFailure("Amount is required");
            return;
        }

        if (userWalletAddress == null || userWalletAddress.isEmpty()) {
            callback.onFailure("User wallet address is required");
            return;
        }

        UserHandler.getInstance().getUserDataFresh(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double unlockedBalance = 0;
                double walletBalance = 0;

                if (snapshot.child("unlocked_balance").exists()) {
                    Object val = snapshot.child("unlocked_balance").getValue();
                    if (val instanceof Number) unlockedBalance = ((Number) val).doubleValue();
                }

                if (snapshot.child("wallet_balance").exists()) {
                    Object val = snapshot.child("wallet_balance").getValue();
                    if (val instanceof Number) walletBalance = ((Number) val).doubleValue();
                }

                double requestAmount;
                try {
                    requestAmount = Double.parseDouble(amount);
                } catch (NumberFormatException e) {
                    callback.onFailure("Invalid amount format");
                    return;
                }

                if (requestAmount < 20) {
                    callback.onFailure("Minimum withdrawal amount is $20.00");
                    return;
                }

                if (requestAmount > unlockedBalance) {
                    callback.onFailure("Insufficient unlocked balance. You can only withdraw profit earnings.");
                    return;
                }

                if (requestAmount > walletBalance) {
                    callback.onFailure("Insufficient wallet balance.");
                    return;
                }

                String requestId = UserHandler.getInstance().getTransactionsRef(userId, "withdraw").push().getKey();
                if (requestId == null) {
                    callback.onFailure("Failed to generate request ID");
                    return;
                }

                long timestamp = System.currentTimeMillis();
                String date = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(new Date(timestamp));

                Map<String, Object> transactionData = new HashMap<>();
                transactionData.put("id", requestId);
                transactionData.put("title", "Withdrawal Request");
                transactionData.put("subtitle", date + " • " + userWalletAddress);
                transactionData.put("amount", "-$" + amount);
                transactionData.put("status", "Pending");
                transactionData.put("type", "withdraw");
                transactionData.put("timestamp", timestamp);
                transactionData.put("userWalletAddress", userWalletAddress);

                Map<String, Object> adminRequestData = new HashMap<>();
                adminRequestData.putAll(transactionData);
                adminRequestData.put("userId", userId);
                adminRequestData.put("userWalletAddress", userWalletAddress);

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("transactions/withdraw/" + requestId, transactionData);
                userUpdates.put("wallet_balance", walletBalance - requestAmount);
                userUpdates.put("unlocked_balance", unlockedBalance - requestAmount);

                mDatabase.child("transactions/withdraw_req/" + userId + "_" + requestId).setValue(adminRequestData);

                UserHandler.getInstance().updateUserData(userId, userUpdates,
                        aVoid -> callback.onSuccess(requestId),
                        e -> callback.onFailure("Failed to create withdrawal request: " + e.getMessage()));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure("Database error: " + error.getMessage());
            }
        });
    }

    public void acceptWithdrawalRequest(String userId, String requestId, WithdrawalStatusCallback callback) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("transactions/withdraw/" + requestId + "/status", "Success");

        mDatabase.child("transactions/withdraw_req/" + userId + "_" + requestId).removeValue();

        UserHandler.getInstance().updateUserData(userId, userUpdates,
                aVoid -> callback.onSuccess("Withdrawal request accepted"),
                e -> callback.onFailure("Failed to accept withdrawal request: " + e.getMessage()));
    }

    public void rejectWithdrawalRequest(String userId, String requestId, WithdrawalStatusCallback callback) {
        UserHandler.getInstance().getUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                double withdrawalAmount = 0;
                DataSnapshot transSnapshot = userSnapshot.child("transactions").child("withdraw").child(requestId);
                if (transSnapshot.exists()) {
                    Object amountObj = transSnapshot.child("amount").getValue();
                    if (amountObj != null) {
                        String amountStr = amountObj.toString().replace("-$", "").replace("$", "");
                        try {
                            withdrawalAmount = Double.parseDouble(amountStr);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }

                double currentBalance = 0;
                double currentUnlocked = 0;

                if (userSnapshot.child("wallet_balance").exists()) {
                    Object val = userSnapshot.child("wallet_balance").getValue();
                    if (val instanceof Number) currentBalance = ((Number) val).doubleValue();
                }

                if (userSnapshot.child("unlocked_balance").exists()) {
                    Object val = userSnapshot.child("unlocked_balance").getValue();
                    if (val instanceof Number) currentUnlocked = ((Number) val).doubleValue();
                }

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("transactions/withdraw/" + requestId + "/status", "Failed");
                userUpdates.put("wallet_balance", currentBalance + withdrawalAmount);
                userUpdates.put("unlocked_balance", currentUnlocked + withdrawalAmount);

                mDatabase.child("transactions/withdraw_req/" + userId + "_" + requestId).removeValue();

                UserHandler.getInstance().updateUserData(userId, userUpdates,
                        aVoid -> callback.onSuccess("Withdrawal request rejected and refunded"),
                        e -> callback.onFailure("Failed to reject withdrawal request: " + e.getMessage()));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure("Database error: " + error.getMessage());
            }
        });
    }

    public interface WithdrawalCallback {
        void onSuccess(String requestId);
        void onFailure(String error);
    }

    public interface WithdrawalStatusCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}
