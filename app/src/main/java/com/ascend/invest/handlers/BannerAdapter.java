package com.ascend.invest.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;

import java.util.List;
import java.util.Locale;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Plan> bannerPlans;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(Plan plan);
    }

    public BannerAdapter(List<Plan> bannerPlans, OnBannerClickListener listener) {
        this.bannerPlans = bannerPlans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Plan plan = bannerPlans.get(position);
        holder.tvPlanName.setText(plan.getName());
        holder.tvSubtitle.setText(String.format(Locale.getDefault(), "Tier %s • %.0f%% ROI • %d Days", 
                plan.getCategory().toUpperCase(), plan.getProfitPercentage(), plan.getDurationDays()));
        
        holder.tvInvest.setText(String.format(Locale.getDefault(), "$%.0f", plan.getInvestAmount()));
        holder.tvReturn.setText(String.format(Locale.getDefault(), "%.0f%%", plan.getProfitPercentage()));
        holder.tvDaily.setText(String.format(Locale.getDefault(), "$%.2f", plan.getDailyProfit()));

        String category = plan.getCategory() != null ? plan.getCategory().toLowerCase() : "";
        holder.tvTitle.setText("ELITE " + category.toUpperCase() + " OPPORTUNITY");
        
        switch (category) {
            case "bronze":
                holder.rlBackground.setBackgroundResource(R.drawable.bg_banner_bronze);
                break;
            case "silver":
                holder.rlBackground.setBackgroundResource(R.drawable.bg_banner_silver);
                break;
            case "gold":
                holder.rlBackground.setBackgroundResource(R.drawable.bg_banner_gold);
                break;
            case "platinum":
                holder.rlBackground.setBackgroundResource(R.drawable.bg_banner_platinum);
                break;
            case "diamond":
                holder.rlBackground.setBackgroundResource(R.drawable.bg_banner_diamond);
                break;
            default:
                holder.tvTitle.setText("INVESTMENT OFFER");
                holder.rlBackground.setBackgroundResource(R.drawable.bg_banner_gold);
                break;
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBannerClick(plan);
        });
    }
    @Override
    public int getItemCount() {
        return bannerPlans.size();
    }
    static class BannerViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvSubtitle, tvTitle, tvInvest, tvReturn, tvDaily;
        RelativeLayout rlBackground;
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvBannerPlanName);
            tvSubtitle = itemView.findViewById(R.id.tvBannerSubtitle);
            tvTitle = itemView.findViewById(R.id.tvBannerTitle);
            tvInvest = itemView.findViewById(R.id.tvBannerInvest);
            tvReturn = itemView.findViewById(R.id.tvBannerReturn);
            tvDaily = itemView.findViewById(R.id.tvBannerDaily);
            rlBackground = itemView.findViewById(R.id.rlBannerBackground);
        }
    }
}
