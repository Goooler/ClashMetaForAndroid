plugins {
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    androidMain.dependencies {
      implementation(projects.glue)
      implementation(projects.ui)

      implementation(libs.composePreference)
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
