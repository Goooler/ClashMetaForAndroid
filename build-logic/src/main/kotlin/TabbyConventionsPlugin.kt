import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.AndroidBasePlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class TabbyConventionsPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      configureAndroid(libs)
      configureKotlin(libs)
      configureSpotless(libs)
      configureMultiplatformAndroid(libs)
    }

  private fun Project.configureAndroid(libs: VersionCatalog) {
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
          sourceCompatibility(libs.requiredVersion("jvmTarget"))
          targetCompatibility(libs.requiredVersion("jvmTarget"))
        }
      }
    }
  }

  private fun Project.configureKotlin(libs: VersionCatalog) {
    tasks.withType<KotlinJvmCompile>().configureEach { task ->
      (task as KotlinCompilationTask<KotlinJvmCompilerOptions>).compilerOptions.apply {
        allWarningsAsErrors.set(true)
        jvmTarget.set(JvmTarget.fromTarget(libs.requiredVersion("jvmTarget")))
        freeCompilerArgs.addAll("-Xcontext-sensitive-resolution")
      }
    }
  }

  private fun Project.configureSpotless(libs: VersionCatalog) {
    pluginManager.apply(libs.findPlugin("spotless").get().get().pluginId)
    extensions.configure<SpotlessExtension> {
      kotlin { format ->
        format.target("src/**/*.kt")
        format.ktfmt(libs.findLibrary("ktfmt").get().get().version).googleStyle()
      }
      kotlinGradle { format ->
        format.ktfmt(libs.findLibrary("ktfmt").get().get().version).googleStyle()
      }
    }
  }

  private fun Project.configureMultiplatformAndroid(libs: VersionCatalog) {
    pluginManager.withPlugin(libs.findPlugin("android-multiplatform").get().get().pluginId) {
      pluginManager.apply(libs.findPlugin("kotlin-multiplatform").get().get().pluginId)
      extensions.configure<KotlinMultiplatformExtension> {
        extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
          namespace = "com.github.kr328.clash.${project.name}"
          compileSdk = 37
          minSdk = 28
          compilerOptions.jvmTarget.set(JvmTarget.fromTarget(libs.requiredVersion("jvmTarget")))
          androidResources.enable = true
        }

        extensions.configure<NamedDomainObjectContainer<KotlinSourceSet>> {
          getByName("commonMain").dependencies {
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
              .forEach { implementation(libs.findLibrary(it).get()) }
          }
        }

        compilerOptions.optIn.addAll(
          "androidx.compose.foundation.ExperimentalFoundationApi",
          "androidx.compose.material3.ExperimentalMaterial3Api",
        )
      }

      pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)
      extensions.configure<ComposeCompilerGradlePluginExtension> {
        stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability.conf"))
      }

      pluginManager.apply(libs.findPlugin("jb-compose").get().get().pluginId)
      extensions.configure<ComposeExtension> {
        extensions.configure<ResourcesExtension> {
          packageOfResClass = "com.github.kr328.clash.${project.name}"
          generateResClass = always
        }
      }
    }
  }

  private fun VersionCatalog.requiredVersion(alias: String) =
    findVersion(alias).get().requiredVersion
}
