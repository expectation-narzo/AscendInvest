package com.ascend.invest.handlers;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ascend.invest.R;
import com.ascend.invest.databinding.SectionTeamBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TeamHandler {

    private final DatabaseReference mDatabase;
    private final SectionTeamBinding binding;
    private TeamAdapter adapter;
    private final List<TeamMember> fullTeamList = new ArrayList<>();
    private final List<TeamMember> filteredList = new ArrayList<>();
    private String currentUserId;
    private int activeFilterLevel = 0; // 0: All, 1: Lvl 1, 2: Lvl 2
    private boolean filterActiveOnly = false;

    public TeamHandler(SectionTeamBinding binding) {
        this.binding = binding;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        initUI();
    }

    private void initUI() {
        adapter = new TeamAdapter(filteredList);
        binding.rvTeamMembers.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        binding.rvTeamMembers.setAdapter(adapter);

        setupSearchAndFilters();
        setupNetworkPerformance();
    }

    private void setupNetworkPerformance() {
        if (binding.cardNetworkPerformance != null) {
            binding.cardNetworkPerformance.setOnClickListener(v -> {
                int activeMembers = 0;
                int totalMembers = fullTeamList.size();
                for (TeamMember m : fullTeamList) if (m.isActive()) activeMembers++;
                
                int inactiveMembers = totalMembers - activeMembers;
                double activeRate = totalMembers > 0 ? (activeMembers * 100.0 / totalMembers) : 0;
                
                String summary = String.format(java.util.Locale.getDefault(), 
                    "Network Performance:\n- Total Members: %d\n- Active Members: %d\n- Inactive Members: %d\n- Activity Rate: %.1f%%",
                    totalMembers, activeMembers, inactiveMembers, activeRate);
                
                new android.app.AlertDialog.Builder(binding.getRoot().getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                       .setTitle("Network Insights")
                       .setMessage(summary)
                       .setPositiveButton("Dismiss", null)
                       .show();
                
                if (binding.tvNetworkPerformanceDesc != null) {
                    binding.tvNetworkPerformanceDesc.setText(String.format(java.util.Locale.getDefault(), 
                        "Current activity rate: %.1f%%. Tap for detailed metrics.", activeRate));
                }
            });
        }
    }

    private void setupSearchAndFilters() {
        if (binding.etSearchTeam != null) {
            binding.etSearchTeam.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (binding.filterAll != null) binding.filterAll.setOnClickListener(v -> { activeFilterLevel = 0; filterActiveOnly = false; applyFilters(); updateFilterUI(v); });
        if (binding.filterLvl1 != null) binding.filterLvl1.setOnClickListener(v -> { activeFilterLevel = 1; filterActiveOnly = false; applyFilters(); updateFilterUI(v); });
        if (binding.filterLvl2 != null) binding.filterLvl2.setOnClickListener(v -> { activeFilterLevel = 2; filterActiveOnly = false; applyFilters(); updateFilterUI(v); });
        if (binding.filterActive != null) binding.filterActive.setOnClickListener(v -> { filterActiveOnly = true; applyFilters(); updateFilterUI(v); });
    }

    private void updateFilterUI(View activeView) {
        View[] filters = {binding.filterAll, binding.filterLvl1, binding.filterLvl2, binding.filterActive};
        for (View v : filters) {
            if (v != null) {
                if (v == activeView) {
                    v.setBackgroundResource(R.drawable.status_purple_bg);
                    if (v instanceof TextView) ((TextView) v).setTextColor(binding.getRoot().getContext().getColor(R.color.primary_purple));
                } else {
                    v.setBackgroundResource(R.drawable.bg_icon_grey);
                    v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9")));
                    if (v instanceof TextView) ((TextView) v).setTextColor(android.graphics.Color.parseColor("#64748B"));
                }
            }
        }
    }

    private void applyFilters() {
        String query = binding.etSearchTeam != null ? binding.etSearchTeam.getText().toString().toLowerCase().trim() : "";
        
        filteredList.clear();
        for (TeamMember member : fullTeamList) {
            boolean matchesSearch = member.getUsername().toLowerCase().contains(query) || 
                                   member.getEmail().toLowerCase().contains(query);
            boolean matchesLevel = filterActiveOnly || (activeFilterLevel == 0) || (member.getLevel() == activeFilterLevel);
            boolean matchesActive = !filterActiveOnly || member.isActive();
            
            if (matchesSearch && matchesLevel && matchesActive) {
                filteredList.add(member);
            }
        }
        adapter.updateList(filteredList);
    }

    public void fetchTeam(String userId) {
        this.currentUserId = userId;
        UserHandler.getInstance().getUsersByReferrer(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        processLevel1(snapshot);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void processLevel1(DataSnapshot level1Snapshot) {
        List<TeamMember> newTeamList = new ArrayList<>();
        int l1Count = (int) level1Snapshot.getChildrenCount();
        final int[] processedL1 = {0};

        if (l1Count == 0) {
            updateData(newTeamList);
            return;
        }

        for (DataSnapshot ds : level1Snapshot.getChildren()) {
            String uid = ds.getKey();
            String username = ds.child("username").getValue(String.class);
            String email = ds.child("email").getValue(String.class);
            boolean active = ds.child("active_plans").exists();
            
            newTeamList.add(new TeamMember(uid, username, email, 1, active));
            
            UserHandler.getInstance().getUsersByReferrer(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot l2ds : snapshot.getChildren()) {
                                String l2uid = l2ds.getKey();
                                String l2username = l2ds.child("username").getValue(String.class);
                                String l2email = l2ds.child("email").getValue(String.class);
                                boolean l2active = l2ds.child("active_plans").exists();
                                newTeamList.add(new TeamMember(l2uid, l2username, l2email, 2, l2active));
                            }
                            processedL1[0]++;
                            if (processedL1[0] == l1Count) {
                                updateData(newTeamList);
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {
                            processedL1[0]++;
                            if (processedL1[0] == l1Count) updateData(newTeamList);
                        }
                    });
        }
    }

    private void updateData(List<TeamMember> list) {
        fullTeamList.clear();
        fullTeamList.addAll(list);
        
        int lvl1 = 0;
        int lvl2 = 0;
        for (TeamMember m : list) {
            if (m.getLevel() == 1) lvl1++;
            else if (m.getLevel() == 2) lvl2++;
        }
        
        if (binding.tvTotalTeamCount != null) binding.tvTotalTeamCount.setText(String.valueOf(list.size()));
        if (binding.tvLvl1Count != null) binding.tvLvl1Count.setText(String.valueOf(lvl1));
        if (binding.tvLvl2Count != null) binding.tvLvl2Count.setText(String.valueOf(lvl2));

        boolean hasReferrals = !list.isEmpty();
        if (binding.headerReferrals != null) binding.headerReferrals.setVisibility(hasReferrals ? View.VISIBLE : View.GONE);
        if (binding.rvTeamMembers != null) binding.rvTeamMembers.setVisibility(hasReferrals ? View.VISIBLE : View.GONE);
        if (binding.emptyTeamState != null) binding.emptyTeamState.setVisibility(hasReferrals ? View.GONE : View.VISIBLE);
        
        applyFilters();
    }
}
