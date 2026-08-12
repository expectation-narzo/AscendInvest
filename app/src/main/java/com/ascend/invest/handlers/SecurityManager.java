package com.ascend.invest.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.MessageDigest;

public class SecurityManager {
/*

    public static void validateAppIntegrity(Activity activity) {
        if (activity == null) return;

        // 1. Signature Verification from Database
        FirebaseDatabase.getInstance().getReference("key/appsign").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String expectedHash = snapshot.getValue(String.class);
                    if (expectedHash != null && !expectedHash.isEmpty() && !"YOUR_RELEASE_SIG_HASH_HERE".equals(expectedHash)) {
                        String currentHash = getCurrentSignatureHash(activity);
                        if (!currentHash.equals(expectedHash)) {
                            killApp(activity, "Integrity Failure - Re-install from official source");
                        }
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Root Check
        if (isDeviceRooted()) {
            killApp(activity, "Security Violation: Root detected");
            return;
        }

        // 3. Emulator Check (Prevents bot farming)
        if (isEmulator()) {
            killApp(activity, "Unauthorized Environment: Emulators not allowed");
            return;
        }

        // 4. Modding Tools Check
        if (hasModdingApps(activity)) {
            killApp(activity, "Unauthorized tools detected");
            return;
        }

        // 5. Debugger Check
        if (isDebuggerAttached() && !com.ascend.invest.BuildConfig.DEBUG) {
            killApp(activity, "Debugging detected");
            return;
        }
    }

    public static boolean isEmulator() {
        return (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.PRODUCT.contains("sdk_google")
                || android.os.Build.PRODUCT.contains("google_sdk")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("sdk_x86")
                || android.os.Build.PRODUCT.contains("vbox86p")
                || android.os.Build.PRODUCT.contains("emulator")
                || android.os.Build.PRODUCT.contains("simulator");
    }

    private static void killApp(Activity activity, String reason) {
        android.widget.Toast.makeText(activity, "Security Violation: " + reason, android.widget.Toast.LENGTH_LONG).show();
        activity.finishAffinity();

        // Force kill process to prevent any bypass
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }, 1500);
    }

    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = {
                "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
                "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            boolean found = in.readLine() != null;
            process.destroy();
            return found;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isSignatureValid(Context context, String expectedHash) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String currentHash = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
                return currentHash.equals(expectedHash);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static String getCurrentSignatureHash(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
            }
        } catch (Exception ignored) {}
        return "";
    }

    public static boolean hasModdingApps(Context context) {
        String[] moddingApps = {
                "com.chelpus.lackypatch", "com.dimonvideo.luckypatcher",
                "com.android.vending.billing.InAppBillingService.LUCK",
                "com.blackmart.market", "com.allinone.free", "com.repodroid.app"
        };
        PackageManager pm = context.getPackageManager();
        for (String pkg : moddingApps) {
            try {
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    public static boolean isDebuggerAttached() {
        return android.os.Debug.isDebuggerConnected();
    }

 */
}
