-verbose
-allowaccessmodification
-repackageclasses

-keepattributes SourceFile,
                LineNumberTable

-renamesourcefileattribute SourceFile

-dontobfuscate

# Remove some Kotlin overhead
-processkotlinnullchecks remove


# Hardcode in core/src/main/cpp/main.c.JNI_OnLoad
-keep class kotlin.Unit
-keep interface kotlinx.coroutines.CompletableDeferred {
  boolean complete(java.lang.Object);
  boolean completeExceptionally(java.lang.Throwable);
}
