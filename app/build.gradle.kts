import com.android.build.api.variant.FilterConfiguration
import de.undercouch.gradle.tasks.download.Download
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.download)
}

android {
  defaultConfig {
    applicationId = "io.github.goooler.tabby"
    targetSdk = 35
    versionName = "3.0.0"
    versionCode = checkNotNull(versionName).toVersionCode()
  }

  val releaseSigning =
    signingConfigs.create("release") {
      storeFile = file("release.keystore")
      storePassword = "demo.app"
      keyAlias = "key0"
      keyPassword = "demo.app"
    }

  buildTypes {
    all { signingConfig = releaseSigning }
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  packaging {
    jniLibs { useLegacyPackaging = true }
    resources { excludes.add("DebugProbesKt.bin") }
  }

  splits {
    abi {
      isEnable = true
      isUniversalApk = true
      reset()
      include("arm64-v8a", "x86_64")
    }
  }
}

androidComponents {
  onVariants(selector().withBuildType("release")) { variant ->
    variant.outputs.forEach { output ->
      with(output) {
        val abiName =
          filters.find { it.filterType == FilterConfiguration.FilterType.ABI }?.identifier
            ?: "universal"
        outputFileName =
          "Tabby-${versionName.get()}-${versionCode.get()}-$abiName-${variant.buildType}.apk"
      }
    }
  }
}

dependencies {
  implementation(projects.glue)
  implementation(projects.ui)
  implementation(projects.ui.crash)
  implementation(projects.ui.home)
  implementation(projects.ui.log)
  implementation(projects.ui.proxy)
  implementation(projects.ui.profile)
  implementation(projects.ui.settings)

  implementation(libs.kotlin.coroutine)

  implementation(libs.androidx.core)
  implementation(libs.androidx.activity.compose)

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.android)
}

val downloadGeoFiles by
  tasks.registering(Download::class) {
    src(
      listOf(
        "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geoip.metadb",
        "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geosite.dat",
        "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/GeoLite2-ASN.mmdb",
      )
    )
    dest("src/main/assets")
    onlyIfModified(true)
    eachFile {
      if (name == "GeoLite2-ASN.mmdb") {
        name = "ASN.mmdb"
      }
    }

    val skipDownloadGeoFiles = providers.provider {
      val propsFile =
        rootProject.file("local.properties").takeIf { it.exists() }
          ?: rootProject.file("gradle.properties")
      val properties = Properties().apply { propsFile.inputStream().use { load(it) } }
      properties.getProperty("skip.downloadGeoFiles").toBoolean() &&
        dest.exists() &&
        dest.listFiles().orEmpty().size == 3
    }
    // Skip the task when the flag is set.
    onlyIf { !skipDownloadGeoFiles.get() }
  }

tasks.preBuild { dependsOn(downloadGeoFiles) }

tasks.clean { delete(downloadGeoFiles) }

fun String.toVersionCode(): Int {
  val (major, minor, patch) = split('.').map { it.toInt() }
  return major * 1_000_000 + minor * 1_000 + patch
}
