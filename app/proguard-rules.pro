# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Firebase Rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# MPAndroidChart Rules
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Data Models (Very Important: Keep names for Firebase parsing)
-keep class com.ascend.invest.handlers.Plan { *; }
-keep class com.ascend.invest.handlers.FAQ { *; }
-keep class com.ascend.invest.handlers.SupportTicket { *; }
-keep class com.ascend.invest.handlers.Transaction { *; }
-keep class com.ascend.invest.handlers.TeamMember { *; }
-keep class com.ascend.invest.handlers.P2POrder { *; }
-keep class com.ascend.invest.handlers.P2PListing { *; }
-keep class com.ascend.invest.handlers.ChatMessage { *; }

# WorkManager Workers (Must keep names for system execution)
-keep class com.ascend.invest.handlers.BrandingWorker { *; }
-keep class com.ascend.invest.handlers.ProfitCheckWorker { *; }
-keep class com.ascend.invest.handlers.P2POrderWorker { *; }

# Prevent de-obfuscation of key handlers
-keep class com.ascend.invest.handlers.SecurityManager { *; }
-keepclassmembers class com.ascend.invest.handlers.SecurityManager {
    public static void validateAppIntegrity(android.app.Activity);
}

# Zxing Rules
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }