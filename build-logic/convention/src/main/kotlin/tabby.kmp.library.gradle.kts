import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
  applyDefaultHierarchyTemplate()

  android {
    namespace = "com.github.kr328.clash.${project.path.removePrefix(":").replace(':', '.')}"
    compileSdk = 37
    minSdk = 28
    withHostTest {}
    compilerOptions { jvmTarget = JvmTarget.JVM_21 }
  }

  jvm { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

  js {
    browser()
    nodejs()
  }

  iosX64()
  iosArm64()
  iosSimulatorArm64()
  macosArm64()
  linuxX64()
  mingwX64()

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs { browser() }

  sourceSets { commonTest.dependencies { implementation(kotlin("test")) } }
}

tasks.register("testClasses") { dependsOn("allTests") }
