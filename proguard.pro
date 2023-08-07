# 导入相关库文件，丢失会导致混淆/加载失败
-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjars libs/forge-1.16.5-36.2.34_mapped_parchment_2022.03.06-1.16.5.jar
-libraryjars libs/gson-2.8.0.jar
-libraryjars libs/FirstPersonMod-2.0.1.jar

# 打印更加详细的信息，但是忽略依赖错误
-verbose
-ignorewarnings

# 不要缩减和优化，否则会导致游戏加载失败
-dontshrink
-dontoptimize

# 使用指定名称的混淆
-obfuscationdictionary dict.txt
-packageobfuscationdictionary dict.txt
-classobfuscationdictionary dict.txt
-repackageclasses com.elfmcys.yesstevemodel

# 保留唯一的主模组类和Mixin类
-keep class com.elfmcys.yesstevemodel.YesSteveModel
-keep class com.elfmcys.yesstevemodel.mixin.*
-keep class com.elfmcys.yesstevemodel.mixin.plugin.*

# 保留部分枚举类属性
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留所有用 @Keep 注解标记的方法或者变量
-keepclassmembers class * {
    @com.elfmcys.yesstevemodel.util.Keep <fields>;
    @com.elfmcys.yesstevemodel.util.Keep <methods>;
}

# 保留异常、内部类、注解、行数等信息
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,LineNumberTable,*Annotation*,Synthetic,EnclosingMethod,EventHandler,Override