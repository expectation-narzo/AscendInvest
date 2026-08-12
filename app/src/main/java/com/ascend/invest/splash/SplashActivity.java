package com.ascend.invest.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.ascend.invest.MainActivity;
import com.ascend.invest.R;
import com.ascend.invest.auth.AuthenticateActivity;
import com.ascend.invest.databinding.ActivitySplashBinding;
import com.ascend.invest.handlers.SecurityManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Perform Comprehensive Integrity Shield Scan
      //  SecurityManager.validateAppIntegrity(this);

        // LOG SIGNATURE HASH (Only needed for first-time setup to find your hash)
//        android.util.Log.d("APP_SIG", "Your Signature Hash: " + SecurityManager.getCurrentSignatureHash(this));

        // Initial setup for status bar
        getWindow().setStatusBarColor(android.graphics.Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        startEliteAnimations();
        navigateToNext();
    }

    private void startEliteAnimations() {
        // 1. Background Circle Reveal
        binding.logoBgCircle.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();

        // 2. Logo Icon Reveal (slightly staggered)
        binding.ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(200)
                .setDuration(900)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        // 3. Title Reveal (Slide up + Fade)
        binding.tvSplashTitle.setTranslationY(30f);
        binding.tvSplashTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(600)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 4. Subtitle Reveal (Fade)
        binding.tvSplashSubtitle.animate()
                .alpha(1f)
                .setStartDelay(1000)
                .setDuration(1000)
                .start();

        // 5. Progress Indicator Reveal
        binding.splashProgress.animate()
                .alpha(1f)
                .setStartDelay(1400)
                .setDuration(600)
                .start();
                
        // 6. Subtle continuous pulse for the center hub
        binding.centerHub.postDelayed(() -> {
            android.animation.ObjectAnimator pulseX = android.animation.ObjectAnimator.ofFloat(binding.centerHub, "scaleX", 1f, 1.03f);
            android.animation.ObjectAnimator pulseY = android.animation.ObjectAnimator.ofFloat(binding.centerHub, "scaleY", 1f, 1.03f);
            
            pulseX.setDuration(2000);
            pulseY.setDuration(2000);
            pulseX.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            pulseY.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            pulseX.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            pulseY.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            pulseX.setInterpolator(new AccelerateDecelerateInterpolator());
            pulseY.setInterpolator(new AccelerateDecelerateInterpolator());
            
            pulseX.start();
            pulseY.start();
        }, 2200);
    }

    private void navigateToNext() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.isEmailVerified()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, AuthenticateActivity.class);
                if (user != null) FirebaseAuth.getInstance().signOut(); // Force signout if not verified
            }
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 4500); // Extended time to appreciate the high-end animation
    }
}
