# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保留Fragment相关类
-keep class androidx.fragment.app.Fragment { *; }
-keep class androidx.fragment.app.FragmentManager { *; }

# 优化导航控制器
-keep class androidx.navigation.** { *; }

# 优化视图绑定
-keep class * extends androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(android.view.View);
}

# 防止反射相关的类被优化掉
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# 优化对ViewPager2和RecyclerView的处理
-keep class androidx.viewpager2.** { *; }
-keep class androidx.recyclerview.widget.RecyclerView { *; }

# 避免优化掉底部导航栏
-keep class com.google.android.material.bottomnavigation.** { *; }

# 全局性能优化
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification