# Add project specific ProGuard rules here.
# By default, the most common rules are included in an automatic way from Android Gradle Plugin.
# You can add more rules here, or at the file listed in the build type(s) section.

# If your project uses Room, uncomment the following rule.
# See https://developer.android.com/training/data-storage/room/additional-config
-dontwarn androidx.room.common.**
-dontwarn androidx.room.runtime.**
-keepnames class androidx.room.common.**
-keepnames class androidx.room.runtime.**

# If your project uses Biometric, uncomment the following rule.
# See https://developer.android.com/training/sign-in/biometric-auth#proguard
-keep class androidx.biometric.** { *; }

# If your project uses Security-Crypto, uncomment the following rule.
# See https://developer.android.com/topic/security/cryptography/android-crypto
-keep class androidx.security.crypto.** { *; }

# If your project uses Lifecycle, uncomment the following rule.
# See https://developer.android.com/topic/libraries/architecture/lifecycle
-keep class androidx.lifecycle.** { *; }

# If your project uses WorkManager, uncomment the following rule.
# See https://developer.android.com/topic/libraries/architecture/workmanager/advanced/setup#proguard
-keep class androidx.work.** { *; }
-keep class * implements androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.RxWorker { *; }
