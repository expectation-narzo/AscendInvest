package com.ascend.invest.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.R;
import com.ascend.invest.databinding.ItemSupportTicketBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupportTicketAdapter extends RecyclerView.Adapter<SupportTicketAdapter.ViewHolder> {

    private List<SupportTicket> ticketList;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(SupportTicket ticket);
    }

    public SupportTicketAdapter(List<SupportTicket> ticketList, OnTicketClickListener listener) {
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSupportTicketBinding binding = ItemSupportTicketBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupportTicket ticket = ticketList.get(position);
        holder.binding.tvTicketTitle.setText(ticket.getTitle());
        holder.binding.tvTicketDesc.setText(ticket.getDescription());
        holder.binding.tvTicketStatus.setText(ticket.getStatus());

        String date = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(new Date(ticket.getTimestamp()));
        holder.binding.tvTicketDate.setText(date);

        if (ticket.getAdminReply() != null && !ticket.getAdminReply().isEmpty()) {
            holder.binding.llAdminReply.setVisibility(View.VISIBLE);
            holder.binding.tvAdminReply.setText(ticket.getAdminReply());
        } else {
            holder.binding.llAdminReply.setVisibility(View.GONE);
        }

        // Status Colors
        switch (ticket.getStatus()) {
            case "Resolved":
                holder.binding.tvTicketStatus.setTextColor(android.graphics.Color.parseColor("#22C55E"));
                holder.binding.tvTicketStatus.setBackgroundResource(R.drawable.bg_green_badge);
                break;
            case "In Progress":
                holder.binding.tvTicketStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
                holder.binding.tvTicketStatus.setBackgroundResource(R.drawable.status_purple_bg);
                break;
            default: // Pending
                holder.binding.tvTicketStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"));
                holder.binding.tvTicketStatus.setBackgroundResource(R.drawable.status_pending_bg);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTicketClick(ticket);
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSupportTicketBinding binding;

        public ViewHolder(@NonNull ItemSupportTicketBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
