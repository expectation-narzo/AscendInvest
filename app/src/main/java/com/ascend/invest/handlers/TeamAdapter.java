package com.ascend.invest.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<TeamMember> teamMembers;

    public TeamAdapter(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamMember member = teamMembers.get(position);
        holder.tvUsername.setText(member.getUsername());
        holder.tvEmail.setText(member.getEmail());
        holder.tvLevel.setText("Lvl " + member.getLevel());
        
        if (member.getLevel() == 2) {
            holder.tvLevel.setTextColor(android.graphics.Color.parseColor("#7367F0"));
            holder.tvLevel.setBackgroundResource(R.drawable.status_purple_bg);
        } else {
            holder.tvLevel.setTextColor(android.graphics.Color.parseColor("#28C76F"));
            holder.tvLevel.setBackgroundResource(R.drawable.status_success_bg);
        }
    }

    @Override
    public int getItemCount() {
        return teamMembers.size();
    }

    public void updateList(List<TeamMember> newList) {
        this.teamMembers = newList;
        notifyDataSetChanged();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvLevel;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_team_username);
            tvEmail = itemView.findViewById(R.id.tv_team_email);
            tvLevel = itemView.findViewById(R.id.tv_member_level);
        }
    }
}
