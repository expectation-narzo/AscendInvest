package com.ascend.invest.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ascend.invest.admin.databinding.ActivityTransactionSearchBinding;
import com.ascend.invest.admin.databinding.ItemRequestBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TransactionSearchActivity extends AppCompatActivity {
    private ActivityTransactionSearchBinding binding;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityTransactionSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSearch.setOnClickListener(v -> {
            String tid = binding.etTransactionId.getText().toString().trim();
            if (!TextUtils.isEmpty(tid)) {
                lookupTransaction(tid);
            }
        });
    }

    private void lookupTransaction(String tid) {
        binding.containerResult.removeAllViews();
        binding.tvLookupStatus.setText("Scanning database for " + tid + "...");
        
        // Search in deposits
        mDatabase.child("transactions/deposit_req").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (tid.equals(ds.child("id").getValue(String.class))) {
                        displayResult(ds, "deposit");
                        binding.tvLookupStatus.setText("Transaction found in Deposit Queue");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Search in withdrawals
                    mDatabase.child("transactions/withdraw_req").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean foundInWithdraw = false;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (tid.equals(ds.child("id").getValue(String.class))) {
                                    displayResult(ds, "withdraw");
                                    binding.tvLookupStatus.setText("Transaction found in Withdrawal Queue");
                                    foundInWithdraw = true;
                                    break;
                                }
                            }
                            if (!foundInWithdraw) {
                                binding.tvLookupStatus.setText("No active request found with this ID");
                                Toast.makeText(TransactionSearchActivity.this, "Transaction not found or already processed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayResult(DataSnapshot ds, String type) {
        ItemRequestBinding itemBinding = ItemRequestBinding.inflate(LayoutInflater.from(this), binding.containerResult, true);
        
        String userId = ds.child("userId").getValue(String.class);
        String requestId = ds.child("id").getValue(String.class);
        String amount = ds.child("amount").getValue(String.class);
        String details = ds.child("subtitle").getValue(String.class);
        
        String walletAddress = ds.child("userWalletAddress").getValue(String.class);
        String txId = ds.child("transactionId").getValue(String.class);

        itemBinding.tvUserId.setText("User: " + userId);
        itemBinding.tvAmount.setText(amount);
        itemBinding.tvDetails.setText(details);
        itemBinding.tvStatusChip.setText(type.toUpperCase());

        if (type.equals("deposit")) {
            if (walletAddress != null) {
                itemBinding.tvWalletAddress.setVisibility(View.VISIBLE);
                itemBinding.tvWalletAddress.setText("Wallet: " + walletAddress);
            }
            if (txId != null) {
                itemBinding.tvTxId.setVisibility(View.VISIBLE);
                itemBinding.tvTxId.setText("TXID: " + txId);
            }
        }

        itemBinding.btnApprove.setOnClickListener(v -> process(userId, requestId, true, type, ds));
        itemBinding.btnReject.setOnClickListener(v -> process(userId, requestId, false, type, ds));
    }

    private void process(String uid, String rid, boolean approve, String type, DataSnapshot ds) {
        // Reuse logic from ManageRequestsActivity or move to a helper
        // For simplicity I'll re-implement here to avoid complex activity communication
        if (type.equals("deposit")) {
            handleDeposit(uid, rid, approve, ds);
        } else {
            handleWithdraw(uid, rid, approve, ds);
        }
    }

    // Re-implementing the core logic
    private void handleDeposit(String uid, String rid, boolean approve, DataSnapshot reqData) {
        if (approve) {
            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    double currentWal = getDouble(userSnapshot.child("wallet_balance").getValue());
                    double currentDep = getDouble(userSnapshot.child("total_deposit").getValue());
                    double amt = getAmountFromStr(reqData.child("amount").getValue(String.class));
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("wallet_balance", currentWal + amt);
                    updates.put("total_deposit", currentDep + amt);
                    updates.put("transactions/deposit/" + rid + "/status", "Success");

                    mDatabase.child("users").child(uid).updateChildren(updates).addOnSuccessListener(aVoid -> {
                        distributeCommissions(uid, amt);
                        mDatabase.child("transactions/deposit_req").child(uid + "_" + rid).removeValue();
                        Toast.makeText(TransactionSearchActivity.this, "Approved", Toast.LENGTH_SHORT).show();
                        binding.containerResult.removeAllViews();
                    });
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            mDatabase.child("users").child(uid).child("transactions/deposit").child(rid).child("status").setValue("Failed");
            mDatabase.child("transactions/deposit_req").child(uid + "_" + rid).removeValue();
            Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show();
            binding.containerResult.removeAllViews();
        }
    }

    private void handleWithdraw(String uid, String rid, boolean approve, DataSnapshot reqData) {
        if (approve) {
            mDatabase.child("users").child(uid).child("transactions/withdraw").child(rid).child("status").setValue("Success");
            mDatabase.child("transactions/withdraw_req").child(uid + "_" + rid).removeValue();
            Toast.makeText(this, "Approved", Toast.LENGTH_SHORT).show();
            binding.containerResult.removeAllViews();
        } else {
            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    double currentWal = getDouble(userSnapshot.child("wallet_balance").getValue());
                    double currentUnl = getDouble(userSnapshot.child("unlocked_balance").getValue());
                    double amt = getAmountFromStr(reqData.child("amount").getValue(String.class));
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("wallet_balance", currentWal + amt);
                    updates.put("unlocked_balance", currentUnl + amt);
                    updates.put("transactions/withdraw/" + rid + "/status", "Failed");

                    mDatabase.child("users").child(uid).updateChildren(updates);
                    mDatabase.child("transactions/withdraw_req").child(uid + "_" + rid).removeValue();
                    Toast.makeText(TransactionSearchActivity.this, "Rejected & Refunded", Toast.LENGTH_SHORT).show();
                    binding.containerResult.removeAllViews();
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void distributeCommissions(String userId, double amount) {
        findReferrerAndDistribute(userId, amount, 1, userId);
    }

    private void findReferrerAndDistribute(String currentUserId, double depositAmount, int level, String originalId) {
        if (level > 20) return;
        mDatabase.child("users").child(currentUserId).child("referredBy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String referrerUid = snapshot.getValue(String.class);
                if (referrerUid != null && !referrerUid.isEmpty()) {
                    mDatabase.child("level/refer").child("level" + level).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot levelSnap) {
                            double percentage = 0.0;
                            if (levelSnap.exists()) {
                                percentage = getDouble(levelSnap.getValue());
                            } else {
                                percentage = getDefaultPercentage(level);
                            }
                            
                            double commission = (depositAmount * percentage) / 100.0;
                            if (commission > 0) creditCommission(referrerUid, commission, level, originalId);
                            findReferrerAndDistribute(referrerUid, depositAmount, level + 1, originalId);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private double getDefaultPercentage(int level) {
        if (level == 1) return 10.0;
        if (level == 2) return 7.0;
        if (level == 3) return 5.0;
        if (level == 4) return 3.0;
        if (level >= 5 && level <= 10) return 2.0;
        if (level >= 11 && level <= 20) return 1.0;
        return 0.0;
    }

    private void creditCommission(String uid, double amt, int level, String fromUid) {
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                double ref = getDouble(s.child("referral_balance").getValue());
                double wal = getDouble(s.child("wallet_balance").getValue());
                double prof = getDouble(s.child("total_profit").getValue());
                double unl = getDouble(s.child("unlocked_balance").getValue());

                Map<String, Object> updates = new HashMap<>();
                updates.put("referral_balance", ref + amt);
                updates.put("wallet_balance", wal + amt);
                updates.put("total_profit", prof + amt);
                updates.put("unlocked_balance", unl + amt);

                String hid = mDatabase.child("users").child(uid).child("referral_history").push().getKey();
                if (hid != null) {
                    Map<String, Object> h = new HashMap<>();
                    h.put("amount", amt);
                    h.put("level", level);
                    h.put("fromUser", fromUid);
                    h.put("timestamp", System.currentTimeMillis());
                    updates.put("referral_history/" + hid, h);
                }
                
                String pId = mDatabase.child("users").child(uid).child("transactions/profit").push().getKey();
                if (pId != null) {
                    Map<String, Object> p = new HashMap<>();
                    p.put("id", pId);
                    p.put("title", "Referral Commission (Lvl " + level + ")");
                    p.put("amount", "+$" + String.format(Locale.US, "%.2f", amt));
                    p.put("status", "Success");
                    p.put("type", "profit");
                    p.put("timestamp", System.currentTimeMillis());
                    updates.put("transactions/profit/" + pId, p);
                }
                mDatabase.child("users").child(uid).updateChildren(updates);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private double getDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }

    private double getAmountFromStr(String s) {
        if (s == null) return 0;
        try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); } catch (Exception e) { return 0; }
    }
}
