package com.ascend.invest.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ascend.invest.MainActivity;
import com.ascend.invest.handlers.ReferralManager;
import com.ascend.invest.handlers.SecurityManager;
import com.ascend.invest.databinding.ActivityAuthenticateBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AuthenticateActivity extends AppCompatActivity {
    private ActivityAuthenticateBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SharedPreferences sharedPrefs;
    private Vibrator vibrator;
    private Handler autoCheckHandler = new Handler(Looper.getMainLooper());
    private Runnable autoCheckRunnable;
    private boolean isCheckingVerification = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityAuthenticateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       // SecurityManager.validateAppIntegrity(this);
        getWindow().setStatusBarColor(android.graphics.Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage(); // Fixes locale issues for email templates
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPrefs = getSharedPreferences("saved_login", Activity.MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setupClickListeners();
        setupInputValidation();
        loadSavedLogin();
        applyEntranceAnimations();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            binding.tvVerificationMsg.setText("Verify your email " + user.getEmail() + " to continue.");
            switchPage(binding.verificationPage);
        }
    }
    private void setupClickListeners() {
        binding.btnGotoSignin.setOnClickListener(v -> switchPage(binding.signInPage));
        binding.btnGotoSignup.setOnClickListener(v -> switchPage(binding.Createpage));
        binding.btnGotoSignupLink.setOnClickListener(v -> switchPage(binding.Createpage));
        binding.btnGotoSigninLink.setOnClickListener(v -> switchPage(binding.signInPage));
        binding.btnBackToWelcomeFromSignin.setOnClickListener(v -> switchPage(binding.welcomePage));
        binding.btnBackToWelcomeFromSignup.setOnClickListener(v -> switchPage(binding.welcomePage));
        binding.textview12.setOnClickListener(v -> switchPage(binding.resentPage));
        binding.textview20.setOnClickListener(v -> switchPage(binding.signInPage));
        
        binding.button1.setOnClickListener(v -> handleSignIn());
        binding.button3.setOnClickListener(v -> handleSignUp());
        binding.button2.setOnClickListener(v -> handlePasswordReset());
        
        binding.btnCheckVerification.setOnClickListener(v -> checkVerificationStatus(true));
        binding.btnResendVerification.setOnClickListener(v -> resendVerification());
        binding.btnBackFromVerification.setOnClickListener(v -> {
            mAuth.signOut();
            stopVerificationAutoCheck();
            switchPage(binding.Createpage);
        });
    }

    private void handleSignUp() {
        String username = binding.edittext6.getText().toString().trim();
        String email = binding.edittext4.getText().toString().trim();
        String password = binding.edittext5.getText().toString().trim();
        String referral = binding.edittextReferral.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || password.length() < 6) {
            triggerError(binding.authCard);
            return;
        }
        if (!binding.checkbox2.isChecked()) {
            Toast.makeText(this, "Accept terms to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        setAuthLoading(true, binding.signupProgress, binding.button3);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Persist identity in Auth Profile immediately
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(pTask -> {
                                // Vault referral data securely and proceed
                                if (!TextUtils.isEmpty(referral)) {
                                    // Local cache for redundancy
                                    sharedPrefs.edit().putString("pending_referral_" + user.getUid(), referral).apply();
                                    mDatabase.child("unverified_referrals").child(user.getUid()).setValue(referral)
                                            .addOnCompleteListener(rTask -> sendVerificationAndTransition());
                                } else {
                                    sendVerificationAndTransition();
                                }
                            });
                        }
                    } else {
                        setAuthLoading(false, binding.signupProgress, binding.button3);
                        if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "This email is already registered. Please sign in.", Toast.LENGTH_LONG).show();
                            switchPage(binding.signInPage);
                        } else {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            triggerError(binding.authCard);
                        }
                    }
                });
    }

    private void handleSignIn() {
        String email = binding.loginUsernameEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            triggerError(binding.authCard);
            return;
        }

        setAuthLoading(true, binding.signinProgress, binding.button1);
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            if (binding.checkbox1.isChecked()) saveLoginInfo(email, password);
                            checkIfUserInDatabase(user.getUid());
                        } else {
                            // Redirect unverified users back to verification hub
                            setAuthLoading(false, binding.signinProgress, binding.button1);
                            sendVerificationAndTransition();
                        }
                    }
                } else {
                    setAuthLoading(false, binding.signinProgress, binding.button1);
                    Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                    triggerError(binding.authCard);
                }
            });
    }

    private void checkIfUserInDatabase(String uid) {
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    proceedToMain();
                } else {

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        String username = user.getDisplayName();
                        mDatabase.child("unverified_referrals").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snap) {
                                String dbReferral = snap.exists() ? snap.getValue(String.class) : "";
                                String finalReferral = !TextUtils.isEmpty(dbReferral) ? dbReferral : sharedPrefs.getString("pending_referral_" + uid, "");
                                saveUserToDatabase(uid, username, user.getEmail(), finalReferral);
                                // Clean up pending data
                                mDatabase.child("unverified_referrals").child(uid).removeValue();
                                sharedPrefs.edit().remove("pending_referral_" + uid).apply();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // On failure, try with local cache anyway to not block the user
                                String cachedReferral = sharedPrefs.getString("pending_referral_" + uid, "");
                                saveUserToDatabase(uid, username, user.getEmail(), cachedReferral);
                            }
                        });
                    } else {
                        mAuth.signOut();
                        switchPage(binding.signInPage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void sendVerificationAndTransition() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                setAuthLoading(false, binding.signupProgress, binding.button3);
                setAuthLoading(false, binding.signinProgress, binding.button1);

                binding.tvVerificationMsg.setText("A verification link was sent to " + user.getEmail());
                switchPage(binding.verificationPage);

                if (!task.isSuccessful()) {
                    String error = task.getException() != null ? task.getException().getMessage() : "Dispatch failed";
                    Log.e("AUTH", "Verification fail: " + error);
                    Toast.makeText(this, "Security Block: Too many attempts or invalid email.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void resendVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            binding.btnResendVerification.setEnabled(false);
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "New link dispatched!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed. Please wait a moment.", Toast.LENGTH_SHORT).show();
                }
                // Mandatory cooldown to prevent Firebase "Unusual Activity" blocks
                new Handler().postDelayed(() -> binding.btnResendVerification.setEnabled(true), 30000);
            });
        }
    }

    private void checkVerificationStatus(boolean showToast) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    stopVerificationAutoCheck();
                    checkIfUserInDatabase(user.getUid());
                } else if (showToast) {
                    Toast.makeText(this, "Not verified yet. Check your inbox.", Toast.LENGTH_SHORT).show();
                    triggerError(binding.btnCheckVerification);
                }
            });
        }
    }

    private void saveUserToDatabase(String uid, String username, String email, String referralCode) {
        String finalUsername = TextUtils.isEmpty(username) ? "User_" + uid.substring(0, 5) : username;
        String myReferralCode = finalUsername.toLowerCase().replaceAll("\\s+", "") + uid.substring(0, 4);
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", finalUsername);
        userMap.put("email", email);
        userMap.put("uid", uid);
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("myReferralCode", myReferralCode);
        userMap.put("wallet_balance", 0.0);
        userMap.put("total_deposit", 0.0);
        userMap.put("total_profit", 0.0);
        userMap.put("unlocked_balance", 0.0);
        
        mDatabase.child("users").child(uid).updateChildren(userMap)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mDatabase.child("referral_codes").child(myReferralCode).setValue(uid);
                    if (!TextUtils.isEmpty(referralCode)) {
                        new ReferralManager().setReferrer(uid, referralCode, new ReferralManager.ReferralSetCallback() {
                            @Override public void onSuccess() { proceedToMain(); }
                            @Override public void onFailure(String error) { proceedToMain(); }
                        });
                    } else {
                        proceedToMain();
                    }
                } else {
                    Toast.makeText(this, "Database write error.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.verificationPage.getVisibility() == View.VISIBLE) {
            startVerificationAutoCheck();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopVerificationAutoCheck();
    }

    private void startVerificationAutoCheck() {
        if (isCheckingVerification) return;
        isCheckingVerification = true;
        autoCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isCheckingVerification) return;
                checkVerificationStatus(false);
                autoCheckHandler.postDelayed(this, 10000); // 10s heartbeat
            }
        };
        autoCheckHandler.postDelayed(autoCheckRunnable, 5000);
    }

    private void stopVerificationAutoCheck() {
        isCheckingVerification = false;
        autoCheckHandler.removeCallbacks(autoCheckRunnable);
    }

    private void switchPage(View targetPage) {
        if (targetPage.getVisibility() == View.VISIBLE) return;
        
        if (targetPage == binding.verificationPage) startVerificationAutoCheck();
        else stopVerificationAutoCheck();

        TransitionManager.beginDelayedTransition(binding.pageContainer, new Fade().setDuration(300));
        View[] pages = {binding.welcomePage, binding.signInPage, binding.Createpage, binding.resentPage, binding.verificationPage};
        for (View page : pages) page.setVisibility(page == targetPage ? View.VISIBLE : View.GONE);
        
        binding.textinputlayout1.setError(null);
        binding.textinputlayout2.setError(null);
    }

    private void applyEntranceAnimations() {
        binding.logoHolder.setAlpha(0f);
        binding.logoHolder.setScaleX(0.2f);
        binding.logoHolder.setScaleY(0.2f);
        binding.logoHolder.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1200).setInterpolator(new OvershootInterpolator(1.4f)).start();

        binding.authTitle.setAlpha(0f);
        binding.authTitle.setTranslationY(40f);
        binding.authTitle.animate().alpha(1f).translationY(0f).setStartDelay(500).setDuration(800).setInterpolator(new DecelerateInterpolator()).start();

        binding.authSubtitle.setAlpha(0f);
        binding.authSubtitle.animate().alpha(1f).setStartDelay(800).setDuration(1000).start();

        binding.authCard.setAlpha(0f);
        binding.authCard.setTranslationY(300f);
        binding.authCard.animate().alpha(1f).translationY(0f).setStartDelay(700).setDuration(1200).setInterpolator(new DecelerateInterpolator(1.2f)).start();
    }

    private void setupInputValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateButtonStates(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.loginUsernameEmail.addTextChangedListener(watcher);
        binding.loginPassword.addTextChangedListener(watcher);
        binding.edittext6.addTextChangedListener(watcher);
        binding.edittext4.addTextChangedListener(watcher);
        binding.edittext5.addTextChangedListener(watcher);
    }

    private void updateButtonStates() {
        boolean loginValid = !TextUtils.isEmpty(binding.loginUsernameEmail.getText()) && !TextUtils.isEmpty(binding.loginPassword.getText());
        binding.button1.setAlpha(loginValid ? 1.0f : 0.6f);
        boolean registerValid = !TextUtils.isEmpty(binding.edittext6.getText()) && !TextUtils.isEmpty(binding.edittext4.getText()) && !TextUtils.isEmpty(binding.edittext5.getText());
        binding.button3.setAlpha(registerValid ? 1.0f : 0.6f);
    }

    private void setAuthLoading(boolean isLoading, View progress, View button) {
        progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        button.setEnabled(!isLoading);
        button.animate().scaleX(isLoading ? 0.92f : 1.0f).scaleY(isLoading ? 0.92f : 1.0f).setDuration(300).start();
    }

    private void triggerError(View view) {
        android.animation.ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(500).start();
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(50);
        }
    }

    private void loadSavedLogin() {
        if (sharedPrefs.contains("emailusername")) {
            binding.loginUsernameEmail.setText(sharedPrefs.getString("emailusername", ""));
            binding.loginPassword.setText(sharedPrefs.getString("password", ""));
            binding.checkbox1.setChecked(true);
            updateButtonStates();
        }
    }

    private void handlePasswordReset() {
        String email = binding.edittext3.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            triggerError(binding.textinputlayout3);
            return;
        }
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Recovery link dispatched", Toast.LENGTH_SHORT).show();
                    switchPage(binding.signInPage);
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Error";
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    triggerError(binding.authCard);
                }
            });
    }

    private void saveLoginInfo(String user, String pass) {
        sharedPrefs.edit().putString("emailusername", user).putString("password", pass).apply();
    }

    private void proceedToMain() {
        String uid = mAuth.getUid();
        if (uid != null) {
            String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            mDatabase.child("users").child(uid).child("currentDeviceId").setValue(deviceId);
        }

        // Start P2P Notification Service after successful login
        Intent serviceIntent = new Intent(this, com.ascend.invest.handlers.P2PNotificationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
