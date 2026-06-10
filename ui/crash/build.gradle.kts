plugins {
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    androidMain.dependencies {
      implementation(projects.glue)
      implementation(projects.ui)

      implementation(libs.composePreference)
    }
  }
}
