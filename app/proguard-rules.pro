-verbose
-allowaccessmodification

-keepattributes SourceFile,
                LineNumberTable

-renamesourcefileattribute SourceFile

-dontobfuscate

# Remove some Kotlin overhead
-processkotlinnullchecks remove


# Hardcode in core/src/main/cpp/main.c.JNI_OnLoad
-keep class kotlin.Unit
-keep interface kotlinx.coroutines.CompletableDeferred

# https://developer.android.com/reference/kotlin/androidx/compose/runtime/ComposeRuntimeFlags#isLinkBufferComposerEnabled()
-assumevalues public class androidx.compose.runtime.ComposeRuntimeFlags {
    static boolean isLinkBufferComposerEnabled return true;
}
