package com.ascend.invest.handlers;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.ascend.invest.databinding.ItemPlanBinding;
import com.google.firebase.database.DataSnapshot;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<Plan> planList;
    private Map<String, DataSnapshot> activePlansData;
    private Map<String, Integer> purchaseCountMap;
    private OnPlanInteractionListener listener;
    private Handler timerHandler = new Handler(Looper.getMainLooper());

    public interface OnPlanInteractionListener {
        void onInvestClick(Plan plan);
        void onClaimClick(Plan plan, DataSnapshot activeData);
    }

    public PlanAdapter(List<Plan> planList, Map<String, DataSnapshot> activePlansData, Map<String, Integer> purchaseCountMap, OnPlanInteractionListener listener) {
        this.planList = planList;
        this.activePlansData = activePlansData;
        this.purchaseCountMap = purchaseCountMap;
        this.listener = listener;
        startTimer();
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                timerHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlanBinding binding = ItemPlanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlanViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.binding.tvPlanName.setText(plan.getName());
        holder.binding.tvPlanDescription.setText(plan.getDescription());
        holder.binding.tvInvestAmount.setText(String.format(Locale.getDefault(), "$%.2f", plan.getInvestAmount()));
        holder.binding.tvDailyProfit.setText(String.format(Locale.getDefault(), "$%.2f", plan.getDailyProfit()));
        holder.binding.tvTotalProfit.setText(String.format(Locale.getDefault(), "$%.2f", plan.getTotalProfit()));
        holder.binding.tvProfitPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", plan.getProfitPercentage()));
        holder.binding.tvDuration.setText(String.format(Locale.getDefault(), "Duration: %d Days", plan.getDurationDays()));

        DataSnapshot activeData = activePlansData != null ? activePlansData.get(plan.getId()) : null;
        int purchaseCount = purchaseCountMap != null && purchaseCountMap.containsKey(plan.getId()) ? purchaseCountMap.get(plan.getId()) : 0;
        int limit = plan.getPurchaseLimit();

        if (activeData != null) {
            holder.binding.btnInvest.setVisibility(View.GONE);
            holder.binding.llActivePlanActions.setVisibility(View.VISIBLE);
            if (holder.binding.tvActiveBadge != null) holder.binding.tvActiveBadge.setVisibility(View.VISIBLE);
            if (holder.binding.llActiveInsights != null) holder.binding.llActiveInsights.setVisibility(View.VISIBLE);

            // Calculate progress and earned
            Long startTime = activeData.child("startTime").getValue(Long.class);
            if (startTime != null) {
                long currentTime = System.currentTimeMillis();
                long oneDayMillis = 24L * 60L * 60L * 1000L;
                long totalDurationMillis = plan.getDurationDays() * oneDayMillis;
                long elapsedTime = currentTime - startTime;
                
                // Progress percentage
                int progress = (int) Math.min(100, (elapsedTime * 100) / totalDurationMillis);
                if (holder.binding.pbPlanProgress != null) holder.binding.pbPlanProgress.setProgress(progress);
                
                // Earned so far (Actually claimed profit)
                double claimedProfit = 0;
                Object claimedVal = activeData.child("claimedProfit").getValue();
                if (claimedVal instanceof Number) {
                    claimedProfit = ((Number) claimedVal).doubleValue();
                }
                
                if (holder.binding.tvEarnedSoFar != null) {
                    holder.binding.tvEarnedSoFar.setText(String.format(Locale.getDefault(), "Earned: $%.2f", claimedProfit));
                }

                long daysLeft = plan.getDurationDays() - (elapsedTime / oneDayMillis);
                if (daysLeft < 0) daysLeft = 0;
                holder.binding.tvDuration.setText(String.format(Locale.getDefault(), "%d Days Left", daysLeft));
            }

            updateClaimButton(holder, plan, activeData);
        } else {
            holder.binding.btnInvest.setVisibility(View.VISIBLE);
            holder.binding.llActivePlanActions.setVisibility(View.GONE);
            if (holder.binding.tvActiveBadge != null) holder.binding.tvActiveBadge.setVisibility(View.GONE);
            if (holder.binding.llActiveInsights != null) holder.binding.llActiveInsights.setVisibility(View.GONE);
            
            holder.binding.tvDuration.setText(String.format(Locale.getDefault(), "%d Days", plan.getDurationDays()));

            if (limit > 0 && purchaseCount >= limit) {
                holder.binding.btnInvest.setText("Limit Reached");
                holder.binding.btnInvest.setEnabled(false);
                holder.binding.btnInvest.setAlpha(0.6f);
            } else {
                holder.binding.btnInvest.setText("Activate Plan");
                holder.binding.btnInvest.setEnabled(true);
                holder.binding.btnInvest.setAlpha(1.0f);
                holder.binding.btnInvest.setOnClickListener(v -> {
                    if (listener != null) listener.onInvestClick(plan);
                });
            }
        }
        
        String category = plan.getCategory() != null ? plan.getCategory().toLowerCase() : "";
        if (!category.isEmpty()) {
            holder.binding.tvCategory.setText(category.toUpperCase());
            holder.binding.tvCategory.setVisibility(View.VISIBLE);
            
            switch (category) {
                case "bronze":
                    holder.binding.tvCategory.setBackgroundResource(R.drawable.bg_category_bronze);
                    holder.binding.tvCategory.setTextColor(android.graphics.Color.parseColor("#9A3412"));
                    break;
                case "silver":
                    holder.binding.tvCategory.setBackgroundResource(R.drawable.bg_category_silver);
                    holder.binding.tvCategory.setTextColor(android.graphics.Color.parseColor("#475569"));
                    break;
                case "gold":
                    holder.binding.tvCategory.setBackgroundResource(R.drawable.bg_category_gold);
                    holder.binding.tvCategory.setTextColor(android.graphics.Color.parseColor("#92400E"));
                    break;
                case "platinum":
                    holder.binding.tvCategory.setBackgroundResource(R.drawable.bg_category_platinum);
                    holder.binding.tvCategory.setTextColor(android.graphics.Color.parseColor("#0F172A"));
                    break;
                case "diamond":
                    holder.binding.tvCategory.setBackgroundResource(R.drawable.bg_category_diamond);
                    holder.binding.tvCategory.setTextColor(android.graphics.Color.parseColor("#6C5CE7"));
                    break;
                default:
                    holder.binding.tvCategory.setBackgroundResource(R.drawable.bg_icon_grey);
                    holder.binding.tvCategory.setTextColor(android.graphics.Color.parseColor("#64748B"));
                    break;
            }
        } else {
            holder.binding.tvCategory.setVisibility(View.GONE);
        }
    }

    private void updateClaimButton(PlanViewHolder holder, Plan plan, DataSnapshot activeData) {
        Long lastClaim = activeData.child("lastProfitClaim").getValue(Long.class);
        if (lastClaim == null) lastClaim = 0L;

        long currentTime = System.currentTimeMillis();
        long nextClaimTime = lastClaim + (24L * 60L * 60L * 1000L);
        
        if (currentTime >= nextClaimTime) {
            holder.binding.btnClaimProfit.setEnabled(true);
            holder.binding.btnClaimProfit.setText("Collect Daily Profit");
            holder.binding.btnClaimProfit.setAlpha(1.0f);
            holder.binding.tvNextClaim.setText("Profit is ready to claim!");
            if (holder.binding.tvActiveBadge != null) {
                holder.binding.tvActiveBadge.setText("● PROFIT READY");
                holder.binding.tvActiveBadge.setBackgroundResource(R.drawable.bg_green_badge);
                holder.binding.tvActiveBadge.setTextColor(android.graphics.Color.parseColor("#22C55E"));
            }
            holder.binding.btnClaimProfit.setOnClickListener(v -> {
                if (listener != null) listener.onClaimClick(plan, activeData);
            });
        } else {
            holder.binding.btnClaimProfit.setEnabled(false);
            holder.binding.btnClaimProfit.setText("Claimed");
            holder.binding.btnClaimProfit.setAlpha(0.6f);
            
            long diff = nextClaimTime - currentTime;
            long hours = (diff / (60 * 60 * 1000)) % 24;
            long minutes = (diff / (60 * 1000)) % 60;
            long seconds = (diff / 1000) % 60;
            
            holder.binding.tvNextClaim.setText(String.format(Locale.getDefault(), "Next claim available in %02d:%02d:%02d", hours, minutes, seconds));
            if (holder.binding.tvActiveBadge != null) {
                holder.binding.tvActiveBadge.setText("● ACTIVE");
                holder.binding.tvActiveBadge.setBackgroundResource(R.drawable.status_purple_bg);
                holder.binding.tvActiveBadge.setTextColor(android.graphics.Color.parseColor("#6C5CE7"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        final ItemPlanBinding binding;

        public PlanViewHolder(@NonNull ItemPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
