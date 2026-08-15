package com.ascend.invest.handlers;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.ascend.invest.databinding.ItemBannerBinding;

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
        ItemBannerBinding binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Plan plan = bannerPlans.get(position);
        holder.binding.tvBannerPlanName.setText(plan.getName());
        holder.binding.tvBannerSubtitle.setText(String.format(Locale.getDefault(), "Tier %s • %.0f%% ROI • %d Days", 
                plan.getCategory().toUpperCase(), plan.getProfitPercentage(), plan.getDurationDays()));
        
        holder.binding.tvBannerInvest.setText(String.format(Locale.getDefault(), "$%.0f", plan.getInvestAmount()));
        holder.binding.tvBannerReturn.setText(String.format(Locale.getDefault(), "%.0f%%", plan.getProfitPercentage()));
        holder.binding.tvBannerDaily.setText(String.format(Locale.getDefault(), "$%.2f", plan.getDailyProfit()));

        String category = plan.getCategory() != null ? plan.getCategory().toLowerCase() : "";
        holder.binding.tvBannerTitle.setText("ELITE " + category.toUpperCase() + " OPPORTUNITY");
        
        switch (category) {
            case "bronze":
                holder.binding.rlBannerBackground.setBackgroundResource(R.drawable.bg_banner_bronze);
                break;
            case "silver":
                holder.binding.rlBannerBackground.setBackgroundResource(R.drawable.bg_banner_silver);
                break;
            case "gold":
                holder.binding.rlBannerBackground.setBackgroundResource(R.drawable.bg_banner_gold);
                break;
            case "platinum":
                holder.binding.rlBannerBackground.setBackgroundResource(R.drawable.bg_banner_platinum);
                break;
            case "diamond":
                holder.binding.rlBannerBackground.setBackgroundResource(R.drawable.bg_banner_diamond);
                break;
            default:
                holder.binding.tvBannerTitle.setText("INVESTMENT OFFER");
                holder.binding.rlBannerBackground.setBackgroundResource(R.drawable.bg_banner_gold);
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
        final ItemBannerBinding binding;

        public BannerViewHolder(@NonNull ItemBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
