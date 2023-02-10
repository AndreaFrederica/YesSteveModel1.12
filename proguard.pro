-dontwarn
-dontshrink
-dontoptimize
-repackageclasses com.elfmcys.yesstevemodel

-obfuscationdictionary dict.txt
-packageobfuscationdictionary dict.txt
-classobfuscationdictionary dict.txt

-keep class com.elfmcys.yesstevemodel.YesSteveModel
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
    @com.elfmcys.yesstevemodel.util.Keep <fields>;
    @com.elfmcys.yesstevemodel.util.Keep <methods>;
}
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,LineNumberTable,*Annotation*,Synthetic,EnclosingMethod,EventHandler,Override