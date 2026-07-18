plugins {
  id("tabby.conventions")
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
    }
  }
}

compose {
  resources {
    publicResClass = true
  }
}
