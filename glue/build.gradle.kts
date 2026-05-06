plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.metro)
}

dependencies {
  api(projects.core)
  api(projects.service)
  api(projects.common)

  implementation(libs.kotlin.coroutine)
  implementation(libs.androidx.core)
}
