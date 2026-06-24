-keep class app.skons.onsafe.widget.** { *; }
-keep class app.skons.onsafe.data.** { *; }
-keep class androidx.security.crypto.** { *; }

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
