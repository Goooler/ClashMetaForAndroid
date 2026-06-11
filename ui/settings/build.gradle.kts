plugins {
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.ui)

      implementation(libs.composePreference)
      implementation(libs.reorderable)
    }
    androidMain.dependencies {
      implementation(projects.glue)
    }
  }
}
