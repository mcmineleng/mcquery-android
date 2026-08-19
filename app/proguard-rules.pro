-keepattributes *Annotation*

-keep class com.google.gson.** { *; }
-keep class me.dilley.MineStat { *; }
-keep class org.xbill.DNS.** { *; }
-dontwarn com.sun.jna.**
-dontwarn javax.naming.**
-dontwarn lombok.**
-dontwarn org.slf4j.impl.**
-dontwarn sun.net.spi.nameservice.**
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
