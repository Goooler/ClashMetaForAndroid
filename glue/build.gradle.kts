plugins { alias(libs.plugins.android.library) }

dependencies {
  api(projects.core)
  api(projects.service)
  api(projects.common)

  implementation(libs.kotlin.coroutine)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.core)
}
