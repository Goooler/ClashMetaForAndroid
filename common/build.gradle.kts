plugins {
  alias(libs.plugins.android.multiplatform)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.koin.core)

      implementation(libs.kotlin.coroutine)
    }
    androidMain.dependencies {
      implementation(libs.androidx.core)
      implementation(libs.androidx.browser)
    }
  }
}

compose {
  resources {
    publicResClass = true
  }
}
