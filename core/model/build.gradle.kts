plugins {
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.kotlin.serialization.json)
    }
  }
}
