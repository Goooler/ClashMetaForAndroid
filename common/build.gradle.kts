plugins {
  alias(libs.plugins.android.multiplatform)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.koin.core)

      implementation(libs.kotlin.coroutine.core)
    }
    androidMain.dependencies {
      implementation(libs.androidx.core)
    }
  }
}

compose {
  resources {
    publicResClass = true
  }
}
