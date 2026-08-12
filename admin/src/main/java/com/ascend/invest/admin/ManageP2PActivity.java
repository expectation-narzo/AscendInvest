package com.ascend.invest.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityManageP2pBinding;
import com.ascend.invest.admin.databinding.ItemP2pDisputeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageP2PActivity extends AppCompatActivity {
    private ActivityManageP2pBinding binding;
    private DatabaseReference mDatabase;
    private List<P2POrder> p2pList = new ArrayList<>();
    private P2PAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityManageP2pBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new P2PAdapter();
        binding.rvP2p.setLayoutManager(new LinearLayoutManager(this));
        binding.rvP2p.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { fetchP2PData(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        fetchP2PData(0);
    }

    private void fetchP2PData(int tabIndex) {
        DatabaseReference ref = tabIndex == 0 ? mDatabase.child("p2p_order_history") : mDatabase.child("p2p_order_history");
        
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                p2pList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    P2POrder order = ds.getValue(P2POrder.class);
                    if (order != null) {
                        if (tabIndex == 0) {
                            if ("DISPUTED".equals(order.status)) p2pList.add(order);
                        } else {
                            p2pList.add(order);
                        }
                    }
                }
                Collections.reverse(p2pList);
                adapter.notifyDataSetChanged();
                binding.tvEmpty.setVisibility(p2pList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void releaseAssets(P2POrder order) {
        // Force complete the trade for the buyer
        mDatabase.child("users").child(order.buyerUid).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) return Transaction.success(currentData);
                double wallet = getDouble(currentData.child("wallet_balance").getValue());
                double totalProf = getDouble(currentData.child("total_profit").getValue());
                double unlocked = getDouble(currentData.child("unlocked_balance").getValue());

                currentData.child("wallet_balance").setValue(wallet + order.amount);
                currentData.child("total_profit").setValue(totalProf + order.amount);
                currentData.child("unlocked_balance").setValue(unlocked + order.amount);
                return Transaction.success(currentData);
            }

            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("p2p_order_history/" + order.id + "/status", "COMPLETED");
                    
                    // Clean up seller's escrow
                    mDatabase.child("users").child(order.sellerUid).child("p2p_escrow_balance").runTransaction(new Transaction.Handler() {
                        @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            double val = getDouble(currentData.getValue());
                            currentData.setValue(Math.max(0, val - order.amount));
                            return Transaction.success(currentData);
                        }
                        @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {}
                    });

                    mDatabase.updateChildren(updates);
                    Toast.makeText(ManageP2PActivity.this, "USDT Released to Buyer", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelDisputedOrder(P2POrder order) {
        // Refund seller
        mDatabase.child("users").child(order.sellerUid).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) return Transaction.success(currentData);
                double listed = getDouble(currentData.child("p2p_listed_balance").getValue());
                double escrow = getDouble(currentData.child("p2p_escrow_balance").getValue());
                
                currentData.child("p2p_listed_balance").setValue(listed + order.amount);
                currentData.child("p2p_escrow_balance").setValue(Math.max(0, escrow - order.amount));
                return Transaction.success(currentData);
            }

            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    mDatabase.child("p2p_order_history").child(order.id).child("status").setValue("CANCELLED");
                    Toast.makeText(ManageP2PActivity.this, "Order Cancelled & Seller Refunded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private double getDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }

    class P2PAdapter extends RecyclerView.Adapter<P2PAdapter.ViewHolder> {
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(ItemP2pDisputeBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            P2POrder order = p2pList.get(pos);
            h.binding.tvP2pTitle.setText("Trade #" + order.id.substring(0, Math.min(order.id.length(), 8)));
            h.binding.tvP2pAmount.setText(String.format(Locale.US, "$%.2f", order.amount));
            h.binding.tvP2pParties.setText(String.format("Buyer: %s | Seller: %s", order.buyerName, order.sellerName));
            h.binding.tvP2pRef.setText("Proof Ref: " + (order.transactionId != null ? order.transactionId : "NONE"));

            if (order.commission > 0) {
                h.binding.tvP2pCommission.setVisibility(View.VISIBLE);
                h.binding.tvP2pCommission.setText(String.format(Locale.US, "System Fee Applied: %.1f%%", order.commission));
            } else {
                h.binding.tvP2pCommission.setVisibility(View.GONE);
            }

            if ("DISPUTED".equals(order.status)) {
                h.binding.btnP2pRelease.setVisibility(View.VISIBLE);
                h.binding.btnP2pCancel.setVisibility(View.VISIBLE);
                h.binding.btnP2pChat.setVisibility(View.VISIBLE);
                h.binding.btnP2pRelease.setOnClickListener(v -> releaseAssets(order));
                h.binding.btnP2pCancel.setOnClickListener(v -> cancelDisputedOrder(order));
                h.binding.btnP2pChat.setOnClickListener(v -> {
                    Intent intent = new Intent(ManageP2PActivity.this, ManageP2PChatActivity.class);
                    intent.putExtra("orderId", order.id);
                    startActivity(intent);
                });
            } else {
                h.binding.btnP2pRelease.setVisibility(View.GONE);
                h.binding.btnP2pCancel.setVisibility(View.GONE);
                h.binding.btnP2pChat.setVisibility(View.GONE);
                h.binding.tvP2pTitle.setText(h.binding.tvP2pTitle.getText() + " (" + order.status + ")");
            }
        }

        @Override public int getItemCount() { return p2pList.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            ItemP2pDisputeBinding binding;
            ViewHolder(ItemP2pDisputeBinding b) { super(b.getRoot()); this.binding = b; }
        }
    }
}
