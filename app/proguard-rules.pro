# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in Android SDK tools.

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Lottie animations
-keep class com.airbnb.lottie.** { *; }

# Keep AdMob
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# Keep data classes for Room
-keep class com.ludoblitz.app.data.models.** { *; }
