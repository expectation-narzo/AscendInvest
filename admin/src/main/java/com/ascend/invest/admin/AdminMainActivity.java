package com.ascend.invest.admin;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ascend.invest.admin.databinding.ActivityAdminMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {
    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupDashboard();
        fetchStats();
        initChart();
    }

    private void initChart() {
        LineChart chart = binding.mainStatsChart;
        List<Entry> entries = new ArrayList<>();
        // Sample data for high-tech look
        entries.add(new Entry(0, 10));
        entries.add(new Entry(1, 25));
        entries.add(new Entry(2, 18));
        entries.add(new Entry(3, 45));
        entries.add(new Entry(4, 38));
        entries.add(new Entry(5, 60));

        LineDataSet dataSet = new LineDataSet(entries, "Infrastructure Load");
        dataSet.setColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary_purple));
        dataSet.setCircleColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary_purple));
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.total_profit_chart_gradient));
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.animateX(1000);
        chart.invalidate();
    }

    private void setupDashboard() {
        binding.btnDeposits.setOnClickListener(v -> startRequestsActivity("deposit"));
        binding.btnWithdrawals.setOnClickListener(v -> startRequestsActivity("withdraw"));
        binding.btnTickets.setOnClickListener(v -> startActivity(new Intent(this, SupportTicketsActivity.class)));
        binding.btnPlans.setOnClickListener(v -> startActivity(new Intent(this, ManagePlansActivity.class)));
        binding.btnFaq.setOnClickListener(v -> startActivity(new Intent(this, ManageFaqActivity.class)));
        binding.btnLinks.setOnClickListener(v -> startActivity(new Intent(this, ManageLinksActivity.class)));
        binding.btnUserManagement.setOnClickListener(v -> startActivity(new Intent(this, UserSearchActivity.class)));
        binding.btnTransactionLookup.setOnClickListener(v -> startActivity(new Intent(this, TransactionSearchActivity.class)));
        binding.btnAnnouncements.setOnClickListener(v -> startActivity(new Intent(this, ManageAnnouncementsActivity.class)));
        binding.btnReferralConfig.setOnClickListener(v -> startActivity(new Intent(this, ManageReferralActivity.class)));
        binding.btnP2pManagement.setOnClickListener(v -> startActivity(new Intent(this, ManageP2PActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void fetchStats() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        
        // Total Users
        db.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { 
                binding.tvTotalUsers.setText(String.valueOf(s.getChildrenCount())); 
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Pending Deposits
        db.getReference("transactions/deposit_req").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { 
                binding.tvPendingDeposits.setText(String.valueOf(s.getChildrenCount())); 
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Pending Withdrawals
        db.getReference("transactions/withdraw_req").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { 
                binding.tvPendingWithdrawals.setText(String.valueOf(s.getChildrenCount())); 
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Total Plans
        db.getReference("plans").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { 
                binding.tvTotalPlans.setText(String.valueOf(s.getChildrenCount())); 
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void startRequestsActivity(String type) {
        Intent intent = new Intent(this, ManageRequestsActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }
}
