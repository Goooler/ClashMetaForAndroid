plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
}

dependencies {
  implementation(projects.glue)
}
