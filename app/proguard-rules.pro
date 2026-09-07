# Add project specific ProGuard rules here.
-keep class com.antidpi.mobile.core.NativeBridge { *; }
-keepclassmembers class com.antidpi.mobile.core.NativeBridge {
    native <methods>;
}
