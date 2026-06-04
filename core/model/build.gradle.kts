plugins {
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android {
    namespace = "com.github.kr328.clash.model"
    compileSdk = 37
    minSdk = 28
    withHostTest {}
    compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21 }
  }

  jvm { compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21 } }

  sourceSets {
    commonMain.dependencies { implementation(libs.kotlin.serialization.json) }

    commonTest.dependencies { implementation(kotlin("test")) }
  }
}
