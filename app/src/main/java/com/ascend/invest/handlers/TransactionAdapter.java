package com.ascend.invest.handlers;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.tvTitle.setText(transaction.getTitle());
        holder.tvSubtitle.setText(transaction.getSubtitle());
        holder.tvAmount.setText(transaction.getAmount());
        holder.tvStatus.setText(transaction.getStatus());

        if (transaction.getTransactionId() != null && !transaction.getTransactionId().isEmpty()) {
            holder.tvTxId.setVisibility(View.VISIBLE);
            holder.tvTxId.setText("TXID: " + transaction.getTransactionId());
        } else {
            holder.tvTxId.setVisibility(View.GONE);
        }

        if ("deposit".equalsIgnoreCase(transaction.getType())) {
            holder.ivIcon.setImageResource(R.drawable.ic_deposit);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
        } else if ("withdraw".equalsIgnoreCase(transaction.getType())) {
            holder.ivIcon.setImageResource(R.drawable.ic_withdraw);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
        } else if ("p2p".equalsIgnoreCase(transaction.getType())) {
            holder.ivIcon.setImageResource(R.drawable.ic_link);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_purple));
            if (transaction.getAmount().startsWith("-")) {
                holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
            } else {
                holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
            }
        }

        // Status colors
        if ("Success".equalsIgnoreCase(transaction.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_success_bg);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_green));
        } else if ("Pending".equalsIgnoreCase(transaction.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
            holder.tvStatus.setTextColor(Color.parseColor("#FF8C42")); // Custom orange
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_failed_bg);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
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
        ImageView ivIcon;
        TextView tvTitle, tvSubtitle, tvAmount, tvStatus, tvTxId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
            tvTitle = itemView.findViewById(R.id.tv_transaction_title);
            tvSubtitle = itemView.findViewById(R.id.tv_transaction_subtitle);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvStatus = itemView.findViewById(R.id.tv_transaction_status);
            tvTxId = itemView.findViewById(R.id.tv_transaction_id);
        }
    }
}
