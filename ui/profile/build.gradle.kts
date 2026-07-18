plugins {
  id("tabby.conventions")
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.ui)

      implementation(libs.composePreference)
      implementation(libs.bytesize)
    }
    androidMain.dependencies {
      implementation(projects.glue)

      implementation(libs.quickie.bundled)
    }
  }
}
