package com.ascend.invest.handlers;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.tvPlanName.setText(plan.getName());
        holder.tvPlanDescription.setText(plan.getDescription());
        holder.tvInvestAmount.setText(String.format(Locale.getDefault(), "$%.2f", plan.getInvestAmount()));
        holder.tvDailyProfit.setText(String.format(Locale.getDefault(), "$%.2f", plan.getDailyProfit()));
        holder.tvTotalProfit.setText(String.format(Locale.getDefault(), "$%.2f", plan.getTotalProfit()));
        holder.tvProfitPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", plan.getProfitPercentage()));
        holder.tvDuration.setText(String.format(Locale.getDefault(), "Duration: %d Days", plan.getDurationDays()));

        DataSnapshot activeData = activePlansData != null ? activePlansData.get(plan.getId()) : null;
        int purchaseCount = purchaseCountMap != null && purchaseCountMap.containsKey(plan.getId()) ? purchaseCountMap.get(plan.getId()) : 0;
        int limit = plan.getPurchaseLimit();

        if (activeData != null) {
            holder.btnInvest.setVisibility(View.GONE);
            holder.llActivePlanActions.setVisibility(View.VISIBLE);
            if (holder.tvActiveBadge != null) holder.tvActiveBadge.setVisibility(View.VISIBLE);
            if (holder.llActiveInsights != null) holder.llActiveInsights.setVisibility(View.VISIBLE);

            // Calculate progress and earned
            Long startTime = activeData.child("startTime").getValue(Long.class);
            if (startTime != null) {
                long currentTime = System.currentTimeMillis();
                long oneDayMillis = 24L * 60L * 60L * 1000L;
                long totalDurationMillis = plan.getDurationDays() * oneDayMillis;
                long elapsedTime = currentTime - startTime;
                
                // Progress percentage
                int progress = (int) Math.min(100, (elapsedTime * 100) / totalDurationMillis);
                if (holder.pbPlanProgress != null) holder.pbPlanProgress.setProgress(progress);
                
                // Earned so far (Actually claimed profit)
                double claimedProfit = 0;
                Object claimedVal = activeData.child("claimedProfit").getValue();
                if (claimedVal instanceof Number) {
                    claimedProfit = ((Number) claimedVal).doubleValue();
                }
                
                if (holder.tvEarnedSoFar != null) {
                    holder.tvEarnedSoFar.setText(String.format(Locale.getDefault(), "Earned: $%.2f", claimedProfit));
                }

                long daysLeft = plan.getDurationDays() - (elapsedTime / oneDayMillis);
                if (daysLeft < 0) daysLeft = 0;
                holder.tvDuration.setText(String.format(Locale.getDefault(), "%d Days Left", daysLeft));
            }

            updateClaimButton(holder, plan, activeData);
        } else {
            holder.btnInvest.setVisibility(View.VISIBLE);
            holder.llActivePlanActions.setVisibility(View.GONE);
            if (holder.tvActiveBadge != null) holder.tvActiveBadge.setVisibility(View.GONE);
            if (holder.llActiveInsights != null) holder.llActiveInsights.setVisibility(View.GONE);
            
            holder.tvDuration.setText(String.format(Locale.getDefault(), "%d Days", plan.getDurationDays()));

            if (limit > 0 && purchaseCount >= limit) {
                holder.btnInvest.setText("Limit Reached");
                holder.btnInvest.setEnabled(false);
                holder.btnInvest.setAlpha(0.6f);
            } else {
                holder.btnInvest.setText("Activate Plan");
                holder.btnInvest.setEnabled(true);
                holder.btnInvest.setAlpha(1.0f);
                holder.btnInvest.setOnClickListener(v -> {
                    if (listener != null) listener.onInvestClick(plan);
                });
            }
        }
        
        String category = plan.getCategory() != null ? plan.getCategory().toLowerCase() : "";
        if (!category.isEmpty()) {
            holder.tvCategory.setText(category.toUpperCase());
            holder.tvCategory.setVisibility(View.VISIBLE);
            
            switch (category) {
                case "bronze":
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_category_bronze);
                    holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#9A3412"));
                    break;
                case "silver":
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_category_silver);
                    holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#475569"));
                    break;
                case "gold":
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_category_gold);
                    holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#92400E"));
                    break;
                case "platinum":
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_category_platinum);
                    holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#0F172A"));
                    break;
                case "diamond":
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_category_diamond);
                    holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#6C5CE7"));
                    break;
                default:
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_icon_grey);
                    holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#64748B"));
                    break;
            }
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }
    }

    private void updateClaimButton(PlanViewHolder holder, Plan plan, DataSnapshot activeData) {
        Long lastClaim = activeData.child("lastProfitClaim").getValue(Long.class);
        if (lastClaim == null) lastClaim = 0L;

        long currentTime = System.currentTimeMillis();
        long nextClaimTime = lastClaim + (24L * 60L * 60L * 1000L);
        
        if (currentTime >= nextClaimTime) {
            holder.btnClaimProfit.setEnabled(true);
            holder.btnClaimProfit.setText("Collect Daily Profit");
            holder.btnClaimProfit.setAlpha(1.0f);
            holder.tvNextClaim.setText("Profit is ready to claim!");
            if (holder.tvActiveBadge != null) {
                holder.tvActiveBadge.setText("● PROFIT READY");
                holder.tvActiveBadge.setBackgroundResource(R.drawable.bg_green_badge);
                holder.tvActiveBadge.setTextColor(android.graphics.Color.parseColor("#22C55E"));
            }
            holder.btnClaimProfit.setOnClickListener(v -> {
                if (listener != null) listener.onClaimClick(plan, activeData);
            });
        } else {
            holder.btnClaimProfit.setEnabled(false);
            holder.btnClaimProfit.setText("Claimed");
            holder.btnClaimProfit.setAlpha(0.6f);
            
            long diff = nextClaimTime - currentTime;
            long hours = (diff / (60 * 60 * 1000)) % 24;
            long minutes = (diff / (60 * 1000)) % 60;
            long seconds = (diff / 1000) % 60;
            
            holder.tvNextClaim.setText(String.format(Locale.getDefault(), "Next claim available in %02d:%02d:%02d", hours, minutes, seconds));
            if (holder.tvActiveBadge != null) {
                holder.tvActiveBadge.setText("● ACTIVE");
                holder.tvActiveBadge.setBackgroundResource(R.drawable.status_purple_bg);
                holder.tvActiveBadge.setTextColor(android.graphics.Color.parseColor("#6C5CE7"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvPlanDescription, tvInvestAmount, tvDailyProfit, tvTotalProfit, tvProfitPercentage, tvDuration, tvCategory, tvNextClaim, tvActiveBadge, tvEarnedSoFar;
        Button btnInvest, btnClaimProfit;
        LinearLayout llActivePlanActions, llActiveInsights;
        com.google.android.material.progressindicator.LinearProgressIndicator pbPlanProgress;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanDescription = itemView.findViewById(R.id.tvPlanDescription);
            tvInvestAmount = itemView.findViewById(R.id.tvInvestAmount);
            tvDailyProfit = itemView.findViewById(R.id.tvDailyProfit);
            tvTotalProfit = itemView.findViewById(R.id.tvTotalProfit);
            tvProfitPercentage = itemView.findViewById(R.id.tvProfitPercentage);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnInvest = itemView.findViewById(R.id.btnInvest);
            btnClaimProfit = itemView.findViewById(R.id.btnClaimProfit);
            tvNextClaim = itemView.findViewById(R.id.tvNextClaim);
            tvActiveBadge = itemView.findViewById(R.id.tvActiveBadge);
            tvEarnedSoFar = itemView.findViewById(R.id.tvEarnedSoFar);
            llActivePlanActions = itemView.findViewById(R.id.llActivePlanActions);
            llActiveInsights = itemView.findViewById(R.id.llActiveInsights);
            pbPlanProgress = itemView.findViewById(R.id.pbPlanProgress);
        }
    }
}
