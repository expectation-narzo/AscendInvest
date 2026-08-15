package com.ascend.invest.handlers;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.ascend.invest.databinding.ItemTransactionBinding;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;
    private OnTransactionClickListener clickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.binding.tvTransactionTitle.setText(transaction.getTitle());
        holder.binding.tvTransactionSubtitle.setText(transaction.getSubtitle());
        holder.binding.tvTransactionAmount.setText(transaction.getAmount());
        holder.binding.tvTransactionStatus.setText(transaction.getStatus());

        if (transaction.getTransactionId() != null && !transaction.getTransactionId().isEmpty()) {
            holder.binding.tvTransactionId.setVisibility(View.VISIBLE);
            holder.binding.tvTransactionId.setText("TXID: " + transaction.getTransactionId());
        } else {
            holder.binding.tvTransactionId.setVisibility(View.GONE);
        }

        if ("deposit".equalsIgnoreCase(transaction.getType())) {
            holder.binding.ivTransactionIcon.setImageResource(R.drawable.ic_deposit);
            holder.binding.ivTransactionIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
            holder.binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
        } else if ("withdraw".equalsIgnoreCase(transaction.getType())) {
            holder.binding.ivTransactionIcon.setImageResource(R.drawable.ic_withdraw);
            holder.binding.ivTransactionIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
            holder.binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
        } else if ("p2p".equalsIgnoreCase(transaction.getType())) {
            holder.binding.ivTransactionIcon.setImageResource(R.drawable.ic_link);
            holder.binding.ivTransactionIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_purple));
            if (transaction.getAmount().startsWith("-")) {
                holder.binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
            } else {
                holder.binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
            }
        }

        // Status colors
        if ("Success".equalsIgnoreCase(transaction.getStatus())) {
            holder.binding.tvTransactionStatus.setBackgroundResource(R.drawable.status_success_bg);
            holder.binding.tvTransactionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
        } else if ("Pending".equalsIgnoreCase(transaction.getStatus())) {
            holder.binding.tvTransactionStatus.setBackgroundResource(R.drawable.status_pending_bg);
            holder.binding.tvTransactionStatus.setTextColor(Color.parseColor("#FF8C42")); // Custom orange
        } else {
            holder.binding.tvTransactionStatus.setBackgroundResource(R.drawable.status_failed_bg);
            holder.binding.tvTransactionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        public ViewHolder(@NonNull ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
