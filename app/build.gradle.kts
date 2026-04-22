import com.android.build.api.variant.FilterConfiguration
import de.undercouch.gradle.tasks.download.Download
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.download)
}

android {
  namespace = "com.github.kr328.clash"
  defaultConfig {
    applicationId = "com.github.metacubex.clash"
    targetSdk = 35
    versionCode = 212101
    versionName = "2.12.1"
    resValue("integer", "release_code", versionCode.toString())
    resValue("string", "release_name", "v$versionName")
    resValue("string", "launch_name", "@string/launch_name_meta")
    resValue("string", "application_name", "@string/application_name_meta")
    ndk.abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
  }

  val keystore = rootProject.file("signing.properties")
  val releaseSigning =
    if (keystore.exists()) {
      signingConfigs.create("release") {
        val prop = Properties()
        keystore.inputStream().use(prop::load)
        storeFile = rootProject.file("release.keystore")
        storePassword = prop.getProperty("keystore.password")
        keyAlias = prop.getProperty("key.alias")
        keyPassword = prop.getProperty("key.password")
      }
    } else {
      signingConfigs["debug"]
    }

  buildTypes {
    all { signingConfig = releaseSigning }
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug { versionNameSuffix = ".debug" }
  }

  buildFeatures { resValues = true }

  packaging {
    jniLibs { useLegacyPackaging = true }
    resources { excludes.add("DebugProbesKt.bin") }
  }

  splits {
    abi {
      isEnable = true
      isUniversalApk = true
      reset()
      include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
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
        val newApkName = "cmfa-${versionName.get()}-$abiName-${variant.buildType}.apk"
        outputFileName = newApkName
      }
    }
  }
}

dependencies {
  implementation(projects.core)
  implementation(projects.service)
  implementation(projects.design)
  implementation(projects.common)

  implementation(libs.kotlin.coroutine)
  implementation(libs.androidx.core)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.coordinator)
  implementation(libs.androidx.recyclerview)
  implementation(libs.google.material)
  implementation(libs.quickie.bundled)
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
  }

tasks.preBuild { dependsOn(downloadGeoFiles) }

tasks.clean { delete(downloadGeoFiles) }
