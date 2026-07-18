import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.AndroidBasePlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class TabbyConventionsPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    def libs = project.extensions.getByType(VersionCatalogsExtension).named("libs")
    def requiredVersion = { alias -> libs.findVersion(alias).get().requiredVersion }

    project.plugins.withType(AndroidBasePlugin).configureEach {
      project.extensions.configure(CommonExtension) { android ->
        android.namespace = "com.github.kr328.clash.${project.name}"
        android.compileSdk = 37
        android.defaultConfig { defaultConfig ->
          defaultConfig.minSdk = 28
          // TODO: https://github.com/Goooler/golang-gradle-plugin/pull/76
          defaultConfig.externalNativeBuild.cmake.abiFilters.addAll("arm64-v8a", "x86_64")
        }
        android.ndkVersion = "29.0.14206865"
        android.compileOptions { options ->
          options.sourceCompatibility(requiredVersion("jvmTarget"))
          options.targetCompatibility(requiredVersion("jvmTarget"))
        }
      }
    }

    project.tasks.withType(KotlinCompile).configureEach { task ->
      task.compilerOptions { options ->
        options.allWarningsAsErrors = true
        options.jvmTarget = JvmTarget.fromTarget(requiredVersion("jvmTarget"))
        options.freeCompilerArgs.addAll("-Xcontext-sensitive-resolution")
      }
    }

    project.pluginManager.apply(libs.findPlugin("spotless").get().get().pluginId)
    project.extensions.configure(SpotlessExtension) { spotless ->
      spotless.kotlin {
        target("src/**/*.kt")
        ktfmt(libs.findLibrary("ktfmt").get().get().version).googleStyle()
      }
      spotless.kotlinGradle { ktfmt(libs.findLibrary("ktfmt").get().get().version).googleStyle() }
    }

    project.pluginManager.withPlugin(libs.findPlugin("android-multiplatform").get().get().pluginId) {
      project.pluginManager.apply(libs.findPlugin("kotlin-multiplatform").get().get().pluginId)
      project.extensions.configure(KotlinMultiplatformExtension) { kotlin ->
        kotlin.extensions.configure(KotlinMultiplatformAndroidLibraryTarget) { android ->
          android.namespace = "com.github.kr328.clash.${project.name}"
          android.compileSdk = 37
          android.minSdk = 28
          android.compilerOptions.jvmTarget = JvmTarget.fromTarget(requiredVersion("jvmTarget"))
          android.androidResources.enable = true
        }

        kotlin.sourceSets.getByName("commonMain").dependencies { dependencies ->
          [
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
          ].each { dependencies.implementation(libs.findLibrary(it).get()) }
        }

        kotlin.compilerOptions.optIn.addAll(
          "androidx.compose.foundation.ExperimentalFoundationApi",
          "androidx.compose.material3.ExperimentalMaterial3Api",
        )
      }

      project.pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)
      project.extensions.configure(ComposeCompilerGradlePluginExtension) { composeCompiler ->
        composeCompiler.stabilityConfigurationFiles.add(project.rootProject.layout.projectDirectory.file("stability.conf"))
      }

      project.pluginManager.apply(libs.findPlugin("jb-compose").get().get().pluginId)
      project.extensions.configure(ComposeExtension) { compose ->
        compose.extensions.configure(ResourcesExtension) { resources ->
          resources.packageOfResClass = "com.github.kr328.clash.${project.name}"
          resources.generateResClass = resources.always
        }
      }
    }
  }
}
