package com.ascend.invest.handlers;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TeamHandler {

    private final DatabaseReference mDatabase;
    private TeamAdapter adapter;
    private final List<TeamMember> fullTeamList = new ArrayList<>();
    private final List<TeamMember> filteredList = new ArrayList<>();
    private final View root;
    private String currentUserId;
    private TextView tvTotalTeam, tvLvl1Count, tvLvl2Count;
    private EditText etSearch;
    private View headerReferrals, emptyState;
    private int activeFilterLevel = 0; // 0: All, 1: Lvl 1, 2: Lvl 2

    public TeamHandler(View root) {
        this.root = root;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        initUI();
    }

    private void initUI() {
        RecyclerView rvTeam = root.findViewById(R.id.rv_team_members);
        tvTotalTeam = root.findViewById(R.id.tv_total_team_count);
        tvLvl1Count = root.findViewById(R.id.tv_lvl1_count);
        tvLvl2Count = root.findViewById(R.id.tv_lvl2_count);
        etSearch = root.findViewById(R.id.et_search_team);
        headerReferrals = root.findViewById(R.id.header_referrals);
        emptyState = root.findViewById(R.id.empty_team_state);

        adapter = new TeamAdapter(filteredList);
        rvTeam.setLayoutManager(new LinearLayoutManager(root.getContext()));
        rvTeam.setAdapter(adapter);

        setupSearchAndFilters();
        setupNetworkPerformance();
    }

    private void setupNetworkPerformance() {
        View cardPerformance = root.findViewById(R.id.card_network_performance);
        if (cardPerformance != null) {
            cardPerformance.setOnClickListener(v -> {
                int activeMembers = 0;
                int totalMembers = fullTeamList.size();
                for (TeamMember m : fullTeamList) if (m.isActive()) activeMembers++;
                
                int inactiveMembers = totalMembers - activeMembers;
                double activeRate = totalMembers > 0 ? (activeMembers * 100.0 / totalMembers) : 0;
                
                String summary = String.format(java.util.Locale.getDefault(), 
                    "Network Performance:\n- Total Members: %d\n- Active Members: %d\n- Inactive Members: %d\n- Activity Rate: %.1f%%",
                    totalMembers, activeMembers, inactiveMembers, activeRate);
                
                new android.app.AlertDialog.Builder(root.getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                       .setTitle("Network Insights")
                       .setMessage(summary)
                       .setPositiveButton("Dismiss", null)
                       .show();
                
                TextView tvDesc = root.findViewById(R.id.tv_network_performance_desc);
                if (tvDesc != null) {
                    tvDesc.setText(String.format(java.util.Locale.getDefault(), 
                        "Current activity rate: %.1f%%. Tap for detailed metrics.", activeRate));
                }
            });
        }
    }

    private void setupSearchAndFilters() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
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

        View filterAll = root.findViewById(R.id.filter_all);
        View filterLvl1 = root.findViewById(R.id.filter_lvl1);
        View filterLvl2 = root.findViewById(R.id.filter_lvl2);
        View filterActive = root.findViewById(R.id.filter_active);

        if (filterAll != null) filterAll.setOnClickListener(v -> { activeFilterLevel = 0; filterActiveOnly = false; applyFilters(); updateFilterUI(v); });
        if (filterLvl1 != null) filterLvl1.setOnClickListener(v -> { activeFilterLevel = 1; filterActiveOnly = false; applyFilters(); updateFilterUI(v); });
        if (filterLvl2 != null) filterLvl2.setOnClickListener(v -> { activeFilterLevel = 2; filterActiveOnly = false; applyFilters(); updateFilterUI(v); });
        if (filterActive != null) filterActive.setOnClickListener(v -> { filterActiveOnly = true; applyFilters(); updateFilterUI(v); });
    }

    private void updateFilterUI(View activeView) {
        int[] filterIds = {R.id.filter_all, R.id.filter_lvl1, R.id.filter_lvl2, R.id.filter_active};
        for (int id : filterIds) {
            View v = root.findViewById(id);
            if (v != null) {
                if (v == activeView) {
                    v.setBackgroundResource(R.drawable.status_purple_bg);
                    if (v instanceof TextView) ((TextView) v).setTextColor(root.getContext().getColor(R.color.primary_purple));
                } else {
                    v.setBackgroundResource(R.drawable.bg_icon_grey);
                    v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9")));
                    if (v instanceof TextView) ((TextView) v).setTextColor(android.graphics.Color.parseColor("#64748B"));
                }
            }
        }
    }

    private boolean filterActiveOnly = false;

    private void applyFilters() {
        String query = etSearch != null ? etSearch.getText().toString().toLowerCase().trim() : "";
        
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
        
        // Use UserHandler for queries
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
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            processedL1[0]++;
                            if (processedL1[0] == l1Count) {
                                updateData(newTeamList);
                            }
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
        
        if (tvTotalTeam != null) tvTotalTeam.setText(String.valueOf(list.size()));
        if (tvLvl1Count != null) tvLvl1Count.setText(String.valueOf(lvl1));
        if (tvLvl2Count != null) tvLvl2Count.setText(String.valueOf(lvl2));

        // Update Visibility based on referral count
        boolean hasReferrals = !list.isEmpty();
        if (headerReferrals != null) headerReferrals.setVisibility(hasReferrals ? View.VISIBLE : View.GONE);
        View rvTeam = root.findViewById(R.id.rv_team_members);
        if (rvTeam != null) rvTeam.setVisibility(hasReferrals ? View.VISIBLE : View.GONE);
        if (emptyState != null) emptyState.setVisibility(hasReferrals ? View.GONE : View.VISIBLE);
        
        applyFilters();
    }
}
