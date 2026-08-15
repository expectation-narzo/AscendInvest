package com.ascend.invest.handlers;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.ascend.invest.databinding.ItemTeamMemberBinding;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<TeamMember> teamMembers;

    public TeamAdapter(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamMemberBinding binding = ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TeamViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamMember member = teamMembers.get(position);
        holder.binding.tvTeamUsername.setText(member.getUsername());
        holder.binding.tvTeamEmail.setText(member.getEmail());
        holder.binding.tvMemberLevel.setText("Lvl " + member.getLevel());
        
        if (member.getLevel() == 2) {
            holder.binding.tvMemberLevel.setTextColor(android.graphics.Color.parseColor("#7367F0"));
            holder.binding.tvMemberLevel.setBackgroundResource(R.drawable.status_purple_bg);
        } else {
            holder.binding.tvMemberLevel.setTextColor(android.graphics.Color.parseColor("#28C76F"));
            holder.binding.tvMemberLevel.setBackgroundResource(R.drawable.status_success_bg);
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
        final ItemTeamMemberBinding binding;

        public TeamViewHolder(@NonNull ItemTeamMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
