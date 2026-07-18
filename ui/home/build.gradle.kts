plugins {
  id("tabby.conventions")
  alias(libs.plugins.android.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.ui)

      implementation(libs.composePreference)

      implementation(libs.semver)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.cio)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.client.serialization.json)
      implementation(libs.kotlin.serialization.json)
    }
    androidMain.dependencies {
      implementation(projects.glue)
    }
    androidUnitTest.dependencies {
      implementation(libs.junit)
      implementation(libs.kotlin.coroutine.test)
      implementation(libs.assertk)
    }
  }
}
