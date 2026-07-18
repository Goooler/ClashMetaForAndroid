import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.AndroidBasePlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class TabbyConventionsPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

      configureAndroid(libs)
      configureKotlin(libs)
      configureSpotless(libs)
      configureMultiplatformAndroid(libs)
    }

  private fun Project.configureAndroid(libs: VersionCatalog) {
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
          sourceCompatibility(libs.requiredVersion("jvmTarget"))
          targetCompatibility(libs.requiredVersion("jvmTarget"))
        }
      }
    }
  }

  private fun Project.configureKotlin(libs: VersionCatalog) {
    tasks.withType(KotlinJvmCompile::class.java).configureEach { task ->
      (task as KotlinCompilationTask<KotlinJvmCompilerOptions>).compilerOptions.apply {
        allWarningsAsErrors.set(true)
        jvmTarget.set(JvmTarget.fromTarget(libs.requiredVersion("jvmTarget")))
        freeCompilerArgs.addAll("-Xcontext-sensitive-resolution")
      }
    }
  }

  private fun Project.configureSpotless(libs: VersionCatalog) {
    pluginManager.apply(libs.findPlugin("spotless").get().get().pluginId)
    extensions.configure(SpotlessExtension::class.java) { spotless ->
      spotless.kotlin { format ->
        format.target("src/**/*.kt")
        format.ktfmt(libs.findLibrary("ktfmt").get().get().version).googleStyle()
      }
      spotless.kotlinGradle { format ->
        format.ktfmt(libs.findLibrary("ktfmt").get().get().version).googleStyle()
      }
    }
  }

  private fun Project.configureMultiplatformAndroid(libs: VersionCatalog) {
    pluginManager.withPlugin(libs.findPlugin("android-multiplatform").get().get().pluginId) {
      pluginManager.apply(libs.findPlugin("kotlin-multiplatform").get().get().pluginId)
      extensions.configure(KotlinMultiplatformExtension::class.java) { kotlin ->
        kotlin.extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class.java) { android ->
          android.namespace = "com.github.kr328.clash.${project.name}"
          android.compileSdk = 37
          android.minSdk = 28
          android.compilerOptions.jvmTarget.set(
            JvmTarget.fromTarget(libs.requiredVersion("jvmTarget"))
          )
          android.androidResources.enable = true
        }

        kotlin.sourceSets.getByName("commonMain").dependencies { dependencies ->
          listOf(
              "jetbrains-compose-ui",
              "jetbrains-compose-uiTooling",
              "jetbrains-compose-uiToolingPreview",
              "jetbrains-compose-runtime",
              "jetbrains-compose-foundation",
              "jetbrains-compose-material3",
              "jetbrains-compose-components-resources",
              "jetbrains-androidx-lifecycle-viewmodelCompose",
              "jetbrains-androidx-lifecycle-runtimeCompose",
              "jetbrains-androidx-lifecycle-viewmodelNavigation3",
              "jetbrains-androidx-navigation3-ui",
              "koin-viewModel",
          )
            .forEach { dependencies.implementation(libs.findLibrary(it).get()) }
        }

        kotlin.compilerOptions.optIn.addAll(
          "androidx.compose.foundation.ExperimentalFoundationApi",
          "androidx.compose.material3.ExperimentalMaterial3Api",
        )
      }

      pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)
      extensions.configure(ComposeCompilerGradlePluginExtension::class.java) { composeCompiler ->
        composeCompiler.stabilityConfigurationFiles.add(
          rootProject.layout.projectDirectory.file("stability.conf")
        )
      }

      pluginManager.apply(libs.findPlugin("jb-compose").get().get().pluginId)
      extensions.configure(ComposeExtension::class.java) { compose ->
        compose.extensions.configure(ResourcesExtension::class.java) { resources ->
          resources.packageOfResClass = "com.github.kr328.clash.${project.name}"
          resources.generateResClass = resources.always
        }
      }
    }
  }

  private fun VersionCatalog.requiredVersion(alias: String) =
    findVersion(alias).get().requiredVersion
}
