package com.ascend.invest.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class P2PHandler {

    private final Context context;
    private final String currentUserId;
    private final DatabaseReference mDatabase;
    private String currentUsername = "User";

    private double currentUnlockedBalance = 0.0;
    private double p2pCommission = 0.0;
    private boolean isUidVerified = false;

    // Marketplace Data
    private List<P2PListing> marketListings = new ArrayList<>();
    private P2PMarketAdapter marketAdapter;
    private List<P2PListing> myListings = new ArrayList<>();
    private P2PMarketAdapter myAdsAdapter;

    private BottomSheetDialog activeTradeDialog;
    private String activeOrderId;
    private BottomSheetDialog activeReleaseDialog;
    private String activeReleaseOrderId;
    private BottomSheetDialog activeAcceptDialog;
    private String activeAcceptOrderId;
    private final Map<String, ValueEventListener> activePresenceListeners = new HashMap<>();
    private final Set<String> notifiedOrders = new HashSet<>();
    private final Set<String> promptedOrders = new HashSet<>();

    public P2PHandler(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        fetchCurrentUsername();
        fetchP2PCommission();
        listenForActiveOrders(); // Start listening immediately on app start
    }

    private void fetchP2PCommission() {
        mDatabase.child("commission").child("p2p").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    p2pCommission = getDouble(snapshot.getValue());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCurrentUsername() {
        mDatabase.child("users").child(currentUserId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) currentUsername = snapshot.getValue(String.class);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void setupP2PSection(View rootView) {
        // Tab Management
        TextView tabMarket = rootView.findViewById(R.id.tab_p2p_market);
        TextView tabMyAds = rootView.findViewById(R.id.tab_p2p_my_ads);
        TextView tabDirect = rootView.findViewById(R.id.tab_p2p_direct);
        View layoutMarket = rootView.findViewById(R.id.ll_p2p_market_container);
        View layoutMyAds = rootView.findViewById(R.id.ll_p2p_my_ads_container);
        View layoutDirect = rootView.findViewById(R.id.ll_p2p_direct_container);
        View btnPostAd = rootView.findViewById(R.id.btn_post_p2p_ad);

        tabMarket.setOnClickListener(v -> {
            tabMarket.setBackgroundResource(R.drawable.menu_item_bg);
            tabMarket.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_purple));
            tabMarket.setTextColor(Color.parseColor("#000000"));
            tabMyAds.setBackground(null);
            tabMyAds.setTextColor(Color.parseColor("#64748B"));
            tabDirect.setBackground(null);
            tabDirect.setTextColor(Color.parseColor("#64748B"));
            layoutMarket.setVisibility(View.VISIBLE);
            layoutMyAds.setVisibility(View.GONE);
            layoutDirect.setVisibility(View.GONE);
            if (btnPostAd != null) btnPostAd.setVisibility(View.VISIBLE);
        });

        tabMyAds.setOnClickListener(v -> {
            tabMyAds.setBackgroundResource(R.drawable.menu_item_bg);
            tabMyAds.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_purple));
            tabMyAds.setTextColor(Color.parseColor("#000000"));
            tabMarket.setBackground(null);
            tabMarket.setTextColor(Color.parseColor("#64748B"));
            tabDirect.setBackground(null);
            tabDirect.setTextColor(Color.parseColor("#64748B"));
            layoutMyAds.setVisibility(View.VISIBLE);
            layoutMarket.setVisibility(View.GONE);
            layoutDirect.setVisibility(View.GONE);
            if (btnPostAd != null) btnPostAd.setVisibility(View.VISIBLE);
        });

        tabDirect.setOnClickListener(v -> {
            tabDirect.setBackgroundResource(R.drawable.menu_item_bg);
            tabDirect.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_purple));
            tabDirect.setTextColor(Color.parseColor("#000000"));
            tabMarket.setBackground(null);
            tabMarket.setTextColor(Color.parseColor("#64748B"));
            tabMyAds.setBackground(null);
            tabMyAds.setTextColor(Color.parseColor("#64748B"));
            layoutDirect.setVisibility(View.VISIBLE);
            layoutMarket.setVisibility(View.GONE);
            layoutMyAds.setVisibility(View.GONE);
            if (btnPostAd != null) btnPostAd.setVisibility(View.GONE);
        });

        // Marketplace Setup
        RecyclerView rvMarket = rootView.findViewById(R.id.rv_p2p_marketplace);
        if (rvMarket != null) {
            rvMarket.setLayoutManager(new LinearLayoutManager(context));
            marketAdapter = new P2PMarketAdapter(marketListings, currentUserId, (listing, isDelete) -> {
                if (isDelete) {
                    showDeleteAdConfirmation(listing);
                } else {
                    showBuyDialog(listing);
                }
            });
            rvMarket.setAdapter(marketAdapter);
            fetchMarketListings();
        }

        // My Ads Setup
        RecyclerView rvMyAds = rootView.findViewById(R.id.rv_p2p_my_ads);
        if (rvMyAds != null) {
            rvMyAds.setLayoutManager(new LinearLayoutManager(context));
            myAdsAdapter = new P2PMarketAdapter(myListings, currentUserId, (listing, isDelete) -> {
                if (isDelete) {
                    showDeleteAdConfirmation(listing);
                } else {
                    showBuyDialog(listing);
                }
            });
            rvMyAds.setAdapter(myAdsAdapter);
            fetchMyAds();
        }

        if (btnPostAd != null) btnPostAd.setOnClickListener(v -> showPostAdDialog());

        // Dispute Monitoring
        View cvDisputeAlert = rootView.findViewById(R.id.cv_p2p_disputes_alert);
        TextView tvDisputeCount = rootView.findViewById(R.id.tv_dispute_count);
        View btnOpenDisputes = rootView.findViewById(R.id.btn_open_disputes);
        setupDisputeMonitoring(cvDisputeAlert, tvDisputeCount, btnOpenDisputes);

        // Direct Transfer Setup
        setupDirectTransfer(rootView);

        // Default to Market Tab
        tabMarket.performClick();
    }

    private void fetchMarketListings() {
        mDatabase.child("p2p_listings").orderByChild("status").equalTo("ACTIVE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                marketListings.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    P2PListing listing = ds.getValue(P2PListing.class);
                    if (listing != null && !listing.sellerUid.equals(currentUserId)) {
                        marketListings.add(listing);
                    }
                }
                if (marketAdapter != null) marketAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchMyAds() {
        mDatabase.child("p2p_listings").orderByChild("sellerUid").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myListings.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    P2PListing listing = ds.getValue(P2PListing.class);
                    if (listing != null && !"DELETED".equals(listing.status)) {
                        myListings.add(listing);
                    }
                }
                if (myAdsAdapter != null) myAdsAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showPostAdDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_post_p2p_ad, null);
        dialog.setContentView(view);

        TextInputEditText etAmount = view.findViewById(R.id.et_ad_amount);
        TextInputEditText etPrice = view.findViewById(R.id.et_ad_price);
        TextInputEditText etMin = view.findViewById(R.id.et_ad_min_limit);
        TextInputEditText etUpi = view.findViewById(R.id.et_ad_upi);
        TextView tvFee = view.findViewById(R.id.tv_post_ad_fee);
        MaterialButton btnPost = view.findViewById(R.id.btn_confirm_post_ad);

        if (tvFee != null) {
            tvFee.setText(String.format(Locale.US, "Listing Tax: %.1f%%", p2pCommission));
        }

        if (etAmount != null) {
            etAmount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        double amt = Double.parseDouble(s.toString());
                        if (tvFee != null) {
                            double feeAmt = (amt * p2pCommission) / 100.0;
                            tvFee.setText(String.format(Locale.US, "Listing Tax: %.1f%% ($%.2f)", p2pCommission, feeAmt));
                        }
                    } catch (Exception e) {
                        if (tvFee != null) tvFee.setText(String.format(Locale.US, "Listing Tax: %.1f%%", p2pCommission));
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnPost != null) {
            btnPost.setOnClickListener(v -> {
                String amountStr = (etAmount != null && etAmount.getText() != null) ? etAmount.getText().toString().trim() : "";
                String priceStr = (etPrice != null && etPrice.getText() != null) ? etPrice.getText().toString().trim() : "";
                String minStr = (etMin != null && etMin.getText() != null) ? etMin.getText().toString().trim() : "";
                String upi = (etUpi != null && etUpi.getText() != null) ? etUpi.getText().toString().trim() : "";

                if (amountStr.isEmpty()) { if (etAmount != null) etAmount.setError("Required"); return; }
                if (priceStr.isEmpty()) { if (etPrice != null) etPrice.setError("Required"); return; }
                if (upi.isEmpty()) { if (etUpi != null) etUpi.setError("Required"); return; }

                try {
                    double amount = Double.parseDouble(amountStr);
                    double price = Double.parseDouble(priceStr);
                    double minLimit = minStr.isEmpty() ? 10 : Double.parseDouble(minStr);
                    
                    double tax = (amount * p2pCommission) / 100.0;
                    double totalNeeded = amount + tax;

                    if (totalNeeded > currentUnlockedBalance) {
                        if (etAmount != null) etAmount.setError("Insufficient balance (+ " + String.format(Locale.US, "%.2f", tax) + " Tax)");
                        return;
                    }

                    if (amount < minLimit) {
                        if (etMin != null) etMin.setError("Limit cannot exceed total amount");
                        return;
                    }

                    postAdvertisement(amount, price, minLimit, upi, dialog);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    private void postAdvertisement(double amount, double price, double minLimit, String upi, BottomSheetDialog dialog) {
        String adId = mDatabase.child("p2p_listings").push().getKey();
        if (adId == null) return;

        mDatabase.child("users").child(currentUserId).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) return Transaction.success(currentData);
                double available = getDouble(currentData.child("unlocked_balance").getValue());
                double listed = getDouble(currentData.child("p2p_listed_balance").getValue());
                
                double tax = (amount * p2pCommission) / 100.0;
                double totalDeduct = amount + tax;

                if (available < totalDeduct) return Transaction.abort();
                
                currentData.child("unlocked_balance").setValue(available - totalDeduct);
                currentData.child("p2p_listed_balance").setValue(listed + amount);
                return Transaction.success(currentData);
            }

            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    P2PListing ad = new P2PListing();
                    ad.id = adId;
                    ad.sellerUid = currentUserId;
                    ad.sellerName = currentUsername;
                    ad.totalAmount = amount;
                    ad.remainingAmount = amount;
                    ad.price = price;
                    ad.minLimit = minLimit;
                    ad.upiId = upi;
                    ad.timestamp = System.currentTimeMillis();
                    ad.status = "ACTIVE";

                    mDatabase.child("p2p_listings").child(adId).setValue(ad);
                    Toast.makeText(context, "Ad Posted Successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Failed to post ad: " + (error != null ? error.getMessage() : "Balance Error"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteAdConfirmation(P2PListing listing) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                .setTitle("Delete Advertisement?")
                .setMessage("Are you sure you want to remove this ad? Remaining " + String.format(Locale.US, "%.2f", listing.remainingAmount) + " USDT will be returned to your unlocked profit.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAdvertisement(listing))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAdvertisement(P2PListing listing) {
        mDatabase.child("p2p_listings").child(listing.id).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                P2PListing currentAd = currentData.getValue(P2PListing.class);
                if (currentAd == null || !currentAd.status.equals("ACTIVE")) return Transaction.abort();
                currentData.setValue(null); // Delete listing node
                return Transaction.success(currentData);
            }

            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    // Ad deleted, now refund the seller's user node balance
                    mDatabase.child("users").child(listing.sellerUid).runTransaction(new Transaction.Handler() {
                        @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData userSnap) {
                            if (userSnap.getValue() == null) return Transaction.success(userSnap);
                            double available = getDouble(userSnap.child("unlocked_balance").getValue());
                            double listed = getDouble(userSnap.child("p2p_listed_balance").getValue());
                            userSnap.child("unlocked_balance").setValue(available + listing.remainingAmount);
                            userSnap.child("p2p_listed_balance").setValue(listed - listing.remainingAmount);
                            return Transaction.success(userSnap);
                        }
                        @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {
                            if (c) Toast.makeText(context, "Ad Deleted & Balance Returned", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Failed to remove ad: " + (error != null ? error.getMessage() : "Ad not found or already deleted"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showBuyDialog(P2PListing listing) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_buy_p2p, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.tv_buy_title);
        TextView price = view.findViewById(R.id.tv_buy_price);
        TextView available = view.findViewById(R.id.tv_buy_available);
        TextInputEditText etAmount = view.findViewById(R.id.et_buy_amount);
        TextView tvReceive = view.findViewById(R.id.tv_buy_receive_amount);
        TextView tvTotal = view.findViewById(R.id.tv_buy_fiat_total);
        MaterialButton btnBuy = view.findViewById(R.id.btn_confirm_buy);

        if (tvReceive != null) {
            tvReceive.setText(String.format(Locale.US, "Service Fee: %.1f%%", p2pCommission));
        }

        if (title != null) title.setText("Buy USDT from " + listing.sellerName);
        if (price != null) price.setText(String.format(Locale.US, "₹%.2f", listing.price));
        if (available != null) available.setText(String.format(Locale.US, "%.2f USDT", listing.remainingAmount));
        
        if (etAmount != null) {
            etAmount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        String input = s.toString().trim();
                        if (input.isEmpty()) {
                            if (tvTotal != null) tvTotal.setText("Total: ₹0.00");
                            if (tvReceive != null) tvReceive.setText(String.format(Locale.US, "Service Fee: %.1f%%", p2pCommission));
                            return;
                        }
                        double amount = Double.parseDouble(input);
                        if (tvTotal != null) tvTotal.setText(String.format(Locale.US, "Total: ₹%.2f", amount * listing.price));
                        if (tvReceive != null) {
                            double receiveAmt = amount * (1 - p2pCommission / 100.0);
                            tvReceive.setText(String.format(Locale.US, "You will receive: %.2f USDT", receiveAmt));
                        }
                    } catch (Exception e) {
                        if (tvTotal != null) tvTotal.setText("Total: ₹0.00");
                        if (tvReceive != null) tvReceive.setText(String.format(Locale.US, "Service Fee: %.1f%%", p2pCommission));
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnBuy != null) {
            btnBuy.setOnClickListener(v -> {
                if (listing.sellerUid.equals(currentUserId)) {
                    Toast.makeText(context, "You cannot buy your own advertisement", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (etAmount == null) return;
                Editable text = etAmount.getText();
                String amountStr = (text != null) ? text.toString().trim() : "";
                
                if (amountStr.isEmpty()) {
                    etAmount.setError("Enter amount");
                    return;
                }
                
                try {
                    double buyAmount = Double.parseDouble(amountStr);

                    if (buyAmount < listing.minLimit) {
                        etAmount.setError("Min order is " + listing.minLimit);
                        return;
                    }
                    if (buyAmount > listing.remainingAmount) {
                        etAmount.setError("Max available: " + listing.remainingAmount);
                        return;
                    }

                    btnBuy.setEnabled(false);
                    btnBuy.setText("Checking Seller Status...");

                    checkSellerOnline(listing.sellerUid, isOnline -> {
                        initiateTrade(listing, buyAmount, isOnline, dialog);
                    });
                } catch (NumberFormatException e) {
                    etAmount.setError("Invalid number");
                }
            });
        }
        dialog.show();
    }

    private void checkSellerOnline(String sellerUid, OnlineCheckCallback callback) {
        mDatabase.child("users").child(sellerUid).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean online = snapshot.exists() && "online".equals(snapshot.getValue(String.class));
                callback.onResult(online);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onResult(false); }
        });
    }

    public interface OnlineCheckCallback {
        void onResult(boolean isOnline);
    }

    private void initiateTrade(P2PListing listing, double buyAmount, boolean sellerOnline, BottomSheetDialog dialog) {
        String orderId = mDatabase.child("ongoing_p2p_orders").push().getKey();
        if (orderId == null) return;

        mDatabase.child("p2p_listings").child(listing.id).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                P2PListing currentAd = currentData.getValue(P2PListing.class);
                if (currentAd == null || !currentAd.status.equals("ACTIVE") || currentAd.remainingAmount < buyAmount) return Transaction.abort();
                currentData.child("remainingAmount").setValue(currentAd.remainingAmount - buyAmount);
                if (currentAd.remainingAmount - buyAmount <= 0) currentData.child("status").setValue("COMPLETED");
                return Transaction.success(currentData);
            }

            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    P2POrder order = new P2POrder(orderId, listing.id, listing.sellerUid, listing.sellerName, currentUserId, currentUsername, buyAmount, listing.price, listing.upiId, p2pCommission);

                    // Always start with Waiting for Seller (Acceptance Phase)
                    order.status = "WAITING_FOR_SELLER";
                    // 5 Minute timer for seller to accept
                    order.expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ongoing_p2p_orders/" + orderId, order);
                    // Signal for notification service
                    updates.put("p2p_notifications/" + listing.sellerUid + "/" + orderId + "_waiting", true);

                    // Move balances on seller node
                    mDatabase.child("users").child(listing.sellerUid).runTransaction(new Transaction.Handler() {
                        @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData snap) {
                            if (snap.getValue() == null) return Transaction.success(snap);
                            double listed = getDouble(snap.child("p2p_listed_balance").getValue());
                            double escrow = getDouble(snap.child("p2p_escrow_balance").getValue());
                            snap.child("p2p_listed_balance").setValue(Math.max(0, listed - buyAmount));
                            snap.child("p2p_escrow_balance").setValue(escrow + buyAmount);
                            return Transaction.success(snap);
                        }
                        @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {
                            mDatabase.updateChildren(updates);
                        }
                    });

                    dialog.dismiss();
                    Toast.makeText(context, sellerOnline ? "Trade Initiated!" : "Seller is offline. Waiting 5 minutes...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Failed to initiate trade: " + (error != null ? error.getMessage() : "Ad no longer available"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void listenForActiveOrders() {
        mDatabase.child("ongoing_p2p_orders").addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                processOrderUpdate(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                processOrderUpdate(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String id = snapshot.getKey();
                if (id != null) {
                    if (id.equals(activeOrderId) && activeTradeDialog != null) { activeTradeDialog.dismiss(); activeOrderId = null; }
                    if (id.equals(activeReleaseOrderId) && activeReleaseDialog != null) { activeReleaseDialog.dismiss(); activeReleaseOrderId = null; }
                    if (id.equals(activeAcceptOrderId) && activeAcceptDialog != null) { activeAcceptDialog.dismiss(); activeAcceptOrderId = null; }
                }
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void processOrderUpdate(DataSnapshot snapshot) {
        P2POrder order = snapshot.getValue(P2POrder.class);
        if (order == null || order.id == null || order.status == null) return;

        boolean isBuyer = order.buyerUid.equals(currentUserId);
        boolean isSeller = order.sellerUid.equals(currentUserId);
        long now = System.currentTimeMillis();

        if (isBuyer) {
            // Check for completed orders to credit wallet locally
            if ("COMPLETED".equals(order.status) && notifiedOrders.add(order.id + "_settled_buyer")) {
                settleBuyerFunds(order);
                if (activeTradeDialog != null) activeTradeDialog.dismiss();
            }

            // Buyer session normally lasts 30 mins max
            if ((now - order.timestamp) > (30 * 60 * 1000)) return;

            if ("WAITING_FOR_SELLER".equals(order.status) || "PENDING".equals(order.status) || "PAID".equals(order.status)) {
                if (activeOrderId == null || activeOrderId.equals(order.id)) {
                    showTradeScreen(order);
                }
            } else if ("DISPUTED".equals(order.status) || "PAYMENT_MISSING".equals(order.status)) {
                if (activeTradeDialog != null && activeTradeDialog.isShowing() && order.id.equals(activeOrderId)) {
                    activeTradeDialog.dismiss();
                    activeOrderId = null;
                }
                if (notifiedOrders.add(order.id + "_" + order.status)) {
                    showBuyerDisputeScreen(order);
                }
            } else if ("CANCELLED".equals(order.status)) {
                if (activeTradeDialog != null && activeTradeDialog.isShowing() && order.id.equals(activeOrderId)) {
                    activeTradeDialog.dismiss();
                    activeOrderId = null;
                }
                showTradeDeclinedScreen(order);
            }
        } else if (isSeller) {
            if ("PAID".equals(order.status)) {
                // Auto-pop for trades in progress
                if (promptedOrders.add(order.id + "_release_pop") || order.id.equals(activeReleaseOrderId)) {
                    showReleaseScreen(order);
                }
            } else if ("WAITING_FOR_SELLER".equals(order.status)) {
                if (notifiedOrders.add(order.id + "_waiting")) {
                    NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_PROFIT, Math.abs(order.id.hashCode()), "P2P Trade Waiting", order.buyerName + " is waiting for you to accept a trade for " + order.amount + " USDT");
                }
                showSellerAcceptPrompt(order);
            }
 else if ("DISPUTED".equals(order.status)) {
                if (order.id.equals(activeReleaseOrderId) && activeReleaseDialog != null) {
                    activeReleaseDialog.dismiss();
                    activeReleaseOrderId = null;
                }
                if (notifiedOrders.add(order.id + "_disputed_seller")) {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                            .setTitle("Trade Disputed")
                            .setMessage("The buyer " + order.buyerName + " has filed a complaint for the trade of " + order.amount + " USDT. The assets are now locked in escrow for admin review.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            } else if ("CANCELLED".equals(order.status)) {
                if (notifiedOrders.add(order.id + "_cancelled_seller")) {
                    Toast.makeText(context, "Trade for " + String.format(Locale.US, "%.2f", order.amount) + " USDT was cancelled by the buyer.", Toast.LENGTH_LONG).show();
                }
                if (activeAcceptDialog != null && activeAcceptDialog.isShowing() && order.id.equals(activeAcceptOrderId)) {
                    activeAcceptDialog.dismiss();
                    activeAcceptOrderId = null;
                }
            }
        }
    }

    private void showSellerAcceptPrompt(P2POrder order) {
        if (((Activity)context).isFinishing() || !promptedOrders.add(order.id)) return;

        if (activeAcceptDialog != null && activeAcceptDialog.isShowing()) activeAcceptDialog.dismiss();

        activeAcceptOrderId = order.id;
        activeAcceptDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_seller_accept, null);
        activeAcceptDialog.setContentView(view);
        activeAcceptDialog.setCancelable(false);

        TextView tvMsg = view.findViewById(R.id.tv_seller_prompt_message);
        TextView tvFiat = view.findViewById(R.id.tv_seller_prompt_fiat);
        TextView tvCrypto = view.findViewById(R.id.tv_seller_prompt_crypto);
        MaterialButton btnAccept = view.findViewById(R.id.btn_seller_accept);
        MaterialButton btnIgnore = view.findViewById(R.id.btn_seller_ignore);

        tvMsg.setText(order.buyerName + " wants to buy USDT from you.");
        tvFiat.setText(String.format(Locale.US, "₹%.2f", order.amount * order.price));
        tvCrypto.setText(String.format(Locale.US, "%.2f USDT", order.amount));

        btnAccept.setOnClickListener(v -> {
            btnAccept.setEnabled(false);
            btnAccept.setText("Accepting...");
            Map<String, Object> updates = new HashMap<>();
            updates.put("ongoing_p2p_orders/" + order.id + "/status", "PENDING");
            // Set 15 Minute timer for Buyer to pay after acceptance
            long expiry = System.currentTimeMillis() + (15 * 60 * 1000);
            updates.put("ongoing_p2p_orders/" + order.id + "/expiryTime", expiry);

            // Notify Buyer that trade is accepted
            updates.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_accepted", true);
            mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> activeAcceptDialog.dismiss());
        });

        btnIgnore.setOnClickListener(v -> {
            activeAcceptDialog.dismiss();
            cancelOrder(order, "Declined by Seller");
        });

        activeAcceptDialog.show();
    }

    private void showTradeScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;

        if (activeTradeDialog != null && activeTradeDialog.isShowing() && order.id.equals(activeOrderId)) {
            TextView tvStatus = activeTradeDialog.findViewById(R.id.tv_trade_status);
            if (tvStatus != null) {
                String currentText = tvStatus.getText().toString();
                // If NOT transitioning from Waiting to Pending, just update the existing UI
                if (!(currentText.contains("Waiting") && "PENDING".equals(order.status))) {
                    updateTradeDialogUI(activeTradeDialog, order);
                    return;
                }
            }
        }

        if (activeTradeDialog != null && activeTradeDialog.isShowing()) activeTradeDialog.dismiss();

        activeOrderId = order.id;
        activeTradeDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_trade, null);
        activeTradeDialog.setContentView(view);
        activeTradeDialog.setCancelable(false);

        TextView tvFiat = view.findViewById(R.id.tv_trade_fiat_amount);
        TextView tvUpi = view.findViewById(R.id.tv_trade_upi_id);
        TextView tvTimer = view.findViewById(R.id.tv_trade_timer);
        TextView tvStatus = view.findViewById(R.id.tv_trade_status);
        View tilRef = view.findViewById(R.id.til_trade_ref);
        TextInputEditText etRef = view.findViewById(R.id.et_trade_ref);
        MaterialButton btnPaid = view.findViewById(R.id.btn_mark_as_paid);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_trade);
        MaterialButton btnComplaint = new MaterialButton(context);
        btnComplaint.setText("File Complaint");
        btnComplaint.setTag("btn_complaint");
        btnComplaint.setVisibility(View.GONE);
        ((ViewGroup)btnCancel.getParent()).addView(btnComplaint);

        tvFiat.setText(String.format(Locale.US, "₹%.2f", order.amount * order.price));
        tvUpi.setText(order.upiId);

        if ("WAITING_FOR_SELLER".equals(order.status)) {
            tvStatus.setText("Waiting for Seller to Accept...");
            tvUpi.setText("UPI Hidden");
            btnPaid.setVisibility(View.GONE);
            tilRef.setVisibility(View.GONE);
            view.findViewById(R.id.btn_copy_upi).setVisibility(View.GONE);
        } else if ("PENDING".equals(order.status)) {
            tvStatus.setText("Pay the Seller");
            btnPaid.setVisibility(View.VISIBLE);
            tilRef.setVisibility(View.VISIBLE);
            view.findViewById(R.id.btn_copy_upi).setVisibility(View.VISIBLE);
        } else if ("PAID".equals(order.status)) {
            tvStatus.setText("Payment Submitted");
            btnPaid.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            tilRef.setVisibility(View.GONE);
            tvTimer.setText("PAID");
            tvTimer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#22C55E")));
        }

        CountDownTimer tradeTimer = new CountDownTimer(Math.max(0, order.expiryTime - System.currentTimeMillis()), 1000) {
            public void onTick(long millisUntilFinished) {
                if ("PAID".equals(order.status) || "COMPLETED".equals(order.status)) {
                    cancel();
                    return;
                }
                long mins = (millisUntilFinished / 1000) / 60;
                long secs = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
            }
            public void onFinish() {
                if (!"PAID".equals(order.status) && !"COMPLETED".equals(order.status)) {
                    cancelOrder(order, "Trade Expired");
                    activeTradeDialog.dismiss();
                }
            }
        };
        tradeTimer.start();

        monitorOtherPresence(order, order.sellerUid, activeTradeDialog, btnComplaint, tradeTimer);

        btnPaid.setOnClickListener(v -> {
            String ref = etRef.getText().toString().trim();
            if (ref.isEmpty()) { Toast.makeText(context, "Enter Transaction ID", Toast.LENGTH_SHORT).show(); return; }
            btnPaid.setEnabled(false);
            btnPaid.setText("Updating...");
            Map<String, Object> updates = new HashMap<>();
            updates.put("ongoing_p2p_orders/" + order.id + "/status", "PAID");
            updates.put("ongoing_p2p_orders/" + order.id + "/transactionId", ref);

            // Notify seller of payment
            updates.put("p2p_notifications/" + order.sellerUid + "/" + order.id + "_paid", true);
            mDatabase.updateChildren(updates);
        });

        btnCancel.setOnClickListener(v -> cancelOrder(order, "Cancelled by Buyer"));
        btnComplaint.setOnClickListener(v -> fileDispute(order));

        view.findViewById(R.id.btn_copy_upi).setOnClickListener(v -> {
            if ("WAITING_FOR_SELLER".equals(order.status)) {
                Toast.makeText(context, "UPI ID is hidden until seller confirms", Toast.LENGTH_SHORT).show();
                return;
            }
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("UPI ID", order.upiId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "UPI ID Copied", Toast.LENGTH_SHORT).show();
        });

        activeTradeDialog.show();
    }

    private void updateTradeDialogUI(BottomSheetDialog dialog, P2POrder order) {
        TextView tvStatus = dialog.findViewById(R.id.tv_trade_status);
        TextView tvTimer = dialog.findViewById(R.id.tv_trade_timer);
        TextView tvUpi = dialog.findViewById(R.id.tv_trade_upi_id);
        View tilRef = dialog.findViewById(R.id.til_trade_ref);
        View btnPaid = dialog.findViewById(R.id.btn_mark_as_paid);
        View btnCancel = dialog.findViewById(R.id.btn_cancel_trade);
        View btnCopy = dialog.findViewById(R.id.btn_copy_upi);

        if ("WAITING_FOR_SELLER".equals(order.status)) {
            if (tvStatus != null) tvStatus.setText("Waiting for Seller to Accept...");
            if (tvUpi != null) tvUpi.setText("UPI Hidden");
            if (btnPaid != null) btnPaid.setVisibility(View.GONE);
            if (tilRef != null) tilRef.setVisibility(View.GONE);
            if (btnCopy != null) btnCopy.setVisibility(View.GONE);
        } else if ("PENDING".equals(order.status)) {
            if (tvStatus != null) tvStatus.setText("Pay the Seller");
            if (tvUpi != null) tvUpi.setText(order.upiId);
            if (btnPaid != null) btnPaid.setVisibility(View.VISIBLE);
            if (tilRef != null) tilRef.setVisibility(View.VISIBLE);
            if (btnCopy != null) btnCopy.setVisibility(View.VISIBLE);
        } else if ("PAID".equals(order.status)) {
            if (tvStatus != null) tvStatus.setText("Payment Submitted");
            if (btnPaid != null) btnPaid.setVisibility(View.GONE);
            if (btnCancel != null) btnCancel.setVisibility(View.GONE);
            if (tilRef != null) tilRef.setVisibility(View.GONE);
            if (tvTimer != null) {
                tvTimer.setText("PAID");
                tvTimer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#22C55E")));
            }
        }
    }

    private void monitorOtherPresence(P2POrder order, String otherUid, BottomSheetDialog dialog, View complaintBtn, CountDownTimer timer) {
        ValueEventListener old = activePresenceListeners.get(order.id);
        if (old != null) mDatabase.child("users").child(otherUid).child("status").removeEventListener(old);

        ValueEventListener listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                boolean isOffline = !"online".equals(snapshot.getValue(String.class));
                if (isOffline) {
                    mDatabase.child("ongoing_p2p_orders").child(order.id).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot orderSnap) {
                            String currentStatus = orderSnap.getValue(String.class);
                            boolean isBuyer = currentUserId.equals(order.buyerUid);
                            boolean isOtherSeller = otherUid.equals(order.sellerUid);

                            if (isBuyer && isOtherSeller && ("PAID".equals(currentStatus) || "PENDING".equals(currentStatus))) {
                                if (complaintBtn != null) {
                                    complaintBtn.setVisibility(View.VISIBLE);
                                    Toast.makeText(context, "Seller went offline. If you have paid, please file a complaint.", Toast.LENGTH_LONG).show();
                                }
                            } else if ("PENDING".equals(currentStatus) && !isBuyer) {
                                cancelOrder(order, "Party went offline");
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        mDatabase.child("users").child(otherUid).child("status").addValueEventListener(listener);
        activePresenceListeners.put(order.id, listener);
        dialog.setOnDismissListener(d -> {
            mDatabase.child("users").child(otherUid).child("status").removeEventListener(listener);
            activePresenceListeners.remove(order.id);
            if (timer != null) timer.cancel();
            if (dialog == activeTradeDialog) activeOrderId = null;
            if (dialog == activeReleaseDialog) activeReleaseOrderId = null;
        });
    }

    private void cancelOrder(P2POrder order, String reason) {
        mDatabase.child("ongoing_p2p_orders").child(order.id).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                P2POrder o = currentData.getValue(P2POrder.class);
                if (o == null || (!"PENDING".equals(o.status) && !"WAITING_FOR_SELLER".equals(o.status))) return Transaction.abort();
                currentData.child("status").setValue("CANCELLED");
                return Transaction.success(currentData);
            }

            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed && currentData != null) {
                    P2POrder fetchedOrder = currentData.getValue(P2POrder.class);
                    P2POrder finalOrder = (fetchedOrder != null) ? fetchedOrder : order;
                    finalOrder.status = "CANCELLED";

                    Map<String, Object> cleanup = new HashMap<>();
                    cleanup.put("ongoing_p2p_orders/" + order.id, null);

                    // Notify Buyer if declined by Seller
                    if ("Declined by Seller".equals(reason)) {
                        cleanup.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_declined", true);
                    } else if ("Cancelled by Buyer".equals(reason)) {
                        cleanup.put("p2p_notifications/" + order.sellerUid + "/" + order.id + "_cancelled", true);
                    }
                    mDatabase.updateChildren(cleanup);
                    logTradeHistory(finalOrder, "CANCELLED");

                    // Multi-path refund
                    mDatabase.child("p2p_listings").child(order.adId).runTransaction(new Transaction.Handler() {
                        @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData snap) {
                            P2PListing ad = snap.getValue(P2PListing.class);
                            if (ad != null) {
                                snap.child("remainingAmount").setValue(ad.remainingAmount + order.amount);
                                snap.child("status").setValue("ACTIVE");
                            }
                            return Transaction.success(snap);
                        }
                        @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {}
                    });

                    mDatabase.child("users").child(order.sellerUid).runTransaction(new Transaction.Handler() {
                        @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData snap) {
                            if (snap.getValue() == null) return Transaction.success(snap);
                            double listed = getDouble(snap.child("p2p_listed_balance").getValue());
                            double escrow = getDouble(snap.child("p2p_escrow_balance").getValue());
                            snap.child("p2p_listed_balance").setValue(listed + order.amount);
                            snap.child("p2p_escrow_balance").setValue(Math.max(0, escrow - order.amount));
                            return Transaction.success(snap);
                        }
                        @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {}
                    });

                    Toast.makeText(context, "Order Cancelled: " + reason, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fileDispute(P2POrder order) {
        if (currentUserId.equals(order.buyerUid) && (order.transactionId == null || order.transactionId.trim().isEmpty())) {
            // Try to pull from EditText if currently open
            if (activeTradeDialog != null && activeTradeDialog.isShowing()) {
                TextInputEditText etRef = activeTradeDialog.findViewById(R.id.et_trade_ref);
                if (etRef != null && etRef.getText() != null) {
                    String ref = etRef.getText().toString().trim();
                    if (!ref.isEmpty()) order.transactionId = ref;
                }
            }

            if (order.transactionId == null || order.transactionId.trim().isEmpty()) {
                final TextInputEditText input = new TextInputEditText(context);
                input.setHint("Transaction ID / Ref No");
                int p = (int) (24 * context.getResources().getDisplayMetrics().density);
                android.widget.FrameLayout container = new android.widget.FrameLayout(context);
                container.setPadding(p, p/2, p, 0);
                container.addView(input);

                new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                        .setTitle("Payment Proof Required")
                        .setMessage("Please enter the Transaction ID/Ref No for the payment you made.")
                        .setView(container)
                        .setPositiveButton("File Complaint", (dialog, which) -> {
                            String ref = input.getText() != null ? input.getText().toString().trim() : "";
                            if (ref.isEmpty()) {
                                Toast.makeText(context, "Transaction ID is required to file a complaint", Toast.LENGTH_SHORT).show();
                            } else {
                                order.transactionId = ref;
                                executeFileDispute(order);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }
        }
        executeFileDispute(order);
    }

    private void executeFileDispute(P2POrder order) {
        order.status = "DISPUTED";
        Map<String, Object> updates = new HashMap<>();
        updates.put("ongoing_p2p_orders/" + order.id, null);

        // Notify other party
        if (currentUserId.equals(order.sellerUid)) {
            updates.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_disputed", true);
        } else {
            updates.put("p2p_notifications/" + order.sellerUid + "/" + order.id + "_disputed", true);
        }

        mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> {
            logTradeHistory(order, "DISPUTED");
            Toast.makeText(context, "Complaint filed. Admins will review the transaction proof.", Toast.LENGTH_LONG).show();
            if (activeReleaseDialog != null) activeReleaseDialog.dismiss();
            if (activeTradeDialog != null) activeTradeDialog.dismiss();
        });
    }

    private void logTradeHistory(P2POrder order, String status) {
        long ts = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(new java.util.Date(ts));
        String amtStr = String.format(Locale.US, "%.2f", order.amount);
        Map<String, Object> updates = new HashMap<>();

        // Log for Buyer
        String bTxId = mDatabase.child("users").child(order.buyerUid).child("transactions").child("p2p").push().getKey();
        if (bTxId != null) {
            double fee = (order.amount * order.commission) / 100.0;
            double netAmount = order.amount - fee;
            
            Map<String, Object> tx = new HashMap<>();
            tx.put("id", bTxId);
            tx.put("title", "P2P Buy: " + order.sellerName);
            tx.put("subtitle", date + " • " + status + (order.commission > 0 ? " (incl. " + order.commission + "% fee)" : ""));
            tx.put("amount", "+$" + String.format(Locale.US, "%.2f", netAmount));
            tx.put("status", "Success".equals(status) || "COMPLETED".equals(status) ? "Success" : status);
            tx.put("type", "p2p");
            tx.put("timestamp", ts);
            updates.put("users/" + order.buyerUid + "/transactions/p2p/" + bTxId, tx);
        }

        // Log for Seller
        String sTxId = mDatabase.child("users").child(order.sellerUid).child("transactions").child("p2p").push().getKey();
        if (sTxId != null) {
            Map<String, Object> tx = new HashMap<>();
            tx.put("id", sTxId);
            tx.put("title", "P2P Sell: " + order.buyerName);
            tx.put("subtitle", date + " • " + status);
            tx.put("amount", "-$" + amtStr);
            tx.put("status", "Success".equals(status) || "COMPLETED".equals(status) ? "Success" : status);
            tx.put("type", "p2p");
            tx.put("timestamp", ts);
            updates.put("users/" + order.sellerUid + "/transactions/p2p/" + sTxId, tx);
        }

        // Global history for admin
        updates.put("p2p_order_history/" + order.id, order);
        mDatabase.updateChildren(updates);
    }

    private void showBuyerDisputeScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_buyer_dispute, null);
        dialog.setContentView(view);

        TextView tvMsg = view.findViewById(R.id.tv_dispute_msg);
        if ("PAYMENT_MISSING".equals(order.status)) {
            if (tvMsg != null) tvMsg.setText("The seller claims they haven't received your payment yet. If you have already paid, please file a dispute with your transaction proof.");
        }

        MaterialButton btnComplaint = view.findViewById(R.id.btn_buyer_file_complaint);
        MaterialButton btnClose = view.findViewById(R.id.btn_buyer_close_dispute);

        btnComplaint.setOnClickListener(v -> {
            fileDispute(order);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showTradeDeclinedScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_buyer_dispute, null);
        dialog.setContentView(view);

        TextView tvHeader = view.findViewById(android.R.id.text1);
        if (tvHeader != null) {
            tvHeader.setText("Trade Declined");
        } else {
            TextView tvHeaderTag = view.findViewWithTag("title");
            if (tvHeaderTag != null) tvHeaderTag.setText("Trade Declined");
        }

        TextView msg = view.findViewById(R.id.tv_dispute_msg);
        if (msg != null) msg.setText("The seller " + order.sellerName + " has declined your trade request for " + order.amount + " USDT.");

        MaterialButton btnAction = view.findViewById(R.id.btn_buyer_file_complaint);
        if (btnAction != null) {
            btnAction.setText("Ok, Understood");
            btnAction.setOnClickListener(v -> dialog.dismiss());
        }

        View btnClose = view.findViewById(R.id.btn_buyer_close_dispute);
        if (btnClose != null) btnClose.setVisibility(View.GONE);

        dialog.show();
    }

    private void showReleaseScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;
        if (activeReleaseDialog != null && activeReleaseDialog.isShowing() && order.id.equals(activeReleaseOrderId)) return;
        if (activeReleaseDialog != null && activeReleaseDialog.isShowing()) activeReleaseDialog.dismiss();

        activeReleaseOrderId = order.id;
        activeReleaseDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_release, null);
        activeReleaseDialog.setContentView(view);
        activeReleaseDialog.setCancelable(false);

        TextView tvFiat = view.findViewById(R.id.tv_release_fiat_amount);
        TextView tvRef = view.findViewById(R.id.tv_release_ref);
        MaterialButton btnRelease = view.findViewById(R.id.btn_confirm_release);
        MaterialButton btnDispute = view.findViewById(R.id.btn_file_dispute);

        tvFiat.setText(String.format(Locale.US, "₹%.2f", order.amount * order.price));
        tvRef.setText("Ref: " + order.transactionId);

        monitorOtherPresence(order, order.buyerUid, activeReleaseDialog, null, null);

        btnRelease.setOnClickListener(v -> {
            btnRelease.setEnabled(false);
            btnRelease.setText("Releasing...");

            mDatabase.child("ongoing_p2p_orders").child(order.id).runTransaction(new Transaction.Handler() {
                @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    P2POrder currentOrder = currentData.getValue(P2POrder.class);
                    if (currentOrder == null || !"PAID".equals(currentOrder.status)) return Transaction.abort();
                    currentData.child("status").setValue("COMPLETED");
                    return Transaction.success(currentData);
                }

                @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (committed && currentData != null) {
                        P2POrder fetchedOrder = currentData.getValue(P2POrder.class);
                        P2POrder finalOrder = (fetchedOrder != null) ? fetchedOrder : order;
                        finalOrder.status = "COMPLETED";

                        Map<String, Object> cleanup = new HashMap<>();
                        cleanup.put("ongoing_p2p_orders/" + order.id, null);

                        settleSellerFunds(order);
                        // Notify Buyer that USDT is released
                        cleanup.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_released", true);
                        mDatabase.updateChildren(cleanup);
                        logTradeHistory(finalOrder, "COMPLETED");

                        Toast.makeText(context, "USDT Released Successfully!", Toast.LENGTH_SHORT).show();
                        if (activeReleaseDialog != null) activeReleaseDialog.dismiss();
                    } else {
                        btnRelease.setEnabled(true);
                        btnRelease.setText("Confirm & Release USDT");
                        Toast.makeText(context, "Release failed: Trade already processed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        btnDispute.setOnClickListener(v -> {
            btnDispute.setEnabled(false);
            btnDispute.setText("Notifying Buyer...");

            Map<String, Object> updates = new HashMap<>();
            updates.put("ongoing_p2p_orders/" + order.id + "/status", "PAYMENT_MISSING");
            updates.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_missing", true);

            mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Buyer notified of missing payment", Toast.LENGTH_SHORT).show();
                if (activeReleaseDialog != null) activeReleaseDialog.dismiss();
            }).addOnFailureListener(e -> {
                btnDispute.setEnabled(true);
                btnDispute.setText("Payment Not Received");
            });
        });

        activeReleaseDialog.show();
    }

    private void settleSellerFunds(P2POrder order) {
        // Atomic Settlement for Seller - Only touches seller's own node
        mDatabase.child("users").child(order.sellerUid).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData snap) {
                if (snap.getValue() == null) return Transaction.success(snap);
                double escrow = getDouble(snap.child("p2p_escrow_balance").getValue());
                double wallet = getDouble(snap.child("wallet_balance").getValue());
                snap.child("p2p_escrow_balance").setValue(Math.max(0, escrow - order.amount));
                snap.child("wallet_balance").setValue(Math.max(0, wallet - order.amount));
                return Transaction.success(snap);
            }
            @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {}
        });
    }

    private void settleBuyerFunds(P2POrder order) {
        // Atomic Settlement for Buyer - Only touches buyer's own node (called by buyer's app)
        mDatabase.child("users").child(order.buyerUid).runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData snap) {
                if (snap.getValue() == null) return Transaction.success(snap);
                double available = getDouble(snap.child("unlocked_balance").getValue());
                double wallet = getDouble(snap.child("wallet_balance").getValue());
                
                double commissionCut = (order.amount * order.commission) / 100.0;
                double finalAmount = order.amount - commissionCut;

                snap.child("unlocked_balance").setValue(available + finalAmount);
                snap.child("wallet_balance").setValue(wallet + finalAmount);
                return Transaction.success(snap);
            }
            @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {}
        });
    }

    private void setupDisputeMonitoring(View alertCard, TextView tvCount, View btnOpen) {
        if (alertCard == null) return;

        mDatabase.child("p2p_order_history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<P2POrder> disputedTrades = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    P2POrder order = ds.getValue(P2POrder.class);
                    if (order != null && "DISPUTED".equals(order.status)) {
                        if (order.buyerUid.equals(currentUserId) || order.sellerUid.equals(currentUserId)) {
                            disputedTrades.add(order);
                        }
                    }
                }

                if (disputedTrades.isEmpty()) {
                    alertCard.setVisibility(View.GONE);
                } else {
                    alertCard.setVisibility(View.VISIBLE);
                    tvCount.setText(disputedTrades.size() + (disputedTrades.size() == 1 ? " Active Dispute" : " Active Disputes"));
                    btnOpen.setOnClickListener(v -> showDisputeListDialog(disputedTrades));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showDisputeListDialog(List<P2POrder> disputedTrades) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_dispute_list, null);
        dialog.setContentView(view);

        RecyclerView rv = view.findViewById(R.id.rv_disputed_trades);
        rv.setLayoutManager(new LinearLayoutManager(context));
        
        class DisputeAdapter extends RecyclerView.Adapter<DisputeAdapter.ViewHolder> {
            @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
                return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_p2p_dispute_simple, p, false));
            }
            @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
                P2POrder order = disputedTrades.get(pos);
                h.title.setText("Trade #" + order.id.substring(0, Math.min(order.id.length(), 8)));
                h.subtitle.setText(String.format(Locale.US, "Amount: $%.2f | %s", order.amount, currentUserId.equals(order.buyerUid) ? "Seller: " + order.sellerName : "Buyer: " + order.buyerName));
                h.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, P2PChatActivity.class);
                    intent.putExtra("orderId", order.id);
                    context.startActivity(intent);
                    dialog.dismiss();
                });
            }
            @Override public int getItemCount() { return disputedTrades.size(); }
            class ViewHolder extends RecyclerView.ViewHolder {
                TextView title, subtitle;
                ViewHolder(View v) { super(v); title = v.findViewById(R.id.tv_dispute_title); subtitle = v.findViewById(R.id.tv_dispute_subtitle); }
            }
        }

        rv.setAdapter(new DisputeAdapter());
        dialog.show();
    }

    private void setupDirectTransfer(View rootView) {
        TextView tvAvailable = rootView.findViewById(R.id.tv_p2p_section_available);
        TextInputEditText etReceiverUid = rootView.findViewById(R.id.et_section_receiver_uid);
        TextInputEditText etAmount = rootView.findViewById(R.id.et_section_transfer_amount);
        MaterialButton btnTransfer = rootView.findViewById(R.id.btn_section_p2p_transfer);
        MaterialButton btnVerify = rootView.findViewById(R.id.btn_verify_uid);
        View cvPreview = rootView.findViewById(R.id.cv_recipient_preview);
        TextView tvVerifiedName = rootView.findViewById(R.id.tv_verified_name);
        TextView btnMax = rootView.findViewById(R.id.tv_btn_max);
        View chip10 = rootView.findViewById(R.id.chip_10);
        View chip50 = rootView.findViewById(R.id.chip_50);
        View chip100 = rootView.findViewById(R.id.chip_100);
        ImageView btnRefresh = rootView.findViewById(R.id.btn_refresh_p2p);

        etReceiverUid.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isUidVerified = false;
                if (btnTransfer != null) btnTransfer.setEnabled(false);
                if (cvPreview != null) cvPreview.setVisibility(View.GONE);
                if (btnVerify != null) { btnVerify.setText("Verify UID"); btnVerify.setEnabled(true); }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        UserHandler.getInstance().listenToUserData(currentUserId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUnlockedBalance = getDouble(snapshot.child("unlocked_balance").getValue());
                    if (tvAvailable != null) tvAvailable.setText(String.format(Locale.US, "$%.2f", currentUnlockedBalance));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnVerify != null) {
            btnVerify.setOnClickListener(v -> {
                String uid = etReceiverUid.getText().toString().trim();
                if (uid.isEmpty()) return;
                btnVerify.setEnabled(false);
                btnVerify.setText("Verifying...");
                mDatabase.child("users").child(uid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isUidVerified = true;
                            btnVerify.setText("Verified");
                            if (tvVerifiedName != null) tvVerifiedName.setText("Recipient: " + snapshot.getValue(String.class));
                            if (cvPreview != null) cvPreview.setVisibility(View.VISIBLE);
                            if (btnTransfer != null) btnTransfer.setEnabled(true);
                        } else {
                            btnVerify.setText("Invalid UID");
                            btnVerify.setEnabled(true);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { btnVerify.setEnabled(true); }
                });
            });
        }

        if (btnMax != null) btnMax.setOnClickListener(v -> etAmount.setText(String.format(Locale.US, "%.2f", currentUnlockedBalance)));
        if (chip10 != null) chip10.setOnClickListener(v -> etAmount.setText("10"));
        if (chip50 != null) chip50.setOnClickListener(v -> etAmount.setText("50"));
        if (chip100 != null) chip100.setOnClickListener(v -> etAmount.setText("100"));
        if (btnRefresh != null) btnRefresh.setOnClickListener(v -> fetchP2PHistory(rootView));

        if (btnTransfer != null) {
            btnTransfer.setOnClickListener(v -> {
                if (!isUidVerified) { Toast.makeText(context, "Verify UID first", Toast.LENGTH_SHORT).show(); return; }
                String amtStr = etAmount.getText().toString().trim();
                if (amtStr.isEmpty()) return;
                double amount = Double.parseDouble(amtStr);

                UserHandler.getInstance().getUserDataFresh(currentUserId, new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUnlockedBalance = getDouble(snapshot.child("unlocked_balance").getValue());
                        if (amount > currentUnlockedBalance) {
                            Toast.makeText(context, "Insufficient unlocked profit", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        performSecureDirectTransfer(etReceiverUid.getText().toString().trim(), amount, etReceiverUid, etAmount, cvPreview, btnVerify, btnTransfer);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }

        fetchP2PHistory(rootView);
        setupRecentRecipients(rootView, etReceiverUid, btnVerify);
    }

    private void setupRecentRecipients(View rootView, TextInputEditText etUid, MaterialButton btnVerify) {
        RecyclerView rvRecent = rootView.findViewById(R.id.rv_recent_p2p);
        View container = rootView.findViewById(R.id.ll_recent_p2p_container);
        if (rvRecent == null) return;

        List<Map<String, String>> recentList = new ArrayList<>();
        mDatabase.child("users").child(currentUserId).child("transactions").child("p2p").limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recentList.clear();
                        Set<String> addedUids = new HashSet<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String title = ds.child("title").getValue(String.class);
                            String subtitle = ds.child("subtitle").getValue(String.class);
                            if (title != null && title.contains("Sent to") && subtitle != null && subtitle.contains("UID: ")) {
                                String name = title.replace("P2P Sent to ", "").replace("Direct ", "");
                                String uid = subtitle.substring(subtitle.indexOf("UID: ") + 5).trim();
                                if (!uid.isEmpty() && !addedUids.contains(uid)) {
                                    Map<String, String> item = new HashMap<>();
                                    item.put("name", name);
                                    item.put("uid", uid);
                                    recentList.add(item);
                                    addedUids.add(uid);
                                }
                            }
                        }
                        if (!recentList.isEmpty()) {
                            if (container != null) container.setVisibility(View.VISIBLE);
                            Collections.reverse(recentList);
                            rvRecent.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                            rvRecent.setAdapter(new RecyclerView.Adapter<RecentViewHolder>() {
                                @NonNull @Override public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
                                    return new RecentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recent_p2p, p, false));
                                }
                                @Override public void onBindViewHolder(@NonNull RecentViewHolder h, int pos) {
                                    Map<String, String> item = recentList.get(pos);
                                    h.name.setText(item.get("name"));
                                    h.itemView.setOnClickListener(v -> {
                                        etUid.setText(item.get("uid"));
                                        if (btnVerify != null) btnVerify.performClick();
                                    });
                                }
                                @Override public int getItemCount() { return Math.min(recentList.size(), 8); }
                            });
                        } else {
                            if (container != null) container.setVisibility(View.GONE);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private static class RecentViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        RecentViewHolder(View v) { super(v); name = v.findViewById(R.id.tv_recent_name); }
    }

    private void fetchP2PHistory(View rootView) {
        RecyclerView rvHistory = rootView.findViewById(R.id.rv_p2p_history);
        TextView tvEmpty = rootView.findViewById(R.id.tv_empty_history);
        if (rvHistory == null) return;
        List<com.ascend.invest.handlers.Transaction> list = new ArrayList<>();
        TransactionAdapter adapter = new TransactionAdapter(list);
        rvHistory.setLayoutManager(new LinearLayoutManager(context));
        rvHistory.setAdapter(adapter);
        mDatabase.child("users").child(currentUserId).child("transactions").child("p2p").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    com.ascend.invest.handlers.Transaction t = ds.getValue(com.ascend.invest.handlers.Transaction.class);
                    if (t != null) list.add(t);
                }
                if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                Collections.sort(list, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void performSecureDirectTransfer(String receiverUid, double amount, TextInputEditText etUid, TextInputEditText etAmt, View cvPreview, MaterialButton btnVerify, MaterialButton btnTransfer) {
        mDatabase.child("users").child(receiverUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String receiverName = snapshot.getValue(String.class);

                mDatabase.child("users").child(currentUserId).runTransaction(new Transaction.Handler() {
                    @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        if (currentData.getValue() == null) return Transaction.success(currentData);
                        double senderUnlocked = getDouble(currentData.child("unlocked_balance").getValue());
                        double senderWallet = getDouble(currentData.child("wallet_balance").getValue());
                        if (senderUnlocked < amount) return Transaction.abort();
                        currentData.child("unlocked_balance").setValue(senderUnlocked - amount);
                        currentData.child("wallet_balance").setValue(senderWallet - amount);
                        return Transaction.success(currentData);
                    }
                    @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        if (committed) {
                            mDatabase.child("users").child(receiverUid).runTransaction(new Transaction.Handler() {
                                @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData snap) {
                                    if (snap.getValue() == null) return Transaction.success(snap);
                                    double receiverUnlocked = getDouble(snap.child("unlocked_balance").getValue());
                                    double receiverWallet = getDouble(snap.child("wallet_balance").getValue());
                                    snap.child("unlocked_balance").setValue(receiverUnlocked + amount);
                                    snap.child("wallet_balance").setValue(receiverWallet + amount);
                                    return Transaction.success(snap);
                                }
                                @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {
                                    logTransfer(receiverUid, receiverName, amount);
                                    Toast.makeText(context, "Transfer Successful!", Toast.LENGTH_SHORT).show();
                                    if (etUid != null) etUid.setText("");
                                    if (etAmt != null) etAmt.setText("");
                                    if (cvPreview != null) cvPreview.setVisibility(View.GONE);
                                    if (btnVerify != null) { btnVerify.setText("Verify UID"); btnVerify.setEnabled(true); }
                                    if (btnTransfer != null) btnTransfer.setEnabled(false);
                                }
                            });
                        } else {
                            Toast.makeText(context, "Transfer failed: Insufficient Balance", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void logTransfer(String receiverUid, String receiverName, double amount) {
        long ts = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(new java.util.Date(ts));
        String amtStr = String.format(Locale.US, "%.2f", amount);
        Map<String, Object> updates = new HashMap<>();
        String sTxId = mDatabase.child("users").child(currentUserId).child("transactions").child("p2p").push().getKey();
        if (sTxId != null) {
            Map<String, Object> tx = new HashMap<>();
            tx.put("id", sTxId); tx.put("title", "Sent to " + receiverName); tx.put("subtitle", date + " • UID: " + receiverUid);
            tx.put("amount", "-$" + amtStr); tx.put("status", "Success"); tx.put("type", "p2p"); tx.put("timestamp", ts);
            updates.put("users/" + currentUserId + "/transactions/p2p/" + sTxId, tx);
        }
        String rTxId = mDatabase.child("users").child(receiverUid).child("transactions").child("p2p").push().getKey();
        if (rTxId != null) {
            Map<String, Object> tx = new HashMap<>();
            tx.put("id", rTxId); tx.put("title", "Received from Source"); tx.put("subtitle", date + " • From Internal Source");
            tx.put("amount", "+$" + amtStr); tx.put("status", "Success"); tx.put("type", "p2p"); tx.put("timestamp", ts);
            updates.put("users/" + receiverUid + "/transactions/p2p/" + rTxId, tx);
        }
        mDatabase.updateChildren(updates);
    }

    private double getDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) { try { return Double.parseDouble((String) val); } catch (Exception ignored) {} }
        return 0.0;
    }
}
