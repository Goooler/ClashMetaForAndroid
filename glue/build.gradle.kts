plugins {
  alias(libs.plugins.android.library)
}

dependencies {
  api(projects.core)
  api(projects.core.model)
  api(projects.service)
  api(projects.service.database)
  api(projects.common)

  implementation(libs.kotlin.coroutine.android)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.core)
}
