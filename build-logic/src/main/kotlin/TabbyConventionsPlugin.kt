import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.AndroidBasePlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class TabbyConventionsPlugin : Plugin<Project> {
  private lateinit var libs: LibrariesForLibs

  override fun apply(target: Project) =
    with(target) {
      libs = extensions.getByType(LibrariesForLibs::class.java)
      configureAndroid()
      configureKotlin()
      configureSpotless()
      configureMultiplatformAndroid()
    }

  private fun Project.configureAndroid() {
    plugins.withType(AndroidBasePlugin::class.java).configureEach {
      extensions.configure(CommonExtension::class.java) { android ->
        android.namespace = "com.github.kr328.clash.${project.name}"
        android.compileSdk = 37
        android.defaultConfig.apply {
          minSdk = 28
          // TODO: https://github.com/Goooler/golang-gradle-plugin/pull/76
          externalNativeBuild.cmake.abiFilters += listOf("arm64-v8a", "x86_64")
        }
        android.ndkVersion = "29.0.14206865"
        android.compileOptions.apply {
          sourceCompatibility(libs.versions.jvmTarget.get())
          targetCompatibility(libs.versions.jvmTarget.get())
        }
      }
    }
  }

  private fun Project.configureKotlin() {
    tasks.withType(KotlinJvmCompile::class.java).configureEach { task ->
      (task as KotlinCompilationTask<KotlinJvmCompilerOptions>).compilerOptions.apply {
        allWarningsAsErrors.set(true)
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        freeCompilerArgs.addAll("-Xcontext-sensitive-resolution")
      }
    }
  }

  private fun Project.configureSpotless() {
    pluginManager.apply(libs.plugins.spotless.get().pluginId)
    extensions.configure(SpotlessExtension::class.java) { spotless ->
      spotless.kotlin { format ->
        format.target("src/**/*.kt")
        format.ktfmt(libs.ktfmt.get().version).googleStyle()
      }
      spotless.kotlinGradle { format ->
        format.ktfmt(libs.ktfmt.get().version).googleStyle()
      }
    }
  }

  private fun Project.configureMultiplatformAndroid() {
    pluginManager.withPlugin(libs.plugins.android.multiplatform.get().pluginId) {
      pluginManager.apply(libs.plugins.kotlin.multiplatform.get().pluginId)
      extensions.configure(KotlinMultiplatformExtension::class.java) { kotlin ->
        kotlin.extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class.java) { android ->
          android.namespace = "com.github.kr328.clash.${project.name}"
          android.compileSdk = 37
          android.minSdk = 28
          android.compilerOptions.jvmTarget.set(
            JvmTarget.fromTarget(libs.versions.jvmTarget.get()),
          )
          android.androidResources.enable = true
        }

        kotlin.sourceSets.getByName("commonMain").dependencies { dependencies ->
          with(dependencies) {
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.uiTooling)
            implementation(libs.jetbrains.compose.uiToolingPreview)
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.components.resources)
            implementation(libs.jetbrains.androidx.lifecycle.viewmodelCompose)
            implementation(libs.jetbrains.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.jetbrains.androidx.navigation3.ui)
            implementation(libs.koin.viewModel)
          }
        }

        kotlin.compilerOptions.optIn.addAll(
          "androidx.compose.foundation.ExperimentalFoundationApi",
          "androidx.compose.material3.ExperimentalMaterial3Api",
        )
      }

      pluginManager.apply(libs.plugins.kotlin.compose.get().pluginId)
      extensions.configure(ComposeCompilerGradlePluginExtension::class.java) { composeCompiler ->
        composeCompiler.stabilityConfigurationFiles.add(
          rootProject.layout.projectDirectory.file("stability.conf"),
        )
      }

      pluginManager.apply(libs.plugins.jb.compose.get().pluginId)
      extensions.configure(ComposeExtension::class.java) { compose ->
        compose.extensions.configure(ResourcesExtension::class.java) { resources ->
          resources.packageOfResClass = "com.github.kr328.clash.${project.name}"
          resources.generateResClass = resources.always
        }
      }
    }
  }
}
