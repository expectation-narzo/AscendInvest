package com.ascend.invest.handlers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.ascend.invest.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class ReferralHandler {

    private final View root;
    private final DatabaseReference mDatabase;
    private final Context context;

    public ReferralHandler(View root) {
        this.root = root;
        this.context = root.getContext();
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void setupReferralSection(String userId) {
        TextView tvCode = root.findViewById(R.id.tv_referral_code);
        TextView tvBalance = root.findViewById(R.id.tv_referral_balance);
        TextView tvTotalRefers = root.findViewById(R.id.tv_total_refers);
        View btnCopy = root.findViewById(R.id.btn_copy_referral);
        View btnShare = root.findViewById(R.id.btn_share_referral);
        LinearLayout llLevelsContainer = root.findViewById(R.id.ll_commission_levels_container);

        fetchCommissionLevels(llLevelsContainer);

        UserHandler.getInstance().listenToUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String code = snapshot.child("myReferralCode").getValue(String.class);
                    if (code != null) {
                        if (tvCode != null) tvCode.setText(code);
                        
                        String fullInviteText = "🚀 *Join Ascend Invest and start earning!*\n\n" +
                                "I'm inviting you to join the elite investment community. Use my Referral Code to get exclusive benefits:\n\n" +
                                "👉 *" + code + "*\n\n" +
                                "1. Install the attached APK file.\n" +
                                "2. Register your account.\n" +
                                "3. Use my code during sign-up!\n\n" +
                                "Start your journey today! 💸";

                        if (btnCopy != null) {
                            btnCopy.setOnClickListener(v -> {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Invitation", fullInviteText);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Invitation copied to clipboard", Toast.LENGTH_SHORT).show();
                            });
                        }

                        if (btnShare != null) {
                            btnShare.setOnClickListener(v -> {
                                shareApkWithReferral(code);
                            });
                        }
                    }

                    Object balanceObj = snapshot.child("referral_balance").getValue();
                    if (tvBalance != null) {
                        if (balanceObj != null) {
                            try {
                                tvBalance.setText("$" + String.format("%.2f", Double.parseDouble(balanceObj.toString())));
                            } catch (NumberFormatException e) {
                                tvBalance.setText("$0.00");
                            }
                        } else {
                            tvBalance.setText("$0.00");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Fetch total refers count (Network Size) using UserHandler query
        UserHandler.getInstance().getUsersByReferrer(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (tvTotalRefers != null) {
                    long count = snapshot.getChildrenCount();
                    tvTotalRefers.setText(count + (count == 1 ? " Member" : " Members"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCommissionLevels(LinearLayout container) {
        if (container == null) return;
        TextView tvPromo = root.findViewById(R.id.tv_referral_promo_desc);

        mDatabase.child("level/refer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                double level1Percentage = 10.0;
                
                for (int i = 1; i <= 20; i++) {
                    String key = "level" + i;
                    double percentage = 0.0;
                    if (snapshot.hasChild(key)) {
                        Object val = snapshot.child(key).getValue();
                        if (val instanceof Number) percentage = ((Number) val).doubleValue();
                    } else {
                        percentage = getDefaultPercentage(i);
                    }

                    if (i == 1) level1Percentage = percentage;

                    if (percentage > 0) {
                        View itemView = LayoutInflater.from(context).inflate(R.layout.item_level_percentage, container, false);
                        TextView tvLabel = itemView.findViewById(R.id.tv_level_label);
                        TextView tvValue = itemView.findViewById(R.id.tv_level_percentage);

                        tvLabel.setText("Level " + i);
                        tvValue.setText(String.format(Locale.US, "%.1f%%", percentage));
                        container.addView(itemView);
                    }
                }

                if (tvPromo != null) {
                    tvPromo.setText(String.format(Locale.US, "Earn up to %.0f%% commission across 20 levels. The larger your network, the higher your passive income.", level1Percentage));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private double getDefaultPercentage(int level) {
        if (level == 1) return 10.0;
        if (level == 2) return 7.0;
        if (level == 3) return 5.0;
        if (level == 4) return 3.0;
        if (level >= 5 && level <= 10) return 2.0;
        if (level >= 11 && level <= 20) return 1.0;
        return 0.0;
    }

    private void shareApkWithReferral(String code) {
        try {
            File sourceFile = new File(context.getApplicationInfo().publicSourceDir);
            File destFile = new File(context.getCacheDir(), "AscendInvest.apk");

            // Only copy if file doesn't exist or size is different (basic check)
            if (!destFile.exists() || destFile.length() != sourceFile.length()) {
                copyFile(sourceFile, destFile);
            }

            Uri apkUri = FileProvider.getUriForFile(context, "com.ascend.invest.fileprovider", destFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            // Set type to APK specifically
            intent.setType("application/vnd.android.package-archive");
            intent.putExtra(Intent.EXTRA_STREAM, apkUri);
            
            String shareBody = "🚀 *Join Ascend Invest and start earning!*\n\n" +
                    "I'm inviting you to join the elite investment community. Use my Referral Code to get exclusive benefits:\n\n" +
                    "👉 *" + code + "*\n\n" +
                    "1. Install the attached APK file.\n" +
                    "2. Register your account.\n" +
                    "3. Use my code during sign-up!\n\n" +
                    "Start your journey today! 💸";
            
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Invitation to Ascend Invest");
            
            // Critical for Android 10+ to ensure the receiving app has permission
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newRawUri("", apkUri));

            // Also copy the full invitation text to clipboard as a backup for the sender
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invitation", shareBody);
            clipboard.setPrimaryClip(clip);

            context.startActivity(Intent.createChooser(intent, "Share App & Referral"));

        } catch (Exception e) {
            Toast.makeText(context, "Error sharing app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Fallback to text only if APK sharing fails
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = "🚀 *Join Ascend Invest and start earning!*\n\n" +
                    "Use my Referral Code to get exclusive benefits:\n\n" +
                    "👉 *" + code + "*\n\n" +
                    "Download the app and start earning! 💸";
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            context.startActivity(Intent.createChooser(intent, "Share via"));
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}
