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
-keep public enum com.kophe.le_sklad_zradn.screens.scanner.view.ScannerMode {
*;
}
-keep public enum com.kophe.le_sklad_zradn.screens.edititem.view.EditItemMode {
*;
}
-keep public enum com.kophe.le_sklad_zradn.screens.createissuance.view.IssuanceScreenMode {
*;
}
-keep class com.google.firestore.*
-keep class com.google.firebase.*
#-keep class com.kophe.le_sklad_zradn.data.datasource.firestore.FirestoreLocation

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
