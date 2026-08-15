package com.ascend.invest.handlers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.ascend.invest.databinding.ItemLevelPercentageBinding;
import com.ascend.invest.databinding.SectionReferBinding;
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

    private final SectionReferBinding binding;
    private final DatabaseReference mDatabase;
    private final Context context;

    public ReferralHandler(SectionReferBinding binding) {
        this.binding = binding;
        this.context = binding.getRoot().getContext();
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void setupReferralSection(String userId) {
        fetchCommissionLevels();

        UserHandler.getInstance().listenToUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String code = snapshot.child("myReferralCode").getValue(String.class);
                    if (code != null) {
                        if (binding.tvReferralCode != null) binding.tvReferralCode.setText(code);
                        
                        String fullInviteText = "🚀 *Join Ascend Invest and start earning!*\n\n" +
                                "I'm inviting you to join the elite investment community. Use my Referral Code to get exclusive benefits:\n\n" +
                                "👉 *" + code + "*\n\n" +
                                "1. Install the attached APK file.\n" +
                                "2. Register your account.\n" +
                                "3. Use my code during sign-up!\n\n" +
                                "Start your journey today! 💸";

                        if (binding.btnCopyReferral != null) {
                            binding.btnCopyReferral.setOnClickListener(v -> {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Invitation", fullInviteText);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Invitation copied to clipboard", Toast.LENGTH_SHORT).show();
                            });
                        }

                        if (binding.btnShareReferral != null) {
                            binding.btnShareReferral.setOnClickListener(v -> shareApkWithReferral(code));
                        }
                    }

                    Object balanceObj = snapshot.child("referral_balance").getValue();
                    if (binding.tvReferralBalance != null) {
                        if (balanceObj != null) {
                            try {
                                binding.tvReferralBalance.setText("$" + String.format(Locale.getDefault(), "%.2f", Double.parseDouble(balanceObj.toString())));
                            } catch (NumberFormatException e) {
                                binding.tvReferralBalance.setText("$0.00");
                            }
                        } else {
                            binding.tvReferralBalance.setText("$0.00");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        UserHandler.getInstance().getUsersByReferrer(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding.tvTotalRefers != null) {
                    long count = snapshot.getChildrenCount();
                    binding.tvTotalRefers.setText(count + (count == 1 ? " Member" : " Members"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCommissionLevels() {
        if (binding.layoutCommissions.llCommissionLevelsContainer == null) return;

        mDatabase.child("level/refer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.layoutCommissions.llCommissionLevelsContainer.removeAllViews();
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
                        ItemLevelPercentageBinding itemBinding = ItemLevelPercentageBinding.inflate(LayoutInflater.from(context), binding.layoutCommissions.llCommissionLevelsContainer, false);
                        itemBinding.tvLevelLabel.setText("Level " + i);
                        itemBinding.tvLevelPercentage.setText(String.format(Locale.US, "%.1f%%", percentage));
                        binding.layoutCommissions.llCommissionLevelsContainer.addView(itemBinding.getRoot());
                    }
                }

                if (binding.tvReferralPromoDesc != null) {
                    binding.tvReferralPromoDesc.setText(String.format(Locale.US, "Earn up to %.0f%% commission across 20 levels. The larger your network, the higher your passive income.", level1Percentage));
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

            if (!destFile.exists() || destFile.length() != sourceFile.length()) {
                copyFile(sourceFile, destFile);
            }

            Uri apkUri = FileProvider.getUriForFile(context, "com.ascend.invest.fileprovider", destFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
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
            
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newRawUri("", apkUri));

            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invitation", shareBody);
            clipboard.setPrimaryClip(clip);

            context.startActivity(Intent.createChooser(intent, "Share App & Referral"));

        } catch (Exception e) {
            Toast.makeText(context, "Error sharing app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
