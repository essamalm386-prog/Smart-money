# kotlinx.serialization — conserver les sérialiseurs générés
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class cm.oyenga.app.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class cm.oyenga.app.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class cm.oyenga.app.data.model.**$$serializer { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
