package com.ascend.invest.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityManageRequestsBinding;
import com.ascend.invest.admin.databinding.ItemRequestBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManageRequestsActivity extends AppCompatActivity {
    private ActivityManageRequestsBinding binding;
    private DatabaseReference mDatabase;
    private String type;
    private List<DataSnapshot> requestList = new ArrayList<>();
    private RequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityManageRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        type = getIntent().getStringExtra("type");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        binding.tvTitle.setText(Objects.equals(type, "deposit") ? "Deposit Requests" : "Withdrawal Requests");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new RequestAdapter();
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRequests.setAdapter(adapter);

        fetchRequests();
    }

    private void fetchRequests() {
        String path = type.equals("deposit") ? "transactions/deposit_req" : "transactions/withdraw_req";
        mDatabase.child(path).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    requestList.add(ds);
                }
                adapter.notifyDataSetChanged();
                binding.tvEmpty.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataSnapshot ds = requestList.get(position);
            String userId = ds.child("userId").getValue(String.class);
            String amount = ds.child("amount").getValue(String.class);
            String details = ds.child("subtitle").getValue(String.class);
            String requestId = ds.child("id").getValue(String.class);
            
            // New fields for deposit
            String walletAddress = ds.child("userWalletAddress").getValue(String.class);
            String txId = ds.child("transactionId").getValue(String.class);

            String shortUid = (userId != null && userId.length() > 8) ? userId.substring(0, 8) + "..." : userId;
            holder.binding.tvUserId.setText("UID: " + shortUid);
            holder.binding.tvAmount.setText(amount);
            holder.binding.tvDetails.setText(details);

            if (type.equals("deposit")) {
                if (walletAddress != null) {
                    holder.binding.tvWalletAddress.setVisibility(View.VISIBLE);
                    holder.binding.tvWalletAddress.setText("Wallet: " + walletAddress);
                } else {
                    holder.binding.tvWalletAddress.setVisibility(View.GONE);
                }
                
                if (txId != null) {
                    holder.binding.tvTxId.setVisibility(View.VISIBLE);
                    holder.binding.tvTxId.setText("TXID: " + txId);
                } else {
                    holder.binding.tvTxId.setVisibility(View.GONE);
                }
            } else {
                holder.binding.tvWalletAddress.setVisibility(View.GONE);
                holder.binding.tvTxId.setVisibility(View.GONE);
            }

            // Dynamic Status Chip Colors
            android.content.Context context = holder.itemView.getContext();
            if (type.equals("deposit")) {
                holder.binding.tvStatusChip.setText("DEPOSIT");
                holder.binding.tvStatusChip.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.secondary_green));
                holder.binding.tvStatusChip.setBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.secondary_light));
            } else {
                holder.binding.tvStatusChip.setText("WITHDRAW");
                holder.binding.tvStatusChip.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.error_red));
                holder.binding.tvStatusChip.setBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.error_light));
            }

            holder.binding.btnApprove.setOnClickListener(v -> processRequest(userId, requestId, true, ds));
            holder.binding.btnReject.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(ManageRequestsActivity.this)
                    .setTitle("Reject Request")
                    .setMessage("Are you sure you want to reject this request?")
                    .setPositiveButton("Reject", (dialog, which) -> processRequest(userId, requestId, false, ds))
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }

        @Override
        public int getItemCount() {
            return requestList.size();
        }

        private void processRequest(String uid, String rid, boolean approve, DataSnapshot reqData) {
            if (type.equals("deposit")) {
                handleDeposit(uid, rid, approve, reqData);
            } else {
                handleWithdraw(uid, rid, approve, reqData);
            }
        }

        private void handleDeposit(String uid, String rid, boolean approve, DataSnapshot reqData) {
            if (approve) {
                mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        double walletBal = 0, depositBal = 0;
                        if (userSnapshot.child("wallet_balance").exists()) walletBal = getDouble(userSnapshot.child("wallet_balance").getValue());
                        if (userSnapshot.child("total_deposit").exists()) depositBal = getDouble(userSnapshot.child("total_deposit").getValue());

                        double amt = getAmountFromStr(reqData.child("amount").getValue(String.class));
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("wallet_balance", walletBal + amt);
                        updates.put("total_deposit", depositBal + amt);
                        updates.put("transactions/deposit/" + rid + "/status", "Success");

                        mDatabase.child("users").child(uid).updateChildren(updates).addOnSuccessListener(aVoid -> {
                            distributeReferralCommissions(uid, amt);
                            mDatabase.child("transactions/deposit_req").child(uid + "_" + rid).removeValue();
                            Toast.makeText(ManageRequestsActivity.this, "Deposit Approved & Commissions Distributed", Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            } else {
                mDatabase.child("users").child(uid).child("transactions/deposit").child(rid).child("status").setValue("Failed");
                mDatabase.child("transactions/deposit_req").child(uid + "_" + rid).removeValue();
                Toast.makeText(ManageRequestsActivity.this, "Deposit Rejected", Toast.LENGTH_SHORT).show();
            }
        }

        private void handleWithdraw(String uid, String rid, boolean approve, DataSnapshot reqData) {
            if (approve) {
                mDatabase.child("users").child(uid).child("transactions/withdraw").child(rid).child("status").setValue("Success");
                mDatabase.child("transactions/withdraw_req").child(uid + "_" + rid).removeValue();
                Toast.makeText(ManageRequestsActivity.this, "Withdrawal Approved", Toast.LENGTH_SHORT).show();
            } else {
                mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        double currentWal = 0, currentUnl = 0;
                        if (userSnapshot.child("wallet_balance").exists()) currentWal = getDouble(userSnapshot.child("wallet_balance").getValue());
                        if (userSnapshot.child("unlocked_balance").exists()) currentUnl = getDouble(userSnapshot.child("unlocked_balance").getValue());

                        double amt = getAmountFromStr(reqData.child("amount").getValue(String.class));
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("wallet_balance", currentWal + amt);
                        updates.put("unlocked_balance", currentUnl + amt);
                        updates.put("transactions/withdraw/" + rid + "/status", "Failed");

                        mDatabase.child("users").child(uid).updateChildren(updates);
                        mDatabase.child("transactions/withdraw_req").child(uid + "_" + rid).removeValue();
                        Toast.makeText(ManageRequestsActivity.this, "Withdrawal Rejected & Refunded", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        }

        private void distributeReferralCommissions(String userId, double amount) {
            findReferrerAndDistribute(userId, amount, 1, userId);
        }

        private void findReferrerAndDistribute(String currentUserId, double depositAmount, int level, String originalDepositorId) {
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
                                    percentage = getDefaultCommissionPercentage(level);
                                }
                                
                                double commission = (depositAmount * percentage) / 100.0;
                                if (commission > 0) {
                                    addCommissionToUser(referrerUid, commission, level, originalDepositorId);
                                }
                                findReferrerAndDistribute(referrerUid, depositAmount, level + 1, originalDepositorId);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
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

        private void addCommissionToUser(String uid, double amt, int level, String fromUid) {
            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double refBal = 0, walBal = 0, totProf = 0, unlBal = 0;
                    if (snapshot.child("referral_balance").exists()) refBal = getDouble(snapshot.child("referral_balance").getValue());
                    if (snapshot.child("wallet_balance").exists()) walBal = getDouble(snapshot.child("wallet_balance").getValue());
                    if (snapshot.child("total_profit").exists()) totProf = getDouble(snapshot.child("total_profit").getValue());
                    if (snapshot.child("unlocked_balance").exists()) unlBal = getDouble(snapshot.child("unlocked_balance").getValue());

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("referral_balance", refBal + amt);
                    updates.put("wallet_balance", walBal + amt);
                    updates.put("total_profit", totProf + amt);
                    updates.put("unlocked_balance", unlBal + amt);

                    String historyId = mDatabase.child("users").child(uid).child("referral_history").push().getKey();
                    if (historyId != null) {
                        Map<String, Object> historyData = new HashMap<>();
                        historyData.put("amount", amt);
                        historyData.put("level", level);
                        historyData.put("fromUser", fromUid);
                        historyData.put("timestamp", System.currentTimeMillis());
                        updates.put("referral_history/" + historyId, historyData);
                    }

                    // Add to profit transactions for charting
                    String profitTxId = mDatabase.child("users").child(uid).child("transactions").child("profit").push().getKey();
                    if (profitTxId != null) {
                        Map<String, Object> profitTxData = new HashMap<>();
                        profitTxData.put("id", profitTxId);
                        profitTxData.put("title", "Referral Commission (Lvl " + level + ")");
                        profitTxData.put("amount", "+$" + String.format(java.util.Locale.US, "%.2f", amt));
                        profitTxData.put("status", "Success");
                        profitTxData.put("type", "profit");
                        profitTxData.put("timestamp", System.currentTimeMillis());
                        updates.put("transactions/profit/" + profitTxId, profitTxData);
                    }

                    mDatabase.child("users").child(uid).updateChildren(updates);
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        private double getDouble(Object val) {
            if (val instanceof Number) return ((Number) val).doubleValue();
            if (val instanceof String) {
                try { return Double.parseDouble((String) val); } catch (Exception e) { return 0; }
            }
            return 0.0;
        }

        private double getAmountFromStr(String s) {
            if (s == null) return 0;
            try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); } catch (Exception e) { return 0; }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemRequestBinding binding;
            ViewHolder(ItemRequestBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
