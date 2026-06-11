plugins {
  alias(libs.plugins.android.multiplatform)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.common)
    }
  }
}
