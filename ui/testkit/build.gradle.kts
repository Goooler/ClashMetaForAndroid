plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
}

dependencies {
  implementation(libs.roborazzi.core)
  implementation(libs.roborazzi.compose)
}
