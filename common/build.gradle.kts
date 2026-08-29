plugins {
  alias(libs.plugins.android.multiplatform)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.koin.core)
      api(libs.kermit)
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
