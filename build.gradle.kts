import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
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
        // TODO: https://github.com/Goooler/golang-gradle-plugin/pull/76
        externalNativeBuild.cmake.abiFilters += listOf("arm64-v8a", "x86_64")
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
      optIn.addAll("kotlin.uuid.ExperimentalUuidApi")
      freeCompilerArgs.addAll("-Xcontext-sensitive-resolution", "-Xexplicit-backing-fields")
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

  plugins.withId(rootProject.libs.plugins.kotlin.compose.get().pluginId) {
    extensions.configure<KotlinAndroidProjectExtension> {
      compilerOptions.optIn.addAll(
        "androidx.compose.foundation.ExperimentalFoundationApi",
        "androidx.compose.material3.ExperimentalMaterial3Api",
      )
    }
    extensions.configure<ComposeCompilerGradlePluginExtension> {
      stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability.conf"))
    }
    dependencies {
      "implementation"(platform(libs.androidx.compose.bom))
      "implementation"(libs.androidx.compose.ui)
      "implementation"(libs.androidx.compose.ui.tooling.preview)
      "implementation"(libs.androidx.compose.ui.util)
      "debugImplementation"(libs.androidx.compose.ui.tooling)
      "implementation"(libs.androidx.compose.animation)
      "implementation"(libs.androidx.compose.material3)
      "implementation"(libs.androidx.lifecycle.viewmodel.compose)
      "implementation"(libs.androidx.lifecycle.viewmodel.navigation3)
      "implementation"(libs.androidx.navigation3.runtime)
      "implementation"(libs.androidx.navigation3.ui)
    }
  }
}
