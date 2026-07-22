plugins {
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android {
    namespace = "com.github.kr328.clash.core.model"
  }

  sourceSets {
    commonMain.dependencies {
      api(libs.kotlin.serialization.json)
    }
  }
}
