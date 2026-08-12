package com.ascend.invest.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityManagePlansBinding;
import com.ascend.invest.admin.databinding.DialogEditPlanBinding;
import com.ascend.invest.admin.databinding.ItemPlanAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ManagePlansActivity extends AppCompatActivity {
    private ActivityManagePlansBinding binding;
    private DatabaseReference mPlansRef;
    private List<Plan> planList = new ArrayList<>();
    private PlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityManagePlansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mPlansRef = FirebaseDatabase.getInstance().getReference("plans");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new PlanAdapter();
        binding.rvPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPlans.setAdapter(adapter);

        binding.fabAddPlan.setOnClickListener(v -> showPlanDialog(null));

        fetchPlans();
    }

    private void fetchPlans() {
        mPlansRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                planList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Plan plan = ds.getValue(Plan.class);
                    if (plan != null) {
                        plan.setId(ds.getKey());
                        planList.add(plan);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showPlanDialog(Plan plan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogEditPlanBinding dialogBinding = DialogEditPlanBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();

        if (plan != null) {
            dialogBinding.tvDialogTitle.setText("Edit Plan");
            dialogBinding.etPlanName.setText(plan.getName());
            dialogBinding.etInvestAmount.setText(String.valueOf(plan.getInvestAmount()));
            dialogBinding.etDailyProfit.setText(String.valueOf(plan.getDailyProfit()));
            dialogBinding.etDuration.setText(String.valueOf(plan.getDurationDays()));
            dialogBinding.etDescription.setText(plan.getDescription());
            dialogBinding.etCategory.setText(plan.getCategory());
            dialogBinding.etPurchaseLimit.setText(String.valueOf(plan.getPurchaseLimit()));
            dialogBinding.switchFeatured.setChecked(plan.isFeatured());
            dialogBinding.switchActiveDialog.setChecked(plan.isActive());
        } else {
            dialogBinding.tvDialogTitle.setText("Create New Plan");
            dialogBinding.etPurchaseLimit.setText("1");
            dialogBinding.switchActiveDialog.setChecked(true);
        }

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSave.setOnClickListener(v -> {
            String name = dialogBinding.etPlanName.getText().toString().trim();
            String investStr = dialogBinding.etInvestAmount.getText().toString().trim();
            String dailyStr = dialogBinding.etDailyProfit.getText().toString().trim();
            String durStr = dialogBinding.etDuration.getText().toString().trim();
            String desc = dialogBinding.etDescription.getText().toString().trim();
            String cat = dialogBinding.etCategory.getText().toString().trim();
            String limitStr = dialogBinding.etPurchaseLimit.getText().toString().trim();
            boolean featured = dialogBinding.switchFeatured.isChecked();
            boolean active = dialogBinding.switchActiveDialog.isChecked();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(investStr) || TextUtils.isEmpty(dailyStr) || TextUtils.isEmpty(durStr)) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double invest = Double.parseDouble(investStr);
                double daily = Double.parseDouble(dailyStr);
                int duration = Integer.parseInt(durStr);
                int limit = limitStr.isEmpty() ? 1 : Integer.parseInt(limitStr);

                String id = (plan != null) ? plan.getId() : mPlansRef.push().getKey();
                
                Plan newPlan = new Plan();
                newPlan.setId(id);
                newPlan.setName(name);
                newPlan.setDescription(desc);
                newPlan.setInvestAmount(invest);
                newPlan.setDailyProfit(daily);
                newPlan.setDurationDays(duration);
                newPlan.setCategory(cat);
                newPlan.setPurchaseLimit(limit);
                newPlan.setFeatured(featured);
                newPlan.setActive(active);
                
                // Recalculate read-only fields
                double totalProfit = daily * duration;
                newPlan.setTotalProfit(totalProfit);
                if (invest > 0) {
                    newPlan.setProfitPercentage((totalProfit / invest) * 100);
                }

                if (id != null) {
                    mPlansRef.child(id).setValue(newPlan).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Plan Saved Successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemPlanAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Plan plan = planList.get(position);
            holder.binding.tvPlanName.setText(plan.getName());
            holder.binding.tvPlanPrice.setText("Investment: $" + plan.getInvestAmount());
            holder.binding.tvPlanDetails.setText("Daily Profit: $" + plan.getDailyProfit() + " | Duration: " + plan.getDurationDays() + " Days");
            
            holder.binding.switchActive.setChecked(plan.isActive());
            holder.binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mPlansRef.child(plan.getId()).child("active").setValue(isChecked);
            });

            holder.binding.btnEdit.setOnClickListener(v -> showPlanDialog(plan));
            holder.binding.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(ManagePlansActivity.this)
                    .setTitle("Delete Plan")
                    .setMessage("Are you sure you want to delete this plan?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        mPlansRef.child(plan.getId()).removeValue();
                        Toast.makeText(ManagePlansActivity.this, "Plan Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }

        @Override
        public int getItemCount() {
            return planList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemPlanAdminBinding binding;
            ViewHolder(ItemPlanAdminBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
