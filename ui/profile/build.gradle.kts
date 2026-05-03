plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(projects.glue)
  implementation(projects.ui)

  implementation(libs.composePreference)
  implementation(libs.quickie.bundled)
  implementation(libs.bytesize)
}
