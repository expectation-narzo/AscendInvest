package com.ascend.invest.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.ascend.invest.databinding.DialogBuyP2pBinding;
import com.ascend.invest.databinding.DialogP2pBuyerDisputeBinding;
import com.ascend.invest.databinding.DialogP2pDisputeListBinding;
import com.ascend.invest.databinding.DialogP2pReleaseBinding;
import com.ascend.invest.databinding.DialogP2pSellerAcceptBinding;
import com.ascend.invest.databinding.DialogP2pTradeBinding;
import com.ascend.invest.databinding.DialogPostP2pAdBinding;
import com.ascend.invest.databinding.ItemP2pDisputeSimpleBinding;
import com.ascend.invest.databinding.ItemRecentP2pBinding;
import com.ascend.invest.databinding.SectionP2pBinding;
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
    private final SectionP2pBinding binding;
    private String currentUsername = "User";

    private double currentUnlockedBalance = 0.0;
    private double p2pCommission = 0.0;
    private boolean isUidVerified = false;

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

    public P2PHandler(Context context, String currentUserId, SectionP2pBinding binding) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.binding = binding;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        fetchCurrentUsername();
        fetchP2PCommission();
        listenForActiveOrders();
    }

    private void fetchP2PCommission() {
        mDatabase.child("commission").child("p2p").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) p2pCommission = getDouble(snapshot.getValue());
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

    public void setupP2PSection() {
        binding.tabP2pMarket.setOnClickListener(v -> {
            binding.tabP2pMarket.setBackgroundResource(R.drawable.menu_item_bg);
            binding.tabP2pMarket.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_purple));
            binding.tabP2pMarket.setTextColor(Color.parseColor("#000000"));
            binding.tabP2pMyAds.setBackground(null);
            binding.tabP2pMyAds.setTextColor(Color.parseColor("#64748B"));
            binding.tabP2pDirect.setBackground(null);
            binding.tabP2pDirect.setTextColor(Color.parseColor("#64748B"));
            binding.llP2pMarketContainer.setVisibility(View.VISIBLE);
            binding.llP2pMyAdsContainer.setVisibility(View.GONE);
            binding.llP2pDirectContainer.setVisibility(View.GONE);
            if (binding.btnPostP2pAd != null) binding.btnPostP2pAd.setVisibility(View.VISIBLE);
        });

        binding.tabP2pMyAds.setOnClickListener(v -> {
            binding.tabP2pMyAds.setBackgroundResource(R.drawable.menu_item_bg);
            binding.tabP2pMyAds.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_purple));
            binding.tabP2pMyAds.setTextColor(Color.parseColor("#000000"));
            binding.tabP2pMarket.setBackground(null);
            binding.tabP2pMarket.setTextColor(Color.parseColor("#64748B"));
            binding.tabP2pDirect.setBackground(null);
            binding.tabP2pDirect.setTextColor(Color.parseColor("#64748B"));
            binding.llP2pMyAdsContainer.setVisibility(View.VISIBLE);
            binding.llP2pMarketContainer.setVisibility(View.GONE);
            binding.llP2pDirectContainer.setVisibility(View.GONE);
            if (binding.btnPostP2pAd != null) binding.btnPostP2pAd.setVisibility(View.VISIBLE);
        });

        binding.tabP2pDirect.setOnClickListener(v -> {
            binding.tabP2pDirect.setBackgroundResource(R.drawable.menu_item_bg);
            binding.tabP2pDirect.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_purple));
            binding.tabP2pDirect.setTextColor(Color.parseColor("#000000"));
            binding.tabP2pMarket.setBackground(null);
            binding.tabP2pMarket.setTextColor(Color.parseColor("#64748B"));
            binding.tabP2pMyAds.setBackground(null);
            binding.tabP2pMyAds.setTextColor(Color.parseColor("#64748B"));
            binding.llP2pDirectContainer.setVisibility(View.VISIBLE);
            binding.llP2pMarketContainer.setVisibility(View.GONE);
            binding.llP2pMyAdsContainer.setVisibility(View.GONE);
            if (binding.btnPostP2pAd != null) binding.btnPostP2pAd.setVisibility(View.GONE);
        });

        if (binding.rvP2pMarketplace != null) {
            binding.rvP2pMarketplace.setLayoutManager(new LinearLayoutManager(context));
            marketAdapter = new P2PMarketAdapter(marketListings, currentUserId, (listing, isDelete) -> {
                if (isDelete) showDeleteAdConfirmation(listing);
                else showBuyDialog(listing);
            });
            binding.rvP2pMarketplace.setAdapter(marketAdapter);
            fetchMarketListings();
        }

        if (binding.rvP2pMyAds != null) {
            binding.rvP2pMyAds.setLayoutManager(new LinearLayoutManager(context));
            myAdsAdapter = new P2PMarketAdapter(myListings, currentUserId, (listing, isDelete) -> {
                if (isDelete) showDeleteAdConfirmation(listing);
                else showBuyDialog(listing);
            });
            binding.rvP2pMyAds.setAdapter(myAdsAdapter);
            fetchMyAds();
        }

        if (binding.btnPostP2pAd != null) binding.btnPostP2pAd.setOnClickListener(v -> showPostAdDialog());

        setupDisputeMonitoring();
        setupDirectTransfer();

        binding.tabP2pMarket.performClick();
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
        DialogPostP2pAdBinding dBinding = DialogPostP2pAdBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dBinding.getRoot());

        if (dBinding.tvPostAdFee != null) {
            dBinding.tvPostAdFee.setText(String.format(Locale.US, "Listing Tax: %.1f%%", p2pCommission));
        }

        dBinding.etAdAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double amt = Double.parseDouble(s.toString());
                    if (dBinding.tvPostAdFee != null) {
                        double feeAmt = (amt * p2pCommission) / 100.0;
                        dBinding.tvPostAdFee.setText(String.format(Locale.US, "Listing Tax: %.1f%% ($%.2f)", p2pCommission, feeAmt));
                    }
                } catch (Exception e) {
                    if (dBinding.tvPostAdFee != null) dBinding.tvPostAdFee.setText(String.format(Locale.US, "Listing Tax: %.1f%%", p2pCommission));
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        dBinding.btnConfirmPostAd.setOnClickListener(v -> {
            String amountStr = dBinding.etAdAmount.getText() != null ? dBinding.etAdAmount.getText().toString().trim() : "";
            String priceStr = dBinding.etAdPrice.getText() != null ? dBinding.etAdPrice.getText().toString().trim() : "";
            String minStr = dBinding.etAdMinLimit.getText() != null ? dBinding.etAdMinLimit.getText().toString().trim() : "";
            String upi = dBinding.etAdUpi.getText() != null ? dBinding.etAdUpi.getText().toString().trim() : "";

            if (amountStr.isEmpty()) { dBinding.etAdAmount.setError("Required"); return; }
            if (priceStr.isEmpty()) { dBinding.etAdPrice.setError("Required"); return; }
            if (upi.isEmpty()) { dBinding.etAdUpi.setError("Required"); return; }

            try {
                double amount = Double.parseDouble(amountStr);
                double price = Double.parseDouble(priceStr);
                double minLimit = minStr.isEmpty() ? 10 : Double.parseDouble(minStr);
                
                double tax = (amount * p2pCommission) / 100.0;
                double totalNeeded = amount + tax;

                if (totalNeeded > currentUnlockedBalance) {
                    dBinding.etAdAmount.setError("Insufficient balance (+ " + String.format(Locale.US, "%.2f", tax) + " Tax)");
                    return;
                }

                if (amount < minLimit) {
                    dBinding.etAdMinLimit.setError("Limit cannot exceed total amount");
                    return;
                }

                postAdvertisement(amount, price, minLimit, upi, dialog);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

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
                    ad.id = adId; ad.sellerUid = currentUserId; ad.sellerName = currentUsername;
                    ad.totalAmount = amount; ad.remainingAmount = amount; ad.price = price;
                    ad.minLimit = minLimit; ad.upiId = upi; ad.timestamp = System.currentTimeMillis();
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
                currentData.setValue(null);
                return Transaction.success(currentData);
            }
            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
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
        DialogBuyP2pBinding dBinding = DialogBuyP2pBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dBinding.getRoot());

        if (dBinding.tvBuyReceiveAmount != null) dBinding.tvBuyReceiveAmount.setText(String.format(Locale.US, "Service Fee: %.1f%%", p2pCommission));
        if (dBinding.tvBuyTitle != null) dBinding.tvBuyTitle.setText("Buy USDT from " + listing.sellerName);
        if (dBinding.tvBuyPrice != null) dBinding.tvBuyPrice.setText(String.format(Locale.US, "₹%.2f", listing.price));
        if (dBinding.tvBuyAvailable != null) dBinding.tvBuyAvailable.setText(String.format(Locale.US, "%.2f USDT", listing.remainingAmount));
        
        dBinding.etBuyAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String input = s.toString().trim();
                    if (input.isEmpty()) {
                        if (dBinding.tvBuyFiatTotal != null) dBinding.tvBuyFiatTotal.setText("Total: ₹0.00");
                        if (dBinding.tvBuyReceiveAmount != null) dBinding.tvBuyReceiveAmount.setText(String.format(Locale.US, "Service Fee: %.1f%%", p2pCommission));
                        return;
                    }
                    double amount = Double.parseDouble(input);
                    if (dBinding.tvBuyFiatTotal != null) dBinding.tvBuyFiatTotal.setText(String.format(Locale.US, "Total: ₹%.2f", amount * listing.price));
                    if (dBinding.tvBuyReceiveAmount != null) {
                        double receiveAmt = amount * (1 - p2pCommission / 100.0);
                        dBinding.tvBuyReceiveAmount.setText(String.format(Locale.US, "You will receive: %.2f USDT", receiveAmt));
                    }
                } catch (Exception e) {
                    if (dBinding.tvBuyFiatTotal != null) dBinding.tvBuyFiatTotal.setText("Total: ₹0.00");
                    if (dBinding.tvBuyReceiveAmount != null) dBinding.tvBuyReceiveAmount.setText(String.format(Locale.US, "Service Fee: %.1f%%", p2pCommission));
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        dBinding.btnConfirmBuy.setOnClickListener(v -> {
            if (listing.sellerUid.equals(currentUserId)) {
                Toast.makeText(context, "You cannot buy your own advertisement", Toast.LENGTH_SHORT).show();
                return;
            }
            String amountStr = dBinding.etBuyAmount.getText() != null ? dBinding.etBuyAmount.getText().toString().trim() : "";
            if (amountStr.isEmpty()) { dBinding.etBuyAmount.setError("Enter amount"); return; }
            try {
                double buyAmount = Double.parseDouble(amountStr);
                if (buyAmount < listing.minLimit) { dBinding.etBuyAmount.setError("Min order is " + listing.minLimit); return; }
                if (buyAmount > listing.remainingAmount) { dBinding.etBuyAmount.setError("Max available: " + listing.remainingAmount); return; }
                dBinding.btnConfirmBuy.setEnabled(false); dBinding.btnConfirmBuy.setText("Checking Seller Status...");
                checkSellerOnline(listing.sellerUid, isOnline -> initiateTrade(listing, buyAmount, isOnline, dialog));
            } catch (NumberFormatException e) { dBinding.etBuyAmount.setError("Invalid number"); }
        });
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
                    order.status = "WAITING_FOR_SELLER";
                    order.expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ongoing_p2p_orders/" + orderId, order);
                    updates.put("p2p_notifications/" + listing.sellerUid + "/" + orderId + "_waiting", true);
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
            @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { processOrderUpdate(snapshot); }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { processOrderUpdate(snapshot); }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {
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
            if ("COMPLETED".equals(order.status) && notifiedOrders.add(order.id + "_settled_buyer")) {
                settleBuyerFunds(order);
                if (activeTradeDialog != null) activeTradeDialog.dismiss();
            }
            if ((now - order.timestamp) > (30 * 60 * 1000)) return;
            if ("WAITING_FOR_SELLER".equals(order.status) || "PENDING".equals(order.status) || "PAID".equals(order.status)) {
                if (activeOrderId == null || activeOrderId.equals(order.id)) showTradeScreen(order);
            } else if ("DISPUTED".equals(order.status) || "PAYMENT_MISSING".equals(order.status)) {
                if (activeTradeDialog != null && activeTradeDialog.isShowing() && order.id.equals(activeOrderId)) { activeTradeDialog.dismiss(); activeOrderId = null; }
                if (notifiedOrders.add(order.id + "_" + order.status)) showBuyerDisputeScreen(order);
            } else if ("CANCELLED".equals(order.status)) {
                if (activeTradeDialog != null && activeTradeDialog.isShowing() && order.id.equals(activeOrderId)) { activeTradeDialog.dismiss(); activeOrderId = null; }
                showTradeDeclinedScreen(order);
            }
        } else if (isSeller) {
            if ("PAID".equals(order.status)) {
                if (promptedOrders.add(order.id + "_release_pop") || order.id.equals(activeReleaseOrderId)) showReleaseScreen(order);
            } else if ("WAITING_FOR_SELLER".equals(order.status)) {
                if (notifiedOrders.add(order.id + "_waiting")) {
                    NotificationHelper.showNotification(context, NotificationHelper.CHANNEL_PROFIT, Math.abs(order.id.hashCode()), "P2P Trade Waiting", order.buyerName + " is waiting for you to accept a trade for " + order.amount + " USDT");
                }
                showSellerAcceptPrompt(order);
            } else if ("DISPUTED".equals(order.status)) {
                if (order.id.equals(activeReleaseOrderId) && activeReleaseDialog != null) { activeReleaseDialog.dismiss(); activeReleaseOrderId = null; }
                if (notifiedOrders.add(order.id + "_disputed_seller")) {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context).setTitle("Trade Disputed")
                            .setMessage("The buyer " + order.buyerName + " has filed a complaint for the trade of " + order.amount + " USDT. The assets are now locked in escrow for admin review.")
                            .setPositiveButton("OK", null).show();
                }
            } else if ("CANCELLED".equals(order.status)) {
                if (notifiedOrders.add(order.id + "_cancelled_seller")) Toast.makeText(context, "Trade for " + String.format(Locale.US, "%.2f", order.amount) + " USDT was cancelled by the buyer.", Toast.LENGTH_LONG).show();
                if (activeAcceptDialog != null && activeAcceptDialog.isShowing() && order.id.equals(activeAcceptOrderId)) { activeAcceptDialog.dismiss(); activeAcceptOrderId = null; }
            }
        }
    }

    private void showSellerAcceptPrompt(P2POrder order) {
        if (((Activity)context).isFinishing() || !promptedOrders.add(order.id)) return;
        if (activeAcceptDialog != null && activeAcceptDialog.isShowing()) activeAcceptDialog.dismiss();
        activeAcceptOrderId = order.id;
        activeAcceptDialog = new BottomSheetDialog(context);
        DialogP2pSellerAcceptBinding dBinding = DialogP2pSellerAcceptBinding.inflate(LayoutInflater.from(context));
        activeAcceptDialog.setContentView(dBinding.getRoot());
        activeAcceptDialog.setCancelable(false);

        dBinding.tvSellerPromptMessage.setText(order.buyerName + " wants to buy USDT from you.");
        dBinding.tvSellerPromptFiat.setText(String.format(Locale.US, "₹%.2f", order.amount * order.price));
        dBinding.tvSellerPromptCrypto.setText(String.format(Locale.US, "%.2f USDT", order.amount));

        dBinding.btnSellerAccept.setOnClickListener(v -> {
            dBinding.btnSellerAccept.setEnabled(false); dBinding.btnSellerAccept.setText("Accepting...");
            Map<String, Object> updates = new HashMap<>();
            updates.put("ongoing_p2p_orders/" + order.id + "/status", "PENDING");
            long expiry = System.currentTimeMillis() + (15 * 60 * 1000);
            updates.put("ongoing_p2p_orders/" + order.id + "/expiryTime", expiry);
            updates.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_accepted", true);
            mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> activeAcceptDialog.dismiss());
        });
        dBinding.btnSellerIgnore.setOnClickListener(v -> { activeAcceptDialog.dismiss(); cancelOrder(order, "Declined by Seller"); });
        activeAcceptDialog.show();
    }

    private void showTradeScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;
        if (activeTradeDialog != null && activeTradeDialog.isShowing() && order.id.equals(activeOrderId)) {
            TextView tvStatus = activeTradeDialog.findViewById(R.id.tv_trade_status);
            if (tvStatus != null) {
                String currentText = tvStatus.getText().toString();
                if (!(currentText.contains("Waiting") && "PENDING".equals(order.status))) {
                    updateTradeDialogUI(activeTradeDialog, order);
                    return;
                }
            }
        }
        if (activeTradeDialog != null && activeTradeDialog.isShowing()) activeTradeDialog.dismiss();
        activeOrderId = order.id;
        activeTradeDialog = new BottomSheetDialog(context);
        DialogP2pTradeBinding dBinding = DialogP2pTradeBinding.inflate(LayoutInflater.from(context));
        activeTradeDialog.setContentView(dBinding.getRoot());
        activeTradeDialog.setCancelable(false);

        dBinding.tvTradeFiatAmount.setText(String.format(Locale.US, "₹%.2f", order.amount * order.price));
        dBinding.tvTradeUpiId.setText(order.upiId);

        MaterialButton btnComplaint = new MaterialButton(context);
        btnComplaint.setText("File Complaint");
        btnComplaint.setVisibility(View.GONE);
        ((ViewGroup)dBinding.btnCancelTrade.getParent()).addView(btnComplaint);

        if ("WAITING_FOR_SELLER".equals(order.status)) {
            dBinding.tvTradeStatus.setText("Waiting for Seller to Accept...");
            dBinding.tvTradeUpiId.setText("UPI Hidden");
            dBinding.btnMarkAsPaid.setVisibility(View.GONE);
            dBinding.tilTradeRef.setVisibility(View.GONE);
            dBinding.btnCopyUpi.setVisibility(View.GONE);
        } else if ("PENDING".equals(order.status)) {
            dBinding.tvTradeStatus.setText("Pay the Seller");
            dBinding.btnMarkAsPaid.setVisibility(View.VISIBLE);
            dBinding.tilTradeRef.setVisibility(View.VISIBLE);
            dBinding.btnCopyUpi.setVisibility(View.VISIBLE);
        } else if ("PAID".equals(order.status)) {
            dBinding.tvTradeStatus.setText("Payment Submitted");
            dBinding.btnMarkAsPaid.setVisibility(View.GONE);
            dBinding.btnCancelTrade.setVisibility(View.GONE);
            dBinding.tilTradeRef.setVisibility(View.GONE);
            dBinding.tvTradeTimer.setText("PAID");
            dBinding.tvTradeTimer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#22C55E")));
        }

        CountDownTimer tradeTimer = new CountDownTimer(Math.max(0, order.expiryTime - System.currentTimeMillis()), 1000) {
            public void onTick(long millisUntilFinished) {
                if ("PAID".equals(order.status) || "COMPLETED".equals(order.status)) { cancel(); return; }
                long mins = (millisUntilFinished / 1000) / 60;
                long secs = (millisUntilFinished / 1000) % 60;
                dBinding.tvTradeTimer.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
            }
            public void onFinish() { if (!"PAID".equals(order.status) && !"COMPLETED".equals(order.status)) { cancelOrder(order, "Trade Expired"); activeTradeDialog.dismiss(); } }
        };
        tradeTimer.start();

        monitorOtherPresence(order, order.sellerUid, activeTradeDialog, btnComplaint, tradeTimer);

        dBinding.btnMarkAsPaid.setOnClickListener(v -> {
            String ref = dBinding.etTradeRef.getText() != null ? dBinding.etTradeRef.getText().toString().trim() : "";
            if (ref.isEmpty()) { Toast.makeText(context, "Enter Transaction ID", Toast.LENGTH_SHORT).show(); return; }
            dBinding.btnMarkAsPaid.setEnabled(false); dBinding.btnMarkAsPaid.setText("Updating...");
            Map<String, Object> updates = new HashMap<>();
            updates.put("ongoing_p2p_orders/" + order.id + "/status", "PAID");
            updates.put("ongoing_p2p_orders/" + order.id + "/transactionId", ref);
            updates.put("p2p_notifications/" + order.sellerUid + "/" + order.id + "_paid", true);
            mDatabase.updateChildren(updates);
        });

        dBinding.btnCancelTrade.setOnClickListener(v -> cancelOrder(order, "Cancelled by Buyer"));
        btnComplaint.setOnClickListener(v -> fileDispute(order));

        dBinding.btnCopyUpi.setOnClickListener(v -> {
            if ("WAITING_FOR_SELLER".equals(order.status)) { Toast.makeText(context, "UPI ID is hidden until seller confirms", Toast.LENGTH_SHORT).show(); return; }
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("UPI ID", order.upiId);
            clipboard.setPrimaryClip(clip); Toast.makeText(context, "UPI ID Copied", Toast.LENGTH_SHORT).show();
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
            if (tvTimer != null) { tvTimer.setText("PAID"); tvTimer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#22C55E"))); }
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
                                if (complaintBtn != null) { complaintBtn.setVisibility(View.VISIBLE); Toast.makeText(context, "Seller went offline. If you have paid, please file a complaint.", Toast.LENGTH_LONG).show(); }
                            } else if ("PENDING".equals(currentStatus) && !isBuyer) cancelOrder(order, "Party went offline");
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
            activePresenceListeners.remove(order.id); if (timer != null) timer.cancel();
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
                    P2POrder finalOrder = (fetchedOrder != null) ? fetchedOrder : order; finalOrder.status = "CANCELLED";
                    Map<String, Object> cleanup = new HashMap<>(); cleanup.put("ongoing_p2p_orders/" + order.id, null);
                    if ("Declined by Seller".equals(reason)) cleanup.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_declined", true);
                    else if ("Cancelled by Buyer".equals(reason)) cleanup.put("p2p_notifications/" + order.sellerUid + "/" + order.id + "_cancelled", true);
                    mDatabase.updateChildren(cleanup); logTradeHistory(finalOrder, "CANCELLED");
                    mDatabase.child("p2p_listings").child(order.adId).runTransaction(new Transaction.Handler() {
                        @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData snap) {
                            P2PListing ad = snap.getValue(P2PListing.class);
                            if (ad != null) { snap.child("remainingAmount").setValue(ad.remainingAmount + order.amount); snap.child("status").setValue("ACTIVE"); }
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
            if (activeTradeDialog != null && activeTradeDialog.isShowing()) {
                TextInputEditText etRef = activeTradeDialog.findViewById(R.id.et_trade_ref);
                if (etRef != null && etRef.getText() != null) {
                    String ref = etRef.getText().toString().trim();
                    if (!ref.isEmpty()) order.transactionId = ref;
                }
            }
            if (order.transactionId == null || order.transactionId.trim().isEmpty()) {
                final TextInputEditText input = new TextInputEditText(context); input.setHint("Transaction ID / Ref No");
                int p = (int) (24 * context.getResources().getDisplayMetrics().density);
                android.widget.FrameLayout container = new android.widget.FrameLayout(context); container.setPadding(p, p/2, p, 0); container.addView(input);
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(context).setTitle("Payment Proof Required").setMessage("Please enter the Transaction ID/Ref No for the payment you made.").setView(container)
                        .setPositiveButton("File Complaint", (dialog, which) -> {
                            String ref = input.getText() != null ? input.getText().toString().trim() : "";
                            if (ref.isEmpty()) Toast.makeText(context, "Transaction ID is required to file a complaint", Toast.LENGTH_SHORT).show();
                            else { order.transactionId = ref; executeFileDispute(order); }
                        }).setNegativeButton("Cancel", null).show();
                return;
            }
        }
        executeFileDispute(order);
    }

    private void executeFileDispute(P2POrder order) {
        order.status = "DISPUTED"; Map<String, Object> updates = new HashMap<>(); updates.put("ongoing_p2p_orders/" + order.id, null);
        if (currentUserId.equals(order.sellerUid)) updates.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_disputed", true);
        else updates.put("p2p_notifications/" + order.sellerUid + "/" + order.id + "_disputed", true);
        mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> {
            logTradeHistory(order, "DISPUTED"); Toast.makeText(context, "Complaint filed. Admins will review the transaction proof.", Toast.LENGTH_LONG).show();
            if (activeReleaseDialog != null) activeReleaseDialog.dismiss(); if (activeTradeDialog != null) activeTradeDialog.dismiss();
        });
    }

    private void logTradeHistory(P2POrder order, String status) {
        long ts = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(new java.util.Date(ts));
        String amtStr = String.format(Locale.US, "%.2f", order.amount);
        Map<String, Object> updates = new HashMap<>();
        String bTxId = mDatabase.child("users").child(order.buyerUid).child("transactions").child("p2p").push().getKey();
        if (bTxId != null) {
            double fee = (order.amount * order.commission) / 100.0; double netAmount = order.amount - fee;
            Map<String, Object> tx = new HashMap<>(); tx.put("id", bTxId); tx.put("title", "P2P Buy: " + order.sellerName);
            tx.put("subtitle", date + " • " + status + (order.commission > 0 ? " (incl. " + order.commission + "% fee)" : ""));
            tx.put("amount", "+$" + String.format(Locale.US, "%.2f", netAmount)); tx.put("status", "Success".equals(status) || "COMPLETED".equals(status) ? "Success" : status);
            tx.put("type", "p2p"); tx.put("timestamp", ts); updates.put("users/" + order.buyerUid + "/transactions/p2p/" + bTxId, tx);
        }
        String sTxId = mDatabase.child("users").child(order.sellerUid).child("transactions").child("p2p").push().getKey();
        if (sTxId != null) {
            Map<String, Object> tx = new HashMap<>(); tx.put("id", sTxId); tx.put("title", "P2P Sell: " + order.buyerName);
            tx.put("subtitle", date + " • " + status); tx.put("amount", "-$" + amtStr); tx.put("status", "Success".equals(status) || "COMPLETED".equals(status) ? "Success" : status);
            tx.put("type", "p2p"); tx.put("timestamp", ts); updates.put("users/" + order.sellerUid + "/transactions/p2p/" + sTxId, tx);
        }
        updates.put("p2p_order_history/" + order.id, order); mDatabase.updateChildren(updates);
    }

    private void showBuyerDisputeScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogP2pBuyerDisputeBinding dBinding = DialogP2pBuyerDisputeBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dBinding.getRoot());
        if ("PAYMENT_MISSING".equals(order.status)) { if (dBinding.tvDisputeMsg != null) dBinding.tvDisputeMsg.setText("The seller claims they haven't received your payment yet. If you have already paid, please file a dispute with your transaction proof."); }
        dBinding.btnBuyerFileComplaint.setOnClickListener(v -> { fileDispute(order); dialog.dismiss(); });
        dBinding.btnBuyerCloseDispute.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showTradeDeclinedScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogP2pBuyerDisputeBinding dBinding = DialogP2pBuyerDisputeBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dBinding.getRoot());
        if (dBinding.tvDisputeMsg != null) dBinding.tvDisputeMsg.setText("The seller " + order.sellerName + " has declined your trade request for " + order.amount + " USDT.");
        dBinding.btnBuyerFileComplaint.setText("Ok, Understood"); dBinding.btnBuyerFileComplaint.setOnClickListener(v -> dialog.dismiss());
        dBinding.btnBuyerCloseDispute.setVisibility(View.GONE);
        dialog.show();
    }

    private void showReleaseScreen(P2POrder order) {
        if (((Activity)context).isFinishing()) return;
        if (activeReleaseDialog != null && activeReleaseDialog.isShowing() && order.id.equals(activeReleaseOrderId)) return;
        if (activeReleaseDialog != null && activeReleaseDialog.isShowing()) activeReleaseDialog.dismiss();
        activeReleaseOrderId = order.id; activeReleaseDialog = new BottomSheetDialog(context);
        DialogP2pReleaseBinding dBinding = DialogP2pReleaseBinding.inflate(LayoutInflater.from(context));
        activeReleaseDialog.setContentView(dBinding.getRoot());
        activeReleaseDialog.setCancelable(false);
        dBinding.tvReleaseFiatAmount.setText(String.format(Locale.US, "₹%.2f", order.amount * order.price));
        dBinding.tvReleaseRef.setText("Ref: " + order.transactionId);
        monitorOtherPresence(order, order.buyerUid, activeReleaseDialog, null, null);
        dBinding.btnConfirmRelease.setOnClickListener(v -> {
            dBinding.btnConfirmRelease.setEnabled(false); dBinding.btnConfirmRelease.setText("Releasing...");
            mDatabase.child("ongoing_p2p_orders").child(order.id).runTransaction(new Transaction.Handler() {
                @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    P2POrder currentOrder = currentData.getValue(P2POrder.class);
                    if (currentOrder == null || !"PAID".equals(currentOrder.status)) return Transaction.abort();
                    currentData.child("status").setValue("COMPLETED"); return Transaction.success(currentData);
                }
                @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (committed && currentData != null) {
                        P2POrder fetchedOrder = currentData.getValue(P2POrder.class); P2POrder finalOrder = (fetchedOrder != null) ? fetchedOrder : order;
                        finalOrder.status = "COMPLETED"; Map<String, Object> cleanup = new HashMap<>(); cleanup.put("ongoing_p2p_orders/" + order.id, null);
                        settleSellerFunds(order); cleanup.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_released", true);
                        mDatabase.updateChildren(cleanup); logTradeHistory(finalOrder, "COMPLETED");
                        Toast.makeText(context, "USDT Released Successfully!", Toast.LENGTH_SHORT).show(); if (activeReleaseDialog != null) activeReleaseDialog.dismiss();
                    } else { dBinding.btnConfirmRelease.setEnabled(true); dBinding.btnConfirmRelease.setText("Confirm & Release USDT"); Toast.makeText(context, "Release failed: Trade already processed", Toast.LENGTH_SHORT).show(); }
                }
            });
        });
        dBinding.btnFileDispute.setOnClickListener(v -> {
            dBinding.btnFileDispute.setEnabled(false); dBinding.btnFileDispute.setText("Notifying Buyer...");
            Map<String, Object> updates = new HashMap<>(); updates.put("ongoing_p2p_orders/" + order.id + "/status", "PAYMENT_MISSING");
            updates.put("p2p_notifications/" + order.buyerUid + "/" + order.id + "_missing", true);
            mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> { Toast.makeText(context, "Buyer notified of missing payment", Toast.LENGTH_SHORT).show(); if (activeReleaseDialog != null) activeReleaseDialog.dismiss(); })
                    .addOnFailureListener(e -> { dBinding.btnFileDispute.setEnabled(true); dBinding.btnFileDispute.setText("Payment Not Received"); });
        });
        activeReleaseDialog.show();
    }

    private void settleSellerFunds(P2POrder order) {
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

    private void setupDisputeMonitoring() {
        mDatabase.child("p2p_order_history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<P2POrder> disputedTrades = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    P2POrder order = ds.getValue(P2POrder.class);
                    if (order != null && "DISPUTED".equals(order.status)) { if (order.buyerUid.equals(currentUserId) || order.sellerUid.equals(currentUserId)) disputedTrades.add(order); }
                }
                if (disputedTrades.isEmpty()) binding.cvP2pDisputesAlert.setVisibility(View.GONE);
                else {
                    binding.cvP2pDisputesAlert.setVisibility(View.VISIBLE);
                    binding.tvDisputeCount.setText(disputedTrades.size() + (disputedTrades.size() == 1 ? " Active Dispute" : " Active Disputes"));
                    binding.btnOpenDisputes.setOnClickListener(v -> showDisputeListDialog(disputedTrades));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showDisputeListDialog(List<P2POrder> disputedTrades) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogP2pDisputeListBinding dBinding = DialogP2pDisputeListBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dBinding.getRoot());
        dBinding.rvDisputedTrades.setLayoutManager(new LinearLayoutManager(context));
        class DisputeAdapter extends RecyclerView.Adapter<DisputeAdapter.ViewHolder> {
            @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) { return new ViewHolder(ItemP2pDisputeSimpleBinding.inflate(LayoutInflater.from(p.getContext()), p, false)); }
            @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
                P2POrder order = disputedTrades.get(pos);
                h.binding.tvDisputeTitle.setText("Trade #" + order.id.substring(0, Math.min(order.id.length(), 8)));
                h.binding.tvDisputeSubtitle.setText(String.format(Locale.US, "Amount: $%.2f | %s", order.amount, currentUserId.equals(order.buyerUid) ? "Seller: " + order.sellerName : "Buyer: " + order.buyerName));
                h.itemView.setOnClickListener(v -> { Intent intent = new Intent(context, P2PChatActivity.class); intent.putExtra("orderId", order.id); context.startActivity(intent); dialog.dismiss(); });
            }
            @Override public int getItemCount() { return disputedTrades.size(); }
            class ViewHolder extends RecyclerView.ViewHolder { final ItemP2pDisputeSimpleBinding binding; ViewHolder(ItemP2pDisputeSimpleBinding b) { super(b.getRoot()); this.binding = b; } }
        }
        dBinding.rvDisputedTrades.setAdapter(new DisputeAdapter());
        dialog.show();
    }

    private void setupDirectTransfer() {
        binding.etSectionReceiverUid.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isUidVerified = false;
                binding.btnSectionP2pTransfer.setEnabled(false);
                binding.cvRecipientPreview.setVisibility(View.GONE);
                binding.btnVerifyUid.setText("Verify UID"); binding.btnVerifyUid.setEnabled(true);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        UserHandler.getInstance().listenToUserData(currentUserId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUnlockedBalance = getDouble(snapshot.child("unlocked_balance").getValue());
                    binding.tvP2pSectionAvailable.setText(String.format(Locale.US, "$%.2f", currentUnlockedBalance));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        binding.btnVerifyUid.setOnClickListener(v -> {
            String uid = binding.etSectionReceiverUid.getText() != null ? binding.etSectionReceiverUid.getText().toString().trim() : "";
            if (uid.isEmpty()) return;
            binding.btnVerifyUid.setEnabled(false); binding.btnVerifyUid.setText("Verifying...");
            mDatabase.child("users").child(uid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        isUidVerified = true; binding.btnVerifyUid.setText("Verified");
                        binding.tvVerifiedName.setText("Recipient: " + snapshot.getValue(String.class));
                        binding.cvRecipientPreview.setVisibility(View.VISIBLE);
                        binding.btnSectionP2pTransfer.setEnabled(true);
                    } else { binding.btnVerifyUid.setText("Invalid UID"); binding.btnVerifyUid.setEnabled(true); }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) { binding.btnVerifyUid.setEnabled(true); }
            });
        });

        binding.tvBtnMax.setOnClickListener(v -> binding.etSectionTransferAmount.setText(String.format(Locale.US, "%.2f", currentUnlockedBalance)));
        binding.chip10.setOnClickListener(v -> binding.etSectionTransferAmount.setText("10"));
        binding.chip50.setOnClickListener(v -> binding.etSectionTransferAmount.setText("50"));
        binding.chip100.setOnClickListener(v -> binding.etSectionTransferAmount.setText("100"));
        binding.btnRefreshP2p.setOnClickListener(v -> fetchP2PHistory());

        binding.btnSectionP2pTransfer.setOnClickListener(v -> {
            if (!isUidVerified) { Toast.makeText(context, "Verify UID first", Toast.LENGTH_SHORT).show(); return; }
            String amtStr = binding.etSectionTransferAmount.getText() != null ? binding.etSectionTransferAmount.getText().toString().trim() : "";
            if (amtStr.isEmpty()) return;
            double amount = Double.parseDouble(amtStr);
            UserHandler.getInstance().getUserDataFresh(currentUserId, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUnlockedBalance = getDouble(snapshot.child("unlocked_balance").getValue());
                    if (amount > currentUnlockedBalance) { Toast.makeText(context, "Insufficient unlocked profit", Toast.LENGTH_SHORT).show(); return; }
                    performSecureDirectTransfer(binding.etSectionReceiverUid.getText().toString().trim(), amount);
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        fetchP2PHistory();
        setupRecentRecipients();
    }

    private void setupRecentRecipients() {
        mDatabase.child("users").child(currentUserId).child("transactions").child("p2p").limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Map<String, String>> recentList = new ArrayList<>();
                        Set<String> addedUids = new HashSet<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String title = ds.child("title").getValue(String.class);
                            String subtitle = ds.child("subtitle").getValue(String.class);
                            if (title != null && title.contains("Sent to") && subtitle != null && subtitle.contains("UID: ")) {
                                String name = title.replace("P2P Sent to ", "").replace("Direct ", "");
                                String uid = subtitle.substring(subtitle.indexOf("UID: ") + 5).trim();
                                if (!uid.isEmpty() && !addedUids.contains(uid)) {
                                    Map<String, String> item = new HashMap<>(); item.put("name", name); item.put("uid", uid);
                                    recentList.add(item); addedUids.add(uid);
                                }
                            }
                        }
                        if (!recentList.isEmpty()) {
                            binding.llRecentP2pContainer.setVisibility(View.VISIBLE);
                            Collections.reverse(recentList);
                            binding.rvRecentP2p.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                            binding.rvRecentP2p.setAdapter(new RecyclerView.Adapter<RecentViewHolder>() {
                                @NonNull @Override public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) { return new RecentViewHolder(ItemRecentP2pBinding.inflate(LayoutInflater.from(context), p, false)); }
                                @Override public void onBindViewHolder(@NonNull RecentViewHolder h, int pos) {
                                    Map<String, String> item = recentList.get(pos); h.binding.tvRecentName.setText(item.get("name"));
                                    h.itemView.setOnClickListener(v -> { binding.etSectionReceiverUid.setText(item.get("uid")); binding.btnVerifyUid.performClick(); });
                                }
                                @Override public int getItemCount() { return Math.min(recentList.size(), 8); }
                            });
                        } else binding.llRecentP2pContainer.setVisibility(View.GONE);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private static class RecentViewHolder extends RecyclerView.ViewHolder { final ItemRecentP2pBinding binding; RecentViewHolder(ItemRecentP2pBinding b) { super(b.getRoot()); this.binding = b; } }

    private void fetchP2PHistory() {
        List<com.ascend.invest.handlers.Transaction> list = new ArrayList<>();
        TransactionAdapter adapter = new TransactionAdapter(list);
        binding.rvP2pHistory.setLayoutManager(new LinearLayoutManager(context));
        binding.rvP2pHistory.setAdapter(adapter);
        mDatabase.child("users").child(currentUserId).child("transactions").child("p2p").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    com.ascend.invest.handlers.Transaction t = ds.getValue(com.ascend.invest.handlers.Transaction.class);
                    if (t != null) list.add(t);
                }
                binding.tvEmptyHistory.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                Collections.sort(list, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void performSecureDirectTransfer(String receiverUid, double amount) {
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
                                    binding.etSectionReceiverUid.setText(""); binding.etSectionTransferAmount.setText("");
                                    binding.cvRecipientPreview.setVisibility(View.GONE);
                                    binding.btnVerifyUid.setText("Verify UID"); binding.btnVerifyUid.setEnabled(true);
                                    binding.btnSectionP2pTransfer.setEnabled(false);
                                }
                            });
                        } else Toast.makeText(context, "Transfer failed: Insufficient Balance", Toast.LENGTH_SHORT).show();
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
            Map<String, Object> tx = new HashMap<>(); tx.put("id", sTxId); tx.put("title", "Sent to " + receiverName); tx.put("subtitle", date + " • UID: " + receiverUid);
            tx.put("amount", "-$" + amtStr); tx.put("status", "Success"); tx.put("type", "p2p"); tx.put("timestamp", ts);
            updates.put("users/" + currentUserId + "/transactions/p2p/" + sTxId, tx);
        }
        String rTxId = mDatabase.child("users").child(receiverUid).child("transactions").child("p2p").push().getKey();
        if (rTxId != null) {
            Map<String, Object> tx = new HashMap<>(); tx.put("id", rTxId); tx.put("title", "Received from Source"); tx.put("subtitle", date + " • From Internal Source");
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
