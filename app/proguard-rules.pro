# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.profesorfalken.jiittimer.**$$serializer { *; }
-keepclassmembers class com.profesorfalken.jiittimer.** {
    *** Companion;
}
-keepclasseswithmembers class com.profesorfalken.jiittimer.** {
    kotlinx.serialization.KSerializer serializer(...);
}
