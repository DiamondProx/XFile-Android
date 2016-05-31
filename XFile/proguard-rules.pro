# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Inst\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembers class * {   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}

# greendao
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

# eventbus
-dontwarn org.greenrobot.eventbus.**
-keep class org.greenrobot.eventbus.**{*;}
-keepclassmembers class ** {
    public void onEvent*(**);
}
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#slidingmenu
-dontwarn com.jeremyfeinstein.slidingmenu.lib.**
-keep class com.jeremyfeinstein.slidingmenu.lib.**{*;}
# netty
-dontwarn sun.**
-dontwarn io.netty.**
-keep class io.netty.** { *; }
-keepattributes Signature,InnerClasses
-keepclasseswithmembers class io.netty.** {
    *;
}
# protobuf
-dontwarn com.google.protobuf.**
-keep class com.google.protobuf.** { *; }
# 友盟-分析
-dontwarn com.umeng.analytics.**
-dontwarn u.aly.**
-keep class com.umeng.analytics.** { *; }
-keep class u.aly.** { *; }
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
# 友盟-推送
-dontwarn com.ta.utdid2.**
-dontwarn com.umeng.**
-dontwarn com.ut.device.**
-dontwarn org.android.agoo.**
-dontwarn org.android.spdy.**
-keep class com.ta.utdid2.** { *; }
-keep class com.umeng.** { *; }
-keep class com.ut.device.** { *; }
-keep class org.android.agoo.** { *; }
-keep class org.android.spdy.** { *; }
-keep public class org.android.Config
-keep,allowshrinking class org.android.agoo.service.* {
    public <fields>;
    public <methods>;
}
-keep,allowshrinking class com.umeng.message.* {
    public <fields>;
    public <methods>;
}
-keep public class com.huangjiang.xfile.R$*{
   public static final int *;
}
# image-loader
-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.** { *; }
# okio
-dontwarn okio.**
-keep class okio.**



