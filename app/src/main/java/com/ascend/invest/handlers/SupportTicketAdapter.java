package com.ascend.invest.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.R;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupportTicket ticket = ticketList.get(position);
        holder.tvTitle.setText(ticket.getTitle());
        holder.tvDesc.setText(ticket.getDescription());
        holder.tvStatus.setText(ticket.getStatus());

        String date = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(new Date(ticket.getTimestamp()));
        holder.tvDate.setText(date);

        if (ticket.getAdminReply() != null && !ticket.getAdminReply().isEmpty()) {
            holder.llAdminReply.setVisibility(View.VISIBLE);
            holder.tvAdminReply.setText(ticket.getAdminReply());
        } else {
            holder.llAdminReply.setVisibility(View.GONE);
        }

        // Status Colors
        switch (ticket.getStatus()) {
            case "Resolved":
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#22C55E"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_green_badge);
                break;
            case "In Progress":
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
                holder.tvStatus.setBackgroundResource(R.drawable.status_purple_bg);
                break;
            default: // Pending
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"));
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
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
        TextView tvTitle, tvDesc, tvStatus, tvAdminReply, tvDate;
        LinearLayout llAdminReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_ticket_title);
            tvDesc = itemView.findViewById(R.id.tv_ticket_desc);
            tvStatus = itemView.findViewById(R.id.tv_ticket_status);
            tvAdminReply = itemView.findViewById(R.id.tv_admin_reply);
            tvDate = itemView.findViewById(R.id.tv_ticket_date);
            llAdminReply = itemView.findViewById(R.id.ll_admin_reply);
        }
    }
}
