plugins {
  alias(libs.plugins.android.multiplatform)
}

kotlin {
  sourceSets {
    androidMain.dependencies {
      implementation(projects.glue)
    }
  }
}
