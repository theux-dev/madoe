# Commons Compress referencia compressores opcionais que não usamos.
-dontwarn org.apache.commons.compress.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.brotli.**
-dontwarn org.tukaani.xz.**
-dontwarn org.objectweb.asm.**
-dontwarn org.slf4j.**
-keep class com.github.junrar.** { *; }

# Mantém arquivo e linha nos relatórios de erro do build de release (o mapping.txt desfaz o ofuscamento).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
