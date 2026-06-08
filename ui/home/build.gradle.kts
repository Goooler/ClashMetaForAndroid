plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(projects.glue)
  implementation(projects.ui)

  implementation(libs.composePreference)
  implementation(libs.okhttp.client)
  implementation(libs.kotlin.serialization.json)
  implementation(libs.semver)

  testImplementation(libs.junit)
  testImplementation(libs.kotlin.coroutine.test)
  testImplementation(libs.assertk)
}
