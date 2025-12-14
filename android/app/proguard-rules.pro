# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

# ==================== General Rules ====================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== Kotlin ====================

-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ==================== AndroidX ====================

-keep class androidx.** { *; }
-dontwarn androidx.**

# ==================== Retrofit ====================

-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ==================== OkHttp ====================

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ==================== Gson ====================

-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep model classes for Gson serialization
-keep class tech.healthpay.keyboard.model.** { *; }
-keep class tech.healthpay.keyboard.api.** { *; }

# ==================== Hilt ====================

-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# ==================== HealthPay App Classes ====================

-keep class tech.healthpay.keyboard.** { *; }
-keep interface tech.healthpay.keyboard.** { *; }

# Keep all public classes and methods
-keepclasseswithmembernames class tech.healthpay.keyboard.** {
    public <methods>;
    public <fields>;
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==================== Security ====================

-keep class androidx.security.** { *; }
-keep class androidx.biometric.** { *; }

# ==================== Timber Logging ====================

-keep class com.jakewharton.timber.** { *; }

# ==================== Debugging ====================

# Uncomment for debugging
# -verbose
# -printmapping mapping.txt
# -printseeds seeds.txt
