plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(projects.core)
  api(projects.service)
  api(projects.common)

  implementation(libs.composePreference)

  implementation(libs.quickie.bundled)
  implementation(libs.bytesize)
}
