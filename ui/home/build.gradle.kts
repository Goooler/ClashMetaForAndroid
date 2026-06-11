plugins {
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.ui)

      implementation(libs.composePreference)
    }
    androidMain.dependencies {
      implementation(projects.glue)

      implementation(libs.okhttp.client)
      implementation(libs.kotlin.serialization.json)
      implementation(libs.semver)
    }
    androidUnitTest.dependencies {
      implementation(libs.junit)
      implementation(libs.kotlin.coroutine.test)
      implementation(libs.assertk)
    }
  }
}
