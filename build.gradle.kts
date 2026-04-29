import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.golang) apply false
  alias(libs.plugins.spotless) apply false
}

allprojects {
  plugins.withType<AndroidBasePlugin>().configureEach {
    extensions.configure<CommonExtension> {
      namespace = "com.github.kr328.clash.${project.name}"
      compileSdk = 37
      defaultConfig.apply {
        minSdk = 28
        ndk.abiFilters += listOf("arm64-v8a", "x86_64")
      }
      ndkVersion = "29.0.14206865"
      compileOptions.apply {
        sourceCompatibility(libs.versions.jvmTarget.get())
        targetCompatibility(libs.versions.jvmTarget.get())
      }
    }
  }

  plugins.withType<JavaBasePlugin>().configureEach {
    extensions.configure<JavaPluginExtension> {
      setSourceCompatibility(libs.versions.jvmTarget.get())
      setTargetCompatibility(libs.versions.jvmTarget.get())
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      allWarningsAsErrors = true
      jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
    }
  }

  plugins.apply(rootProject.libs.plugins.spotless.get().pluginId)
  extensions.configure<SpotlessExtension> {
    kotlin {
      target("src/**/*.kt")
      ktfmt(libs.ktfmt.get().version).googleStyle()
    }
    kotlinGradle { ktfmt(libs.ktfmt.get().version).googleStyle() }
  }
}
