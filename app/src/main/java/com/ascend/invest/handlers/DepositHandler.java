package com.ascend.invest.handlers;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.ascend.invest.R;
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

public class DepositHandler {

    private final DatabaseReference mDatabase;

    public DepositHandler() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void setupDepositListeners(View root, String userId, String adminAddress) {
        View depositButton = root.findViewById(R.id.deposit_button);
        if (depositButton == null) return;

        depositButton.setOnClickListener(v -> {
            EditText etAmount = root.findViewById(R.id.deposit_amount);
            EditText etAddress = root.findViewById(R.id.user_wallet_address);
            EditText etTxId = root.findViewById(R.id.transaction_id);
            
            if (etAmount == null || etAddress == null || etTxId == null) return;

            String amount = etAmount.getText().toString().trim();
            String userAddress = etAddress.getText().toString().trim();
            String txId = etTxId.getText().toString().trim();

            if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(txId)) {
                Toast.makeText(root.getContext(), "Amount and Transaction ID are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if transaction ID has already been used (using query for safety against illegal key characters)
            mDatabase.child("used_transaction_ids").orderByValue().equalTo(txId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(root.getContext(), "This Transaction ID has already been used. Please provide a unique hash.", Toast.LENGTH_LONG).show();
                    } else {
                        createDepositRequest(userId, amount, userAddress, txId, adminAddress, new DepositCallback() {
                            @Override
                            public void onSuccess(String requestId) {
                                // Mark ID as used with a random key to avoid Firebase key restrictions on hashes
                                mDatabase.child("used_transaction_ids").push().setValue(txId);
                                
                                Toast.makeText(root.getContext(), "Deposit request submitted successfully", Toast.LENGTH_SHORT).show();
                                etAmount.setText("");
                                etAddress.setText("");
                                etTxId.setText("");
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(root.getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }

    public void createDepositRequest(String userId, String amount, String userWalletAddress, String transactionId, String adminWalletAddress, DepositCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure("User ID is required");
            return;
        }

        if (amount == null || amount.isEmpty()) {
            callback.onFailure("Amount is required");
            return;
        }

        try {
            double depAmt = Double.parseDouble(amount);
            if (depAmt < 10) {
                callback.onFailure("Minimum deposit amount is $10.00");
                return;
            }
        } catch (NumberFormatException e) {
            callback.onFailure("Invalid amount format");
            return;
        }

        // Wallet address is now optional, but transactionId is not.
        String displayAddress = (userWalletAddress == null || userWalletAddress.isEmpty()) ? "Not Provided" : userWalletAddress;

        if (transactionId == null || transactionId.isEmpty()) {
            callback.onFailure("Transaction ID is required");
            return;
        }

        if (adminWalletAddress == null || adminWalletAddress.isEmpty()) {
            callback.onFailure("Admin wallet address is required");
            return;
        }

        // Generate unique request ID
        String requestId = UserHandler.getInstance().getTransactionsRef(userId, "deposit").push().getKey();
        if (requestId == null) {
            callback.onFailure("Failed to generate request ID");
            return;
        }

        long timestamp = System.currentTimeMillis();
        String date = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(new Date(timestamp));
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("id", requestId);
        transactionData.put("title", "Deposit Request");
        transactionData.put("subtitle", date + " • " + displayAddress);
        transactionData.put("amount", "+$" + amount);
        transactionData.put("status", "Pending");
        transactionData.put("type", "deposit");
        transactionData.put("timestamp", timestamp);
        transactionData.put("userWalletAddress", displayAddress);
        transactionData.put("transactionId", transactionId);
        transactionData.put("adminWalletAddress", adminWalletAddress);

        // Create admin request data (includes user info)
        Map<String, Object> adminRequestData = new HashMap<>();
        adminRequestData.putAll(transactionData);
        adminRequestData.put("userId", userId);
        adminRequestData.put("userWalletAddress", displayAddress);
        adminRequestData.put("adminWalletAddress", adminWalletAddress);

        // Update both locations
        mDatabase.child("transactions/deposit_req/" + userId + "_" + requestId).setValue(adminRequestData);
        
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("transactions/deposit/" + requestId, transactionData);

        UserHandler.getInstance().updateUserData(userId, userUpdates, 
                aVoid -> callback.onSuccess(requestId), 
                e -> callback.onFailure("Failed to create deposit request: " + e.getMessage()));
    }

    public void acceptDepositRequest(String userId, String requestId, DepositStatusCallback callback) {
        // First, get the request data from admin location
        mDatabase.child("transactions").child("deposit_req").child(userId + "_" + requestId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Update user transaction status to Success and update balance
                            UserHandler.getInstance().getUserData(userId, new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    double currentBalance = 0;
                                    double currentTotalDeposit = 0;
                                    
                                    if (userSnapshot.child("wallet_balance").exists()) {
                                        Object val = userSnapshot.child("wallet_balance").getValue();
                                        if (val instanceof Number) currentBalance = ((Number) val).doubleValue();
                                    }
                                    
                                    if (userSnapshot.child("total_deposit").exists()) {
                                        Object val = userSnapshot.child("total_deposit").getValue();
                                        if (val instanceof Number) currentTotalDeposit = ((Number) val).doubleValue();
                                    }

                                    Object amountObj = snapshot.child("amount").getValue();
                                    double depositAmount = 0;
                                    if (amountObj != null) {
                                        String amountStr = amountObj.toString().replace("+$", "").replace("$", "");
                                        try {
                                            depositAmount = Double.parseDouble(amountStr);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    final double finalDepositAmount = depositAmount;
                                    final double newBalance = currentBalance + finalDepositAmount;
                                    final double newTotalDeposit = currentTotalDeposit + finalDepositAmount;

                                    Map<String, Object> userUpdates = new HashMap<>();
                                    userUpdates.put("transactions/deposit/" + requestId + "/status", "Success");
                                    userUpdates.put("wallet_balance", newBalance);
                                    userUpdates.put("total_deposit", newTotalDeposit);

                                    mDatabase.child("transactions/deposit_req/" + userId + "_" + requestId).removeValue();

                                    UserHandler.getInstance().updateUserData(userId, userUpdates, 
                                            aVoid -> {
                                                // Distribute referral commissions
                                                if (finalDepositAmount > 0) {
                                                    new ReferralManager().distributeCommissions(userId, finalDepositAmount);
                                                }
                                                callback.onSuccess("Deposit request accepted");
                                            }, 
                                            e -> callback.onFailure("Failed to accept deposit request: " + e.getMessage()));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    callback.onFailure("Database error: " + error.getMessage());
                                }
                            });
                        } else {
                            callback.onFailure("Deposit request not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure("Database error: " + error.getMessage());
                    }
                });
    }

    public void rejectDepositRequest(String userId, String requestId, DepositStatusCallback callback) {
        // Update user transaction status to Failed and remove from admin location
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("transactions/deposit/" + requestId + "/status", "Failed");
        
        mDatabase.child("transactions/deposit_req/" + userId + "_" + requestId).removeValue();

        UserHandler.getInstance().updateUserData(userId, userUpdates, 
                aVoid -> callback.onSuccess("Deposit request rejected"), 
                e -> callback.onFailure("Failed to reject deposit request: " + e.getMessage()));
    }

    // Callback interfaces
    public interface DepositCallback {
        void onSuccess(String requestId);
        void onFailure(String error);
    }

    public interface DepositStatusCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}
