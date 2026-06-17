plugins {
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.ui)

      implementation(libs.composePreference)

      implementation(libs.semver)
      implementation(libs.kotlin.serialization.json)
    }
    androidMain.dependencies {
      implementation(projects.glue)

      implementation(libs.okhttp.client)
    }
    androidUnitTest.dependencies {
      implementation(libs.junit)
      implementation(libs.kotlin.coroutine.test)
      implementation(libs.assertk)
    }
  }
}
