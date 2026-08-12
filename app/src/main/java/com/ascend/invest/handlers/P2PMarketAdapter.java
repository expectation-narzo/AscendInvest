package com.ascend.invest.handlers;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class P2PMarketAdapter extends RecyclerView.Adapter<P2PMarketAdapter.ViewHolder> {

    private final List<P2PListing> listings;
    private final String currentUserId;
    private final OnListingClickListener listener;
    private final Map<ViewHolder, ValueEventListener> statusListeners = new HashMap<>();
    private final Map<ViewHolder, DatabaseReference> statusRefs = new HashMap<>();

    public interface OnListingClickListener {
        void onActionClick(P2PListing listing, boolean isDelete);
    }

    public P2PMarketAdapter(List<P2PListing> listings, String currentUserId, OnListingClickListener listener) {
        this.listings = listings;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_p2p_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        P2PListing listing = listings.get(position);
        holder.tvName.setText(listing.sellerName != null ? listing.sellerName : "User");
        holder.tvPrice.setText(String.format(Locale.US, "₹%.2f", listing.price));
        holder.tvAmount.setText(String.format(Locale.US, "%.2f USDT", listing.remainingAmount));
        holder.tvLimit.setText(String.format(Locale.US, "Min: $%.2f", listing.minLimit));

        boolean isOwnAd = listing.sellerUid.equals(currentUserId);
        
        if (isOwnAd) {
            holder.btnAction.setText("Delete Ad");
            holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EF4444"))); // Red
        } else {
            holder.btnAction.setText("Buy USDT");
            holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22C55E"))); // Green
        }

        // Cleanup old listener
        ValueEventListener oldL = statusListeners.remove(holder);
        DatabaseReference oldR = statusRefs.remove(holder);
        if (oldL != null && oldR != null) oldR.removeEventListener(oldL);

        // Fetch Seller Online Status
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference().child("users").child(listing.sellerUid).child("status");
        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isOnline = snapshot.exists() && "online".equals(snapshot.getValue(String.class));
                int color = Color.parseColor(isOnline ? "#22C55E" : "#94A3B8");
                
                holder.onlineIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                if (!isOwnAd) {
                    holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        
        statusRef.addValueEventListener(statusListener);
        statusListeners.put(holder, statusListener);
        statusRefs.put(holder, statusRef);
        
        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onActionClick(listing, isOwnAd);
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        ValueEventListener l = statusListeners.remove(holder);
        DatabaseReference r = statusRefs.remove(holder);
        if (l != null && r != null) r.removeEventListener(l);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvLimit, tvPrice;
        MaterialButton btnAction;
        View onlineIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_seller_name);
            tvPrice = itemView.findViewById(R.id.tv_listing_price);
            tvAmount = itemView.findViewById(R.id.tv_listing_amount);
            tvLimit = itemView.findViewById(R.id.tv_listing_limit);
            btnAction = itemView.findViewById(R.id.btn_buy_listing);
            onlineIndicator = itemView.findViewById(R.id.view_online_indicator);
        }
    }
}
