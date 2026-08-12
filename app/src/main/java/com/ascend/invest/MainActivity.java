package com.ascend.invest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.handlers.ChartHandler;
import com.ascend.invest.handlers.DepositHandler;
import com.ascend.invest.handlers.FAQ;
import com.ascend.invest.handlers.FAQAdapter;
import com.ascend.invest.handlers.PlanHandler;
import com.ascend.invest.handlers.ReferralHandler;
import com.ascend.invest.handlers.SecurityManager;
import com.ascend.invest.handlers.SupportTicket;
import com.ascend.invest.handlers.SupportTicketAdapter;
import com.ascend.invest.handlers.TeamHandler;
import com.ascend.invest.handlers.TransactionHandler;
import com.ascend.invest.handlers.UserHandler;
import com.ascend.invest.handlers.WalletAddressHandler;
import com.ascend.invest.handlers.WithdrawalHandler;
import com.ascend.invest.QR.PremiumQRGenerator;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String username = "user";
    private TransactionHandler transactionHandler;
    private PlanHandler planHandler;
    private DepositHandler depositHandler;
    private WithdrawalHandler withdrawalHandler;
    private ChartHandler depositChartHandler;
    private ChartHandler profitChartHandler;
    private ChartHandler yieldChartHandler;
    private TeamHandler teamHandler;
    private ReferralHandler referralHandler;
    private SupportTicketAdapter ticketAdapter;
    private java.util.List<SupportTicket> ticketList = new ArrayList<>();
    private java.util.List<FAQ> allFaqs = new ArrayList<>();
    private java.util.List<FAQ> displayedFaqs = new ArrayList<>();
    private FAQAdapter faqAdapter;
    private String telegramSupportUrl;
    private String supportEmail;
    private String sessionWalletAddress = null;
    private java.util.List<com.ascend.invest.handlers.Announcement> announcements = new ArrayList<>();
    private com.ascend.invest.handlers.P2PHandler p2pHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Notification Channels
        com.ascend.invest.handlers.NotificationHelper.createNotificationChannels(this);

        if (savedInstanceState != null) {
            sessionWalletAddress = savedInstanceState.getString("session_wallet_address");
        }

        // Perform Integrity Shield Scan
   //     SecurityManager.validateAppIntegrity(this);

        setContentView(R.layout.activity_home);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        
        // Set dark icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        getWindow().setNavigationBarColor(Color.parseColor("#00000000"));

        requestNotificationPermission();
        initialize_firebase();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Enable notifications to receive profit alerts!", Toast.LENGTH_LONG).show();
                    }
                }).launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // User status handled by onDisconnect and explicit logout to allow background activity
    }

    private void updateUserStatus(boolean online) {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            DatabaseReference statusRef = mDatabase.child("users").child(uid).child("status");
            DatabaseReference lastSeenRef = mDatabase.child("users").child(uid).child("lastSeen");

            if (online) {
                statusRef.setValue("online");
                statusRef.onDisconnect().setValue("offline");
                lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
            } else {
                statusRef.setValue("offline");
                lastSeenRef.setValue(ServerValue.TIMESTAMP);
            }
        }
    }

    private void initialize_firebase(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (mAuth.getCurrentUser() != null) {
            // Force reload to check if account still exists in Auth
            mAuth.getCurrentUser().reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    fetchUsernameAndInitialize(mAuth.getCurrentUser().getUid());
                } else {
                    // Account might have been deleted from Auth
                    handleAccountMissing();
                }
            });
        } else {
            finishAffinity();
        }
    }

    private void fetchAppWalletAddress(String userId) {
        if (sessionWalletAddress != null) {
            handle_deposit_address_qr(sessionWalletAddress, userId);
            return;
        }

        new WalletAddressHandler().getRandomAddress(new WalletAddressHandler.WalletAddressCallback() {
            @Override
            public void onAddressReceived(String address) {
                sessionWalletAddress = address;
                handle_deposit_address_qr(address, userId);
            }

            @Override
            public void onError(String error) {
                // Fallback or retry?
                Log.e("DEPOSIT", "Wallet error: " + error);
                Toast.makeText(MainActivity.this, "Error fetching deposit wallet: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("session_wallet_address", sessionWalletAddress);
    }
    private void fetchUsernameAndInitialize(String userId) {
        UserHandler.getInstance().getUserRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Single Device Check
                    String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                    String storedDeviceId = snapshot.child("currentDeviceId").getValue(String.class);
                    if (storedDeviceId != null && !storedDeviceId.equals(deviceId)) {
                        handleAccountMissing();
                        return;
                    }

                    // User exists, get username if available
                    if (snapshot.hasChild("username")) {
                        username = snapshot.child("username").getValue(String.class);
                    }
                    fetchAppWalletAddress(userId);
                    initializeUI(userId);
                } else {
                    // User record not found in database - account likely deleted
                    handleAccountMissing();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If permission denied or other error, safety first
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    handleAccountMissing();
                } else {
                    // For other errors (offline), try to proceed with what we have
                    fetchAppWalletAddress(userId);
                    initializeUI(userId);
                }
            }
        });
    }

    private void handleAccountMissing() {
        if (mAuth != null) {
            updateUserStatus(false);
            mAuth.signOut();
        }
        Intent intent = new Intent(this, com.ascend.invest.auth.AuthenticateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Account not found or deleted", Toast.LENGTH_LONG).show();
    }

    private void initializeUI(String userId) {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Update Drawer Info
        TextView drawerUsername = findViewById(R.id.drawer_username);
        TextView drawerUserid = findViewById(R.id.drawer_userid);
        if (drawerUsername != null) drawerUsername.setText(username);
        if (drawerUserid != null) {
            String shortId = userId.length() > 8 ? userId.substring(0, 8) : userId;
            drawerUserid.setText("ID: " + shortId + "...");
            drawerUserid.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("User UID", userId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "UID copied to clipboard", Toast.LENGTH_SHORT).show();
            });
        }

        initializeDrawerButtons(drawerLayout);
        
        p2pHandler = new com.ascend.invest.handlers.P2PHandler(this, userId);

        // Prioritize Dashboard listeners for better initial experience
        setupDashboardBalanceListeners(userId);
        
        // Delay other initializations slightly to prevent UI hang on startup
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            transaction_control(userId);
            plan_control(userId);
            team_control(userId);
            referral_control(userId);
            p2pHandler.setupP2PSection(findViewById(android.R.id.content));
            setupCharts(userId);
            fetchSupportTickets(userId);
            setupAnnouncements(userId);
            fetchLeaderboard();
        }, 500);
    }

    private void fetchLeaderboard() {
        LinearLayout container = findViewById(R.id.ll_leaderboard_container);
        if (container == null) return;

        mDatabase.child("users").orderByChild("total_profit").limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                java.util.List<DataSnapshot> users = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) users.add(ds);
                Collections.reverse(users);

                int rank = 1;
                for (DataSnapshot userSnap : users) {
                    View itemView = getLayoutInflater().inflate(R.layout.item_leaderboard, container, false);
                    TextView tvRank = itemView.findViewById(R.id.tv_rank);
                    TextView tvName = itemView.findViewById(R.id.tv_rank_name);
                    TextView tvEarnings = itemView.findViewById(R.id.tv_earnings);
                    
                    String name = userSnap.child("username").getValue(String.class);
                    Object profitVal = userSnap.child("total_profit").getValue();
                    double profit = 0.0;
                    if (profitVal instanceof Number) profit = ((Number) profitVal).doubleValue();
                    
                    if (name != null) {
                        tvRank.setText(String.valueOf(rank));
                        if (rank == 1) {
                            tvRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FBBF24")));
                            tvRank.setTextColor(Color.WHITE);
                        } else if (rank == 2) {
                            tvRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#94A3B8")));
                            tvRank.setTextColor(Color.WHITE);
                        } else if (rank == 3) {
                            tvRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#B45309")));
                            tvRank.setTextColor(Color.WHITE);
                        }

                        tvName.setText(name);
                        tvEarnings.setText("$" + String.format(Locale.US, "%.2f", profit));
                        container.addView(itemView);
                        rank++;
                    }
                }
                
                if (container.getChildCount() == 0) {
                    TextView empty = new TextView(MainActivity.this);
                    empty.setText("Competitive season starting soon...");
                    empty.setGravity(android.view.Gravity.CENTER);
                    empty.setPadding(0, 40, 0, 40);
                    container.addView(empty);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


    }

    private void updateBadges(long referralCount, double totalProfit) {
        View b1 = findViewById(R.id.badge_newbie);
        View b2 = findViewById(R.id.badge_pro);
        View b3 = findViewById(R.id.badge_whale);
        
        // Dynamic Badge Activation
        if (b1 != null && (referralCount >= 1 || totalProfit >= 10)) b1.setAlpha(1.0f);
        if (b2 != null && (referralCount >= 10 || totalProfit >= 500)) b2.setAlpha(1.0f);
        if (b3 != null && (referralCount >= 50 || totalProfit >= 5000)) b3.setAlpha(1.0f);
    }

    private void setupAnnouncements(String userId) {
        View notificationBtn = findViewById(R.id.notification_button);
        View redDot = findViewById(R.id.notification_dot);
        if (notificationBtn == null || redDot == null) return;

        mDatabase.child("announcement").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                announcements.clear();
                long lastSeenTime = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("last_announcement_time", 0);
                boolean hasNew = false;
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    com.ascend.invest.handlers.Announcement a = ds.getValue(com.ascend.invest.handlers.Announcement.class);
                    if (a != null) {
                        announcements.add(a);
                        if (a.getTimestamp() > lastSeenTime) hasNew = true;
                    }
                }
                Collections.reverse(announcements);
                redDot.setVisibility(hasNew ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        notificationBtn.setOnClickListener(v -> {
            if (announcements.isEmpty()) {
                Toast.makeText(this, "No announcements yet", Toast.LENGTH_SHORT).show();
                return;
            }

            redDot.setVisibility(View.GONE);
            if (!announcements.isEmpty()) {
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                    .putLong("last_announcement_time", announcements.get(0).getTimestamp()).apply();
            }

            showAnnouncementPopup(notificationBtn);
        });
    }

    private void showAnnouncementPopup(View anchor) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, anchor);
        for (int i = 0; i < Math.min(announcements.size(), 5); i++) {
            com.ascend.invest.handlers.Announcement a = announcements.get(i);
            popup.getMenu().add(0, i, i, a.getTitle());
        }
        
        popup.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            com.ascend.invest.handlers.Announcement a = announcements.get(index);
            
            com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
            builder.setTitle(a.getTitle())
                   .setMessage(a.getMessage())
                   .setPositiveButton("OK", null)
                   .show();
            return true;
        });
        popup.show();
    }

    private void fetchSupportTickets(String userId) {
        mDatabase.child("support_tickets").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    SupportTicket ticket = ds.getValue(SupportTicket.class);
                    if (ticket != null) {
                        ticket.setId(ds.getKey());
                        ticketList.add(ticket);
                    }
                }
                Collections.reverse(ticketList);
                if (ticketAdapter != null) ticketAdapter.notifyDataSetChanged();
                
                View container = findViewById(R.id.ll_my_tickets_container);
                if (container != null) {
                    container.setVisibility(ticketList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupCharts(String userId) {
        LineChart depositChart = findViewById(R.id.total_deposit_line_chart);
        if (depositChart != null) {
            depositChartHandler = new ChartHandler(depositChart, "deposit", "#FF8C42", R.drawable.total_deposit_chart_gradient);
            depositChartHandler.init(userId);
        }

        LineChart profitChart = findViewById(R.id.salesChart);
        if (profitChart != null) {
            // Net Earnings - Plan Profit Only - Green
            profitChartHandler = new ChartHandler(profitChart, "profit", "#28C76F", R.drawable.total_net_earning_chart_gradient, "Profit");
            profitChartHandler.init(userId);
        }

        LineChart yieldChart = findViewById(R.id.yield_line_chart);
        if (yieldChart != null) {
            // Yield Analytics - Both - Purple
            yieldChartHandler = new ChartHandler(yieldChart, "profit", "#6C5CE7", R.drawable.total_profit_chart_gradient);
            yieldChartHandler.init(userId);
        }
    }

    private void setupDashboardBalanceListeners(String userId) {
        TextView tvWalletBalance = findViewById(R.id.tv_wallet_balance_value);
        TextView tvTotalDeposit = findViewById(R.id.tv_total_deposit_value);
        TextView tvTotalProfit = findViewById(R.id.tv_total_profit_value);
        
        TextView tvLockedValue = findViewById(R.id.tv_locked_value);
        TextView tvUnlockedValue = findViewById(R.id.tv_unlocked_value);
        com.google.android.material.progressindicator.LinearProgressIndicator pbLocked = findViewById(R.id.pb_locked);
        com.google.android.material.progressindicator.LinearProgressIndicator pbUnlocked = findViewById(R.id.pb_unlocked);

        // New Balance TextViews in Dashboard/Deposit/Withdraw sections
        TextView tvDepositBalance = findViewById(R.id.tv_deposit_wallet_balance);
        TextView tvWithdrawAvailable = findViewById(R.id.tv_withdraw_available_balance);
        TextView tvP2PAvailable = findViewById(R.id.tv_p2p_section_available);
        TextView tvYieldTotal = findViewById(R.id.tvTotalValue);
        
        // Earning Report Metrics
        TextView tvEarningMetric = findViewById(R.id.tv_earning_metric_value);
        TextView tvProfitMetric = findViewById(R.id.tv_profit_metric_value);
        com.google.android.material.progressindicator.LinearProgressIndicator pbEarningMetric = findViewById(R.id.pb_earning_metric);
        com.google.android.material.progressindicator.LinearProgressIndicator pbProfitMetric = findViewById(R.id.pb_profit_metric);

        UserHandler.getInstance().listenToUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Check for single device login
                    String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                    String storedDeviceId = snapshot.child("currentDeviceId").getValue(String.class);
                    if (storedDeviceId != null && !storedDeviceId.equals(deviceId)) {
                        Toast.makeText(MainActivity.this, "Security Alert: Logged in from another device", Toast.LENGTH_LONG).show();
                        handleAccountMissing();
                        return;
                    }

                    // Helper to safely get double from snapshot
                    java.util.function.BiFunction<DataSnapshot, String, Double> getSumFromTransactions = (transactionsSnap, titleFilter) -> {
                        double runningSum = 0;
                        if (transactionsSnap.exists()) {
                            for (DataSnapshot ds : transactionsSnap.getChildren()) {
                                if ("Success".equals(ds.child("status").getValue())) {
                                    String title = ds.child("title").getValue(String.class);
                                    if (titleFilter == null || (title != null && title.contains(titleFilter))) {
                                        Object amtVal = ds.child("amount").getValue();
                                        if (amtVal != null) {
                                            try {
                                                String clean = amtVal.toString().replaceAll("[^0-9.]", "");
                                                runningSum += Double.parseDouble(clean);
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            }
                        }
                        return runningSum;
                    };

                    java.util.function.Function<DataSnapshot, Double> getDouble = (ds) -> {
                        if (!ds.exists()) return 0.0;
                        Object val = ds.getValue();
                        if (val instanceof Number) return ((Number) val).doubleValue();
                        return 0.0;
                    };

                    // Wallet Balance
                    double curWalBal = getDouble.apply(snapshot.child("wallet_balance"));
                    String formattedBalance = "$" + String.format("%.2f", curWalBal);
                    if (tvWalletBalance != null) tvWalletBalance.setText(formattedBalance);
                    if (tvDepositBalance != null) tvDepositBalance.setText(formattedBalance);

                    // Total Deposit (Total Capital) - Sum only success ones
                    double curTotDep = getSumFromTransactions.apply(snapshot.child("transactions").child("deposit"), null);
                    if (tvTotalDeposit != null) tvTotalDeposit.setText("$" + String.format("%.2f", curTotDep));

                    // Total Profit (Yield) - Both Plans and Refer
                    double curTotProf = getSumFromTransactions.apply(snapshot.child("transactions").child("profit"), null);
                    
                    // Plan Profit Only
                    double planProfit = getSumFromTransactions.apply(snapshot.child("transactions").child("profit"), "Profit");

                    String formattedProfit = "$" + String.format("%.2f", curTotProf);
                    if (tvTotalProfit != null) tvTotalProfit.setText(formattedProfit);
                    if (tvYieldTotal != null) tvYieldTotal.setText(formattedProfit);
                    
                    if (tvEarningMetric != null) tvEarningMetric.setText(formattedProfit);
                    if (tvProfitMetric != null) tvProfitMetric.setText("$" + String.format("%.2f", planProfit));

                    // Unlocked Balance (Profit available for withdrawal)
                    double curUnlBal = getDouble.apply(snapshot.child("unlocked_balance"));
                    
                    // Capping unlocked balance by wallet balance
                    curUnlBal = Math.min(curWalBal, curUnlBal);
                    
                    // Locked Balance (Deposits not yet invested)
                    double curLocBal = Math.max(0, curWalBal - curUnlBal);
                    
                    if (tvLockedValue != null) tvLockedValue.setText("$" + String.format("%.2f", curLocBal));
                    if (tvUnlockedValue != null) tvUnlockedValue.setText("$" + String.format("%.2f", curUnlBal));
                    if (tvWithdrawAvailable != null) tvWithdrawAvailable.setText("$" + String.format("%.2f", curUnlBal));
                    if (tvP2PAvailable != null) tvP2PAvailable.setText("$" + String.format("%.2f", curUnlBal));
                    
                    if (curWalBal > 0) {
                        int unlockedPercent = (int) ((curUnlBal / curWalBal) * 100);
                        int lockedPercent = (int) ((curLocBal / curWalBal) * 100);
                        if (pbUnlocked != null) pbUnlocked.setProgress(unlockedPercent);
                        if (pbLocked != null) pbLocked.setProgress(lockedPercent);
                    } else {
                        if (pbUnlocked != null) pbUnlocked.setProgress(0);
                        if (pbLocked != null) pbLocked.setProgress(0);
                    }

                    // Earning Report Progress
                    if (curTotProf > 0) {
                        int planPercent = (int) ((planProfit / curTotProf) * 100);
                        if (pbProfitMetric != null) pbProfitMetric.setProgress(planPercent);
                        if (pbEarningMetric != null) pbEarningMetric.setProgress(100);
                    } else {
                        if (pbProfitMetric != null) pbProfitMetric.setProgress(0);
                        if (pbEarningMetric != null) pbEarningMetric.setProgress(0);
                    }

                    // Update Badges with current stats
                    mDatabase.child("users").orderByChild("referredBy").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            updateBadges(snapshot.getChildrenCount(), curTotProf);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                } else {
                    handleAccountMissing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    handleAccountMissing();
                }
            }
        });
    }



    private void initializeDrawerButtons(DrawerLayout drawerLayout) {
        findViewById(R.id.dashboard_btn).setOnClickListener(v -> {
            showSection(R.id.section_dashboard, R.id.dashboard_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        findViewById(R.id.refer_btn).setOnClickListener(v -> {
            showSection(R.id.section_refer, R.id.refer_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        findViewById(R.id.deposit_btn).setOnClickListener(v -> {
            showSection(R.id.section_deposits, R.id.deposit_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        findViewById(R.id.withdraw_btn).setOnClickListener(v -> {
            showSection(R.id.section_withdraw, R.id.withdraw_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.p2p_btn).setOnClickListener(v -> {
            showSection(R.id.section_p2p, R.id.p2p_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.team_btn).setOnClickListener(v -> {
            showSection(R.id.section_team, R.id.team_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.plan_btn).setOnClickListener(v -> {
            showSection(R.id.section_plan, R.id.plan_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.support_btn).setOnClickListener(v -> {
            showSection(R.id.section_support, R.id.support_btn);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.logout_btn).setOnClickListener(v -> {
            updateUserStatus(false);
            mAuth.signOut();
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, com.ascend.invest.auth.AuthenticateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        // Default selection
        updateDrawerSelection(R.id.dashboard_btn);
        setupSupportSection();
    }

    private void setupSupportSection() {
        RecyclerView rvTickets = findViewById(R.id.rv_support_tickets);
        if (rvTickets != null) {
            ticketAdapter = new SupportTicketAdapter(ticketList, this::showTicketDetail);
            rvTickets.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            rvTickets.setAdapter(ticketAdapter);
        }

        initFaqs();
        fetchSupportUrls();

        View telegramBtn = findViewById(R.id.btn_telegram_support);
        if (telegramBtn != null) {
            telegramBtn.setOnClickListener(v -> {
                if (telegramSupportUrl == null || telegramSupportUrl.isEmpty()) {
                    Toast.makeText(this, "Support link not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = telegramSupportUrl;
                // Auto-format username into a web link if needed
                if (!url.startsWith("http") && !url.startsWith("tg://")) {
                    if (url.startsWith("@")) url = "https://t.me/" + url.substring(1);
                    else url = "https://t.me/" + url;
                }

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open support link", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View emailBtn = findViewById(R.id.btn_email_support);
        if (emailBtn != null) {
            emailBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(android.net.Uri.parse("mailto:" + supportEmail));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Support Inquiry - " + username);
                try {
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } catch (Exception e) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View openTicketBtn = findViewById(R.id.btn_open_ticket);
        if (openTicketBtn != null) {
            openTicketBtn.setOnClickListener(v -> showCreateTicketDialog());
        }
    }

    private void showTicketDetail(SupportTicket ticket) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_ticket_detail, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvStatus = view.findViewById(R.id.tv_detail_status);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);
        TextView tvDesc = view.findViewById(R.id.tv_detail_desc);
        TextView tvAdminReply = view.findViewById(R.id.tv_detail_admin_reply);
        View llAdminReply = view.findViewById(R.id.ll_detail_admin_reply);

        tvTitle.setText(ticket.getTitle());
        tvStatus.setText(ticket.getStatus());
        tvDesc.setText(ticket.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
        String date = dateFormat.format(new Date(ticket.getTimestamp()));
        tvDate.setText(date);

        if (ticket.getAdminReply() != null && !ticket.getAdminReply().isEmpty()) {
            llAdminReply.setVisibility(View.VISIBLE);
            tvAdminReply.setText(ticket.getAdminReply());
        } else {
            llAdminReply.setVisibility(View.GONE);
        }

        // Status Styling
        switch (ticket.getStatus()) {
            case "Resolved":
                tvStatus.setTextColor(Color.parseColor("#22C55E"));
                tvStatus.setBackgroundResource(R.drawable.bg_green_badge);
                break;
            case "In Progress":
                tvStatus.setTextColor(Color.parseColor("#3B82F6"));
                tvStatus.setBackgroundResource(R.drawable.status_purple_bg);
                break;
            default:
                tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
                break;
        }

        // Find the button inside the view and set click listener
        View closeBtn = null;
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View v = group.getChildAt(i);
                if (v instanceof com.google.android.material.button.MaterialButton) {
                    closeBtn = v;
                    break;
                }
            }
        }
        if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchSupportUrls() {
        mDatabase.child("url").child("telegram").child("link").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String link = snapshot.getValue(String.class);
                    if (link != null && !link.isEmpty()) {
                        telegramSupportUrl = link;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mDatabase.child("url").child("email").child("address").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = snapshot.getValue(String.class);
                    if (email != null && !email.isEmpty()) {
                        supportEmail = email;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initFaqs() {
        mDatabase.child("faq").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFaqs.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FAQ faq = ds.getValue(FAQ.class);
                    if (faq != null) {
                        allFaqs.add(faq);
                    }
                }
                updateDisplayedFaqs(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        RecyclerView rvFaqs = findViewById(R.id.rv_faqs);
        if (rvFaqs != null) {
            faqAdapter = new FAQAdapter(displayedFaqs);
            rvFaqs.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            rvFaqs.setAdapter(faqAdapter);

            // Add standard list divider for uniform spacing
            androidx.recyclerview.widget.DividerItemDecoration divider = new androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL);
            android.graphics.drawable.Drawable dividerDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.faq_list_divider);
            if (dividerDrawable != null) divider.setDrawable(dividerDrawable);
            rvFaqs.addItemDecoration(divider);
        }

        TextView btnViewAll = findViewById(R.id.btn_view_all_faqs);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                if (displayedFaqs.size() < allFaqs.size()) {
                    updateDisplayedFaqs(true);
                    btnViewAll.setText("Show Less");
                } else {
                    updateDisplayedFaqs(false);
                    btnViewAll.setText("View All");
                }
            });
        }

        EditText etSearch = findViewById(R.id.et_search_faqs);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterFaqs(s.toString());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void updateDisplayedFaqs(boolean showAll) {
        displayedFaqs.clear();
        if (showAll) {
            displayedFaqs.addAll(allFaqs);
        } else {
            for (int i = 0; i < Math.min(3, allFaqs.size()); i++) {
                displayedFaqs.add(allFaqs.get(i));
            }
        }
        if (faqAdapter != null) faqAdapter.notifyDataSetChanged();
    }

    private void filterFaqs(String query) {
        displayedFaqs.clear();
        TextView tvNoFaqs = findViewById(R.id.tv_no_faqs);
        
        if (query.isEmpty()) {
            updateDisplayedFaqs(false);
            TextView btnViewAll = findViewById(R.id.btn_view_all_faqs);
            if (btnViewAll != null) btnViewAll.setText("View All");
            if (tvNoFaqs != null) tvNoFaqs.setVisibility(View.GONE);
        } else {
            for (FAQ faq : allFaqs) {
                if ((faq.getTitle() != null && faq.getTitle().toLowerCase().contains(query.toLowerCase())) || 
                    (faq.getDescription() != null && faq.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                    displayedFaqs.add(faq);
                }
            }
            TextView btnViewAll = findViewById(R.id.btn_view_all_faqs);
            if (btnViewAll != null) btnViewAll.setText("Show All");
            
            if (tvNoFaqs != null) {
                tvNoFaqs.setVisibility(displayedFaqs.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
        if (faqAdapter != null) faqAdapter.notifyDataSetChanged();
    }

    private void showCreateTicketDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_ticket, null);
        dialog.setContentView(view);

        EditText etTitle = view.findViewById(R.id.et_ticket_title);
        EditText etDesc = view.findViewById(R.id.et_ticket_desc);
        View btnSubmit = view.findViewById(R.id.btn_submit_ticket);

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getUid();
            if (userId == null) return;

            String ticketId = mDatabase.child("support_tickets").child(userId).push().getKey();
            if (ticketId == null) return;

            SupportTicket ticket = new SupportTicket(ticketId, title, desc, "Pending", System.currentTimeMillis());
            
            mDatabase.child("support_tickets").child(userId).child(ticketId).setValue(ticket)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ticket submitted successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void showSection(int sectionId, int navBtnId) {
        hideAllSections();
        View section = findViewById(sectionId);
        if (section != null) {
            section.setVisibility(View.VISIBLE);
        }
        updateDrawerSelection(navBtnId);
    }

    private void updateDrawerSelection(int selectedId) {
        int[] navIds = {R.id.dashboard_btn, R.id.deposit_btn, R.id.withdraw_btn, R.id.p2p_btn,
                        R.id.refer_btn, R.id.team_btn, R.id.plan_btn, R.id.support_btn};
        
        for (int id : navIds) {
            View btn = findViewById(id);
            if (btn instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout layout = (android.widget.LinearLayout) btn;
                ImageView icon = null;
                TextView text = null;
                
                // Find ImageView and TextView inside the layout
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    if (child instanceof ImageView) icon = (ImageView) child;
                    if (child instanceof TextView) text = (TextView) child;
                }
                
                if (icon != null && text != null) {
                    if (id == selectedId) {
                        layout.setBackgroundResource(R.drawable.menu_item_selected_bg);
                        icon.setColorFilter(ContextCompat.getColor(this, R.color.primary_purple));
                        text.setTextColor(ContextCompat.getColor(this, R.color.primary_purple));
                        text.setTypeface(null, android.graphics.Typeface.BOLD);
                    } else {
                        layout.setBackgroundResource(R.drawable.menu_item_bg);
                        icon.setColorFilter(Color.parseColor("#64748B"));
                        text.setTextColor(Color.parseColor("#64748B"));
                        text.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                }
            }
        }
    }

    private void hideAllSections() {
        int[] sections = {R.id.section_dashboard, R.id.section_deposits, R.id.section_withdraw, R.id.section_p2p,
                R.id.section_refer, R.id.section_team, R.id.section_plan, R.id.section_support};
        for (int id : sections) {
            View v = findViewById(id);
            if (v != null) v.setVisibility(View.GONE);
        }
    }

    // withdraw and deposit history control
    private void transaction_control(String userId){
        transactionHandler = new TransactionHandler(findViewById(android.R.id.content));
        transactionHandler.fetchTransactions(userId);
    }

    private void plan_control(String userId) {
        planHandler = new PlanHandler(this, findViewById(android.R.id.content));
        planHandler.init(userId);
    }

    private void team_control(String userId) {
        teamHandler = new TeamHandler(findViewById(android.R.id.content));
        teamHandler.fetchTeam(userId);
    }

    private void referral_control(String userId) {
        referralHandler = new ReferralHandler(findViewById(android.R.id.content));
        referralHandler.setupReferralSection(userId);
        
        withdrawalHandler = new WithdrawalHandler();
        withdrawalHandler.setupWithdrawListeners(findViewById(android.R.id.content), userId);
    }

   // Deposit Flow
    private void handle_deposit_address_qr(String address , String uid){
        new Thread(() -> {
            try {
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_without_bg);
                Bitmap qr = PremiumQRGenerator.generate(address, 1000, logo);
                
                runOnUiThread(() -> {
                    TextView wallet_address = findViewById(R.id.tv_my_address);
                    if (wallet_address != null) wallet_address.setText(address);
                    
                    ImageView imageView = findViewById(R.id.iv_qr_code);
                    if (imageView != null) imageView.setImageBitmap(qr);

                    View btnCopy = findViewById(R.id.btn_copy_address);
                    if (btnCopy != null) {
                        btnCopy.setOnClickListener(v -> {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Wallet Address", address);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(MainActivity.this, "Wallet address copied", Toast.LENGTH_SHORT).show();
                        });
                    }

                    depositHandler = new DepositHandler();
                    depositHandler.setupDepositListeners(findViewById(android.R.id.content), uid, address);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

}
