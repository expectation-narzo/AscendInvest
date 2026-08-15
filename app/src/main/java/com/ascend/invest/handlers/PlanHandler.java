package com.ascend.invest.handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.ascend.invest.R;
import com.ascend.invest.databinding.LayoutPlanFiltersBinding;
import com.ascend.invest.databinding.SectionPlanBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PlanHandler {

    private final Context context;
    private final SectionPlanBinding binding;

    private PlanAdapter adapter;
    private BannerAdapter bannerAdapter;

    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private static final long AUTO_SCROLL_DELAY = 4000;

    private final List<Plan> allPlans = new ArrayList<>();
    private final List<Plan> filteredPlans = new ArrayList<>();
    private final List<Plan> bannerList = new ArrayList<>();

    private final Map<String, DataSnapshot> activePlansData = new HashMap<>();
    private final Map<String, Integer> purchaseCountMap = new HashMap<>();

    private String selectedCategory = "All";
    private final Set<String> availableCategories = new HashSet<>();
    private float minBudget = 0, maxBudget = 1000;
    private float currentMin = 0, currentMax = 1000;
    private boolean isLowToHigh = true;

    private final DatabaseReference mDatabase;
    private String userId;

    public PlanHandler(Context context, SectionPlanBinding binding) {
        this.context = context;
        this.binding = binding;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();

        if (binding.btnToggleFilter != null) {
            binding.btnToggleFilter.setOnClickListener(v -> showFilterDialog());
        }

        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        adapter = new PlanAdapter(filteredPlans, activePlansData, purchaseCountMap, new PlanAdapter.OnPlanInteractionListener() {
            @Override
            public void onInvestClick(Plan plan) {
                handleInvestment(plan);
            }

            @Override
            public void onClaimClick(Plan plan, DataSnapshot activeData) {
                claimProfit(plan, activeData, false);
            }
        });
        binding.rvPlans.setLayoutManager(new LinearLayoutManager(context));
        binding.rvPlans.setAdapter(adapter);

        bannerAdapter = new BannerAdapter(bannerList, plan -> scrollToPlan(plan.getId()));
        binding.vpBanners.setAdapter(bannerAdapter);

        new TabLayoutMediator(binding.bannerIndicator, binding.vpBanners, (tab, position) -> {}).attach();

        setupAutoScroll();
    }

    private void setupAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerList.size() > 1) {
                    int nextItem = (binding.vpBanners.getCurrentItem() + 1) % bannerList.size();
                    binding.vpBanners.setCurrentItem(nextItem, true);
                }
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        };

        binding.vpBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                autoScrollHandler.removeCallbacks(autoScrollRunnable);
                autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
            }
        });
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        LayoutPlanFiltersBinding dBinding = LayoutPlanFiltersBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dBinding.getRoot());

        if (dBinding.tvMinBudget != null) dBinding.tvMinBudget.setText(String.format(Locale.getDefault(), "$%.0f", minBudget));
        if (dBinding.tvMaxBudget != null) dBinding.tvMaxBudget.setText(String.format(Locale.getDefault(), "$%.0f", maxBudget));

        if (dBinding.btnResetFilters != null) {
            dBinding.btnResetFilters.setOnClickListener(v -> {
                selectedCategory = "All";
                currentMin = minBudget;
                currentMax = maxBudget;
                isLowToHigh = true;
                applyFilters();
                dialog.dismiss();
            });
        }

        if (isLowToHigh) {
            dBinding.cgSort.check(R.id.chipLowToHigh);
        } else {
            dBinding.cgSort.check(R.id.chipHighToLow);
        }

        List<String> sortedCats = new ArrayList<>(availableCategories);
        Collections.sort(sortedCats);
        for (String cat : sortedCats) {
            Chip chip = (Chip) LayoutInflater.from(context).inflate(R.layout.item_filter_chip, dBinding.cgCategories, false);
            chip.setText(cat);
            chip.setCheckable(true);
            if (cat.equals(selectedCategory)) chip.setChecked(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedCategory = cat;
            });
            dBinding.cgCategories.addView(chip);
        }

        float sliderMax = maxBudget > minBudget ? maxBudget : minBudget + 1;
        dBinding.rsBudget.setValueFrom(minBudget);
        dBinding.rsBudget.setValueTo(sliderMax);
        dBinding.rsBudget.setValues(currentMin, currentMax);

        dBinding.tvBudgetLabel.setText(String.format(Locale.getDefault(), "$%.0f - $%.0f", currentMin, currentMax));

        dBinding.rsBudget.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            dBinding.tvBudgetLabel.setText(String.format(Locale.getDefault(), "$%.0f - $%.0f", values.get(0), values.get(1)));
        });

        dBinding.btnApplyFilters.setOnClickListener(v -> {
            List<Float> values = dBinding.rsBudget.getValues();
            currentMin = values.get(0);
            currentMax = values.get(1);

            int checkedSortId = dBinding.cgSort.getCheckedChipId();
            isLowToHigh = (checkedSortId == R.id.chipLowToHigh);

            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        filteredPlans.clear();
        bannerList.clear();

        boolean isAllSelected = selectedCategory.equals("All");

        for (Plan plan : allPlans) {
            boolean categoryMatch = isAllSelected || plan.getCategory().equalsIgnoreCase(selectedCategory);
            boolean budgetMatch = plan.getInvestAmount() >= currentMin && plan.getInvestAmount() <= currentMax;

            if (categoryMatch && budgetMatch) {
                filteredPlans.add(plan);
                if (plan.isFeatured() && isAllSelected) {
                    bannerList.add(plan);
                }
            }
        }

        Collections.sort(filteredPlans, (p1, p2) -> {
            if (isLowToHigh) {
                return Double.compare(p1.getInvestAmount(), p2.getInvestAmount());
            } else {
                return Double.compare(p2.getInvestAmount(), p1.getInvestAmount());
            }
        });

        Collections.sort(bannerList, (p1, p2) -> Double.compare(p2.getInvestAmount(), p1.getInvestAmount()));

        adapter.notifyDataSetChanged();
        bannerAdapter.notifyDataSetChanged();

        int bannerVisibility = bannerList.isEmpty() ? View.GONE : View.VISIBLE;
        int indicatorVisibility = bannerList.size() <= 1 ? View.GONE : View.VISIBLE;

        if (binding.bannerContainer != null) {
            binding.bannerContainer.setVisibility(bannerVisibility);
        }
        if (binding.bannerIndicator != null) {
            binding.bannerIndicator.setVisibility(indicatorVisibility);
        }
    }

    private void scrollToPlan(String planId) {
        for (int i = 0; i < filteredPlans.size(); i++) {
            if (filteredPlans.get(i).getId().equals(planId)) {
                binding.rvPlans.smoothScrollToPosition(i);
                break;
            }
        }
    }

    public void init(String userId) {
        this.userId = userId;
        fetchUserStatus();
        fetchPlans();
    }

    private void fetchUserStatus() {
        if (userId == null) return;
        UserHandler.getInstance().listenToUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    activePlansData.clear();
                    DataSnapshot plansSnap = snapshot.child("active_plans");
                    for (DataSnapshot ds : plansSnap.getChildren()) {
                        activePlansData.put(ds.getKey(), ds);
                    }

                    purchaseCountMap.clear();
                    DataSnapshot countsSnap = snapshot.child("purchase_counts");
                    for (DataSnapshot ds : countsSnap.getChildren()) {
                        Integer count = ds.getValue(Integer.class);
                        if (count != null) purchaseCountMap.put(ds.getKey(), count);
                    }

                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchPlans() {
        DatabaseReference plansRef = mDatabase.child("plans");
        plansRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlans.clear();
                availableCategories.clear();
                availableCategories.add("All");

                double absoluteMin = Double.MAX_VALUE;
                double absoluteMax = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    Plan plan = data.getValue(Plan.class);
                    if (plan != null) {
                        plan.setId(data.getKey());
                        if (plan.isActive()) {
                            allPlans.add(plan);
                            if (plan.getCategory() != null) availableCategories.add(plan.getCategory());

                            double amt = plan.getInvestAmount();
                            if (amt < absoluteMin) absoluteMin = amt;
                            if (amt > absoluteMax) absoluteMax = amt;
                        }
                    }
                }

                if (allPlans.isEmpty()) {
                    absoluteMin = 0;
                    absoluteMax = 1000;
                }

                minBudget = (float) absoluteMin;
                maxBudget = (float) absoluteMax;

                if (currentMax == 1000 && currentMin == 0) {
                    currentMin = minBudget;
                    currentMax = maxBudget;
                }

                applyFilters();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void handleInvestment(Plan plan) {
        if (userId == null) return;

        UserHandler.getInstance().getUserDataFresh(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double currentBalance = 0;
                double unlockedBalance = 0;
                double p2pListed = 0;
                double p2pEscrow = 0;

                Object balVal = snapshot.child("wallet_balance").getValue();
                if (balVal instanceof Number) currentBalance = ((Number) balVal).doubleValue();

                Object unlVal = snapshot.child("unlocked_balance").getValue();
                if (unlVal instanceof Number) unlockedBalance = ((Number) unlVal).doubleValue();

                Object listVal = snapshot.child("p2p_listed_balance").getValue();
                if (listVal instanceof Number) p2pListed = ((Number) listVal).doubleValue();

                Object escVal = snapshot.child("p2p_escrow_balance").getValue();
                if (escVal instanceof Number) p2pEscrow = ((Number) escVal).doubleValue();

                double spendableBalance = currentBalance - p2pListed - p2pEscrow;

                if (spendableBalance < plan.getInvestAmount()) {
                    Toast.makeText(context, "Insufficient balance (USDT locked in P2P Ads/Trades)", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (activePlansData.containsKey(plan.getId())) return;
                int currentPurchases = purchaseCountMap.containsKey(plan.getId()) ? purchaseCountMap.get(plan.getId()) : 0;
                if (plan.getPurchaseLimit() > 0 && currentPurchases >= plan.getPurchaseLimit()) return;

                double investAmount = plan.getInvestAmount();
                double dailyProfit = plan.getDailyProfit();
                double lockedBalance = Math.max(0, currentBalance - unlockedBalance);

                double newUnlockedBalance = unlockedBalance;
                if (investAmount > lockedBalance) {
                    double remainingToDeductFromUnlocked = investAmount - lockedBalance;
                    newUnlockedBalance = Math.max(0, unlockedBalance - remainingToDeductFromUnlocked);
                }

                double finalWalletBalance = currentBalance - investAmount + dailyProfit;
                double finalUnlockedBalance = newUnlockedBalance + dailyProfit;
                double finalTotalProfit = 0;
                Object totProfVal = snapshot.child("total_profit").getValue();
                if (totProfVal instanceof Number) finalTotalProfit = ((Number) totProfVal).doubleValue();

                finalTotalProfit += dailyProfit;

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("wallet_balance", finalWalletBalance);
                userUpdates.put("unlocked_balance", finalUnlockedBalance);
                userUpdates.put("total_profit", finalTotalProfit);

                Map<String, Object> activePlanData = new HashMap<>();
                activePlanData.put("planId", plan.getId());
                activePlanData.put("startTime", ServerValue.TIMESTAMP);
                activePlanData.put("durationDays", plan.getDurationDays());
                activePlanData.put("dailyProfit", plan.getDailyProfit());
                activePlanData.put("lastProfitClaim", ServerValue.TIMESTAMP);
                activePlanData.put("investedAmount", investAmount);
                activePlanData.put("planName", plan.getName());
                activePlanData.put("claimedProfit", dailyProfit);

                userUpdates.put("active_plans/" + plan.getId(), activePlanData);
                userUpdates.put("purchase_counts/" + plan.getId(), currentPurchases + 1);

                String invTxId = UserHandler.getInstance().getTransactionsRef(userId, "investment").push().getKey();
                if (invTxId != null) {
                    Map<String, Object> txData = new HashMap<>();
                    txData.put("id", invTxId);
                    txData.put("title", "Plan Activated: " + plan.getName());
                    txData.put("amount", "-$" + investAmount);
                    txData.put("status", "Success");
                    txData.put("type", "investment");
                    txData.put("timestamp", ServerValue.TIMESTAMP);
                    userUpdates.put("transactions/investment/" + invTxId, txData);
                }

                String profTxId = UserHandler.getInstance().getTransactionsRef(userId, "profit").push().getKey();
                if (profTxId != null) {
                    Map<String, Object> txData = new HashMap<>();
                    txData.put("id", profTxId);
                    txData.put("title", "Initial Profit: " + plan.getName());
                    txData.put("amount", "+$" + String.format("%.2f", dailyProfit));
                    txData.put("status", "Success");
                    txData.put("type", "profit");
                    txData.put("timestamp", ServerValue.TIMESTAMP);
                    userUpdates.put("transactions/profit/" + profTxId, txData);
                }

                UserHandler.getInstance().updateUserData(userId, userUpdates, task -> {
                    Toast.makeText(context, "Plan " + plan.getName() + " activated with initial profit!", Toast.LENGTH_LONG).show();
                }, e -> Toast.makeText(context, "Activation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void claimProfit(Plan plan, DataSnapshot activeData, boolean isAuto) {
        Long lastClaim = activeData.child("lastProfitClaim").getValue(Long.class);
        if (lastClaim == null) return;
        long currentTime = System.currentTimeMillis();
        long oneDayMillis = 24L * 60L * 60L * 1000L;
        if (currentTime - lastClaim < oneDayMillis) return;
        long daysToClaim = (currentTime - lastClaim) / oneDayMillis;
        double profitToCredit = daysToClaim * plan.getDailyProfit();

        UserHandler.getInstance().getUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double currentBalance = 0;
                double currentTotalProfit = 0;
                double currentUnlockedBalance = 0;
                double claimedSoFar = 0;

                Object balVal = snapshot.child("wallet_balance").getValue();
                if (balVal instanceof Number) currentBalance = ((Number) balVal).doubleValue();

                Object totProfVal = snapshot.child("total_profit").getValue();
                if (totProfVal instanceof Number) currentTotalProfit = ((Number) totProfVal).doubleValue();

                Object unlVal = snapshot.child("unlocked_balance").getValue();
                if (unlVal instanceof Number) currentUnlockedBalance = ((Number) unlVal).doubleValue();

                DataSnapshot planSnap = snapshot.child("active_plans").child(plan.getId());
                Object claimedVal = planSnap.child("claimedProfit").getValue();
                if (claimedVal instanceof Number) claimedSoFar = ((Number) claimedVal).doubleValue();

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("wallet_balance", currentBalance + profitToCredit);
                userUpdates.put("total_profit", currentTotalProfit + profitToCredit);
                userUpdates.put("unlocked_balance", currentUnlockedBalance + profitToCredit);
                userUpdates.put("active_plans/" + plan.getId() + "/lastProfitClaim", ServerValue.TIMESTAMP);
                userUpdates.put("active_plans/" + plan.getId() + "/claimedProfit", claimedSoFar + profitToCredit);

                String txId = UserHandler.getInstance().getTransactionsRef(userId, "profit").push().getKey();
                if (txId != null) {
                    Map<String, Object> txData = new HashMap<>();
                    txData.put("id", txId);
                    txData.put("title", (isAuto ? "Auto " : "") + "Profit Claimed: " + plan.getName());
                    txData.put("amount", "+$" + String.format("%.2f", profitToCredit));
                    txData.put("status", "Success");
                    txData.put("type", "profit");
                    txData.put("timestamp", ServerValue.TIMESTAMP);
                    userUpdates.put("transactions/profit/" + txId, txData);
                }

                UserHandler.getInstance().updateUserData(userId, userUpdates, aVoid -> {
                    if (!isAuto) {
                        Toast.makeText(context, "Daily profit of $" + String.format("%.2f", profitToCredit) + " claimed!", Toast.LENGTH_SHORT).show();
                    }
                }, e -> Toast.makeText(context, "Claim failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
