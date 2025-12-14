# HealthPay Keyboard ProGuard Rules

# Keep app classes
-keep class tech.healthpay.keyboard.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Model classes
-keep class tech.healthpay.keyboard.model.** { *; }
-keep class tech.healthpay.keyboard.api.**$* { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# ZXing
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# Security
-keep class androidx.security.crypto.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.** { *; }
