plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.ksp)
}

dependencies {
  implementation(projects.core.model)
  implementation(projects.core)

  implementation(libs.kotlin.coroutine.android)
  api(libs.kaidl.runtime)

  ksp(libs.kaidl.compiler)
}
