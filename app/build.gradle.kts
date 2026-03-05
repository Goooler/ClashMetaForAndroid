import com.android.build.api.variant.FilterConfiguration
import de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.application")
    alias(libs.plugins.download)
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            // TODO: https://github.com/android/gradle-recipes/blob/cbe7c7dea2a3f5b1764756f24bf453d1235c80e2/listenToArtifacts/README.md
            with(output as com.android.build.api.variant.impl.VariantOutputImpl) {
                val abiName = output.filters
                    .find { it.filterType == FilterConfiguration.FilterType.ABI }
                    ?.identifier ?: "universal"
                val newApkName = "cmfa-${versionName.get()}-meta-${abiName}-${variant.buildType}.apk"
                outputFileName = newApkName
            }
        }
    }
}

dependencies {
    compileOnly(project(":hideapi"))

    implementation(project(":core"))
    implementation(project(":service"))
    implementation(project(":design"))
    implementation(project(":common"))

    implementation(libs.kotlin.coroutine)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.coordinator)
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.material)
    implementation(libs.quickie.bundled)
}

val downloadGeoFiles by tasks.registering(Download::class) {
    src(
        listOf(
            "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geoip.metadb",
            "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geosite.dat",
            "https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/GeoLite2-ASN.mmdb",
        ),
    )
    dest("src/main/assets")
    onlyIfModified(true)
    eachFile {
        if (name == "GeoLite2-ASN.mmdb") {
            name = "ASN.mmdb"
        }
    }
}

tasks.preBuild {
    dependsOn(downloadGeoFiles)
}

tasks.clean {
    delete(downloadGeoFiles)
}
