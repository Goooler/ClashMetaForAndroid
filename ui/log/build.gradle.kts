plugins {
  id("tabby.conventions")
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.ui)
    }
    androidMain.dependencies {
      implementation(projects.glue)
    }
  }
}
