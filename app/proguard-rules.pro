# SQLCipher
-keep class net.zetetic.** { *; }
-dontwarn net.zetetic.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.pdfwallet.data.db.**$$serializer { *; }
-keepclassmembers class com.pdfwallet.data.db.** {
    *** Companion;
}
-keepclasseswithmembers class com.pdfwallet.data.db.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# PDFBox
-keep class org.apache.pdfbox.** { *; }
-dontwarn org.apache.pdfbox.**

# Gemini AI SDK
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# Google API Client
-keep class com.google.api.** { *; }
-dontwarn com.google.api.**

# Firebase
-keep class com.google.firebase.** { *; }

# ZXing (Barcode)
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
