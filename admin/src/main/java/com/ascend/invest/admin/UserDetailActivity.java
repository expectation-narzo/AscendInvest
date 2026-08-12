package com.ascend.invest.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityUserDetailBinding;
import com.ascend.invest.admin.databinding.ItemHistoryGenericBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {
    private ActivityUserDetailBinding binding;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserRef;
    private String uid;
    private HistoryAdapter adapter;
    private List<HistoryItem> currentHistoryList = new ArrayList<>();
    private Map<String, List<HistoryItem>> historyMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabase.child("users").child(uid);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupTabs();
        setupRecyclerView();
        fetchUserDetails();
        binding.btnSave.setOnClickListener(v -> saveChanges());
        binding.btnResetDevice.setOnClickListener(v -> resetDevice());
    }

    private void resetDevice() {
        mUserRef.child("currentDeviceId").removeValue().addOnSuccessListener(aVoid -> {
            binding.etDeviceId.setText("");
            Toast.makeText(this, "Device authorization reset", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Deposits"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Withdrawals"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Profits"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Active Plans"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Referrals"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { updateHistoryList(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.rvUserHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUserHistory.setAdapter(adapter);
    }

    private void fetchUserDetails() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String username = snapshot.child("username").getValue(String.class);
                binding.tvDisplayName.setText(username);
                binding.etUsername.setText(username);
                binding.etEmail.setText(snapshot.child("email").getValue(String.class));
                binding.tvUidLabel.setText(String.format("UID: %s", uid));

                Object createdAt = snapshot.child("createdAt").getValue();
                if (createdAt instanceof Long) {
                    String date = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date((Long) createdAt));
                    binding.tvJoinDate.setText(String.format("Joined on: %s", date));
                }

                binding.etWalletBalance.setText(String.valueOf(getDouble(snapshot.child("wallet_balance").getValue())));
                binding.etUnlockedBalance.setText(String.valueOf(getDouble(snapshot.child("unlocked_balance").getValue())));
                binding.etTotalDeposit.setText(String.valueOf(getDouble(snapshot.child("total_deposit").getValue())));
                binding.etTotalProfit.setText(String.valueOf(getDouble(snapshot.child("total_profit").getValue())));
                binding.etP2pListed.setText(String.valueOf(getDouble(snapshot.child("p2p_listed_balance").getValue())));
                binding.etP2pEscrow.setText(String.valueOf(getDouble(snapshot.child("p2p_escrow_balance").getValue())));

                binding.etReferredBy.setText(snapshot.child("referredBy").getValue(String.class));
                binding.etReferralCode.setText(snapshot.child("myReferralCode").getValue(String.class));
                binding.etDeviceId.setText(snapshot.child("currentDeviceId").getValue(String.class));

                loadHistory(snapshot.child("transactions/deposit"), "Deposits");
                loadHistory(snapshot.child("transactions/withdraw"), "Withdrawals");
                loadHistory(snapshot.child("transactions/profit"), "Profits");
                loadHistory(snapshot.child("active_plans"), "Active Plans");
                loadHistory(snapshot.child("referral_history"), "Referrals");

                updateHistoryList(binding.tabLayout.getSelectedTabPosition());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadHistory(DataSnapshot snapshot, String key) {
        List<HistoryItem> list = new ArrayList<>();
        for (DataSnapshot ds : snapshot.getChildren()) {
            HistoryItem item = new HistoryItem();
            item.id = ds.getKey();
            item.type = key;
            
            if (key.equals("Active Plans")) {
                item.title = ds.child("planName").getValue(String.class);
                item.amount = "$" + String.format(Locale.US, "%.2f", getDouble(ds.child("investedAmount").getValue()));
                item.subtitle = "Yield: $" + String.format(Locale.US, "%.2f", getDouble(ds.child("dailyProfit").getValue())) + "/day";
                Object startTimeObj = ds.child("startTime").getValue();
                Object durationObj = ds.child("durationDays").getValue();
                if (startTimeObj instanceof Long && durationObj instanceof Number) {
                    long startTime = (Long) startTimeObj;
                    int duration = ((Number) durationObj).intValue();
                    int elapsed = (int) ((System.currentTimeMillis() - startTime) / (1000L * 60 * 60 * 24));
                    int remaining = Math.max(0, duration - elapsed);
                    item.status = remaining + " DAYS LEFT";
                } else {
                    item.status = ds.child("status").getValue(String.class);
                }
            } else if (key.equals("Referrals")) {
                item.title = "Lvl " + ds.child("level").getValue() + " Commission";
                item.amount = "+$" + String.format(Locale.US, "%.2f", getDouble(ds.child("amount").getValue()));
                item.subtitle = "Origin: " + ds.child("fromUser").getValue();
                item.status = "SUCCESS";
            } else {
                item.title = ds.child("title").getValue(String.class);
                item.amount = ds.child("amount").getValue(String.class);
                item.subtitle = ds.child("subtitle").getValue(String.class);
                item.status = ds.child("status").getValue(String.class);
            }
            list.add(item);
        }
        Collections.reverse(list);
        historyMap.put(key, list);
    }

    private void updateHistoryList(int position) {
        String key;
        switch (position) {
            case 0: key = "Deposits"; break;
            case 1: key = "Withdrawals"; break;
            case 2: key = "Profits"; break;
            case 3: key = "Active Plans"; break;
            case 4: key = "Referrals"; break;
            default: key = "Deposits";
        }
        currentHistoryList.clear();
        List<HistoryItem> items = historyMap.get(key);
        if (items != null) currentHistoryList.addAll(items);
        adapter.notifyDataSetChanged();
    }

    private void saveChanges() {
        Editable nameEd = binding.etUsername.getText();
        if (nameEd == null || TextUtils.isEmpty(nameEd.toString())) {
            Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> up = new HashMap<>();
        up.put("username", nameEd.toString().trim());
        up.put("wallet_balance", parseDouble(binding.etWalletBalance.getText()));
        up.put("unlocked_balance", parseDouble(binding.etUnlockedBalance.getText()));
        up.put("total_deposit", parseDouble(binding.etTotalDeposit.getText()));
        up.put("total_profit", parseDouble(binding.etTotalProfit.getText()));
        up.put("p2p_listed_balance", parseDouble(binding.etP2pListed.getText()));
        up.put("p2p_escrow_balance", parseDouble(binding.etP2pEscrow.getText()));
        up.put("referredBy", binding.etReferredBy.getText() != null ? binding.etReferredBy.getText().toString().trim() : "");
        up.put("myReferralCode", binding.etReferralCode.getText() != null ? binding.etReferralCode.getText().toString().trim() : "");

        mUserRef.updateChildren(up).addOnSuccessListener(aVoid -> 
            Toast.makeText(this, "Account Infrastructure Updated", Toast.LENGTH_SHORT).show());
    }

    private void processRequest(String rid, boolean approve, String type) {
        if (type.equals("Deposits")) {
            handleDepositAction(rid, approve);
        } else if (type.equals("Withdrawals")) {
            handleWithdrawAction(rid, approve);
        }
    }

    private void handleDepositAction(String rid, boolean approve) {
        if (approve) {
            mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    double currentWal = getDouble(userSnapshot.child("wallet_balance").getValue());
                    double currentDep = getDouble(userSnapshot.child("total_deposit").getValue());
                    
                    DataSnapshot transSnap = userSnapshot.child("transactions/deposit").child(rid);
                    double amt = getAmountFromStr(transSnap.child("amount").getValue(String.class));
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("wallet_balance", currentWal + amt);
                    updates.put("total_deposit", currentDep + amt);
                    updates.put("transactions/deposit/" + rid + "/status", "Success");

                    mUserRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                        distributeCommissions(uid, amt);
                        mDatabase.child("transactions/deposit_req").child(uid + "_" + rid).removeValue();
                        Toast.makeText(UserDetailActivity.this, "Deposit Approved", Toast.LENGTH_SHORT).show();
                    });
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            mUserRef.child("transactions/deposit").child(rid).child("status").setValue("Failed");
            mDatabase.child("transactions/deposit_req").child(uid + "_" + rid).removeValue();
            Toast.makeText(this, "Deposit Rejected", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleWithdrawAction(String rid, boolean approve) {
        if (approve) {
            mUserRef.child("transactions/withdraw").child(rid).child("status").setValue("Success");
            mDatabase.child("transactions/withdraw_req").child(uid + "_" + rid).removeValue();
            Toast.makeText(this, "Withdrawal Approved", Toast.LENGTH_SHORT).show();
        } else {
            mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    double currentWal = getDouble(userSnapshot.child("wallet_balance").getValue());
                    double currentUnl = getDouble(userSnapshot.child("unlocked_balance").getValue());
                    
                    DataSnapshot transSnap = userSnapshot.child("transactions/withdraw").child(rid);
                    double amt = getAmountFromStr(transSnap.child("amount").getValue(String.class));
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("wallet_balance", currentWal + amt);
                    updates.put("unlocked_balance", currentUnl + amt);
                    updates.put("transactions/withdraw/" + rid + "/status", "Failed");

                    mUserRef.updateChildren(updates);
                    mDatabase.child("transactions/withdraw_req").child(uid + "_" + rid).removeValue();
                    Toast.makeText(UserDetailActivity.this, "Withdrawal Rejected & Refunded", Toast.LENGTH_SHORT).show();
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
                    // Fetch dynamic percentage from database
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

    private void creditCommission(String targetUid, double amt, int level, String fromUid) {
        mDatabase.child("users").child(targetUid).addListenerForSingleValueEvent(new ValueEventListener() {
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

                String hid = mDatabase.child("users").child(targetUid).child("referral_history").push().getKey();
                if (hid != null) {
                    Map<String, Object> h = new HashMap<>();
                    h.put("amount", amt);
                    h.put("level", level);
                    h.put("fromUser", fromUid);
                    h.put("timestamp", System.currentTimeMillis());
                    updates.put("referral_history/" + hid, h);
                }
                
                String pId = mDatabase.child("users").child(targetUid).child("transactions/profit").push().getKey();
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
                mDatabase.child("users").child(targetUid).updateChildren(updates);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private double getDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) { try { return Double.parseDouble((String) val); } catch (Exception e) { return 0; } }
        return 0.0;
    }

    private double parseDouble(Editable e) {
        if (e == null || TextUtils.isEmpty(e.toString())) return 0.0;
        try { return Double.parseDouble(e.toString().trim()); } catch (Exception ex) { return 0.0; }
    }

    private double getAmountFromStr(String s) {
        if (s == null) return 0;
        try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); } catch (Exception e) { return 0; }
    }

    private static class HistoryItem {
        String id, title, amount, subtitle, status, type;
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(ItemHistoryGenericBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            HistoryItem item = currentHistoryList.get(pos);
            h.binding.tvHistTitle.setText(item.title);
            h.binding.tvHistAmount.setText(item.amount);
            h.binding.tvHistSubtitle.setText(item.subtitle);
            h.binding.tvHistStatus.setText(item.status != null ? item.status.toUpperCase() : "UNKNOWN");
            h.binding.tvHistId.setText(String.format("REF: %s", item.id));

            android.content.Context ctx = h.itemView.getContext();
            int color = androidx.core.content.ContextCompat.getColor(ctx, R.color.text_secondary);
            int bg = androidx.core.content.ContextCompat.getColor(ctx, R.color.surface_gray);
            
            if ("SUCCESS".equalsIgnoreCase(item.status) || "ACTIVE".equalsIgnoreCase(item.status) || (item.status != null && item.status.contains("DAYS LEFT"))) {
                color = androidx.core.content.ContextCompat.getColor(ctx, R.color.secondary_green);
                bg = androidx.core.content.ContextCompat.getColor(ctx, R.color.secondary_light);
            } else if ("FAILED".equalsIgnoreCase(item.status) || "REJECTED".equalsIgnoreCase(item.status)) {
                color = androidx.core.content.ContextCompat.getColor(ctx, R.color.error_red);
                bg = androidx.core.content.ContextCompat.getColor(ctx, R.color.error_light);
            } else if ("PENDING".equalsIgnoreCase(item.status)) {
                color = androidx.core.content.ContextCompat.getColor(ctx, R.color.warning_amber);
                bg = androidx.core.content.ContextCompat.getColor(ctx, R.color.warning_light);
            }
            
            h.binding.tvHistStatus.setTextColor(color);
            h.binding.tvHistStatus.setBackgroundColor(bg);

            // Action Buttons Logic
            if ("PENDING".equalsIgnoreCase(item.status) && ("Deposits".equals(item.type) || "Withdrawals".equals(item.type))) {
                h.binding.llHistActions.setVisibility(View.VISIBLE);
                h.binding.btnHistApprove.setOnClickListener(v -> processRequest(item.id, true, item.type));
                h.binding.btnHistReject.setOnClickListener(v -> processRequest(item.id, false, item.type));
            } else {
                h.binding.llHistActions.setVisibility(View.GONE);
            }
        }

        @Override public int getItemCount() { return currentHistoryList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemHistoryGenericBinding binding;
            ViewHolder(ItemHistoryGenericBinding b) { super(b.getRoot()); this.binding = b; }
        }
    }
}
