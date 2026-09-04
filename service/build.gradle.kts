plugins {
  alias(libs.plugins.android.library)
}

dependencies {
  api(projects.service.database)
  api(projects.service.remote)

  implementation(projects.core.model)

  implementation(projects.core)
  implementation(projects.common)

  implementation(libs.kotlin.coroutine.android)
  implementation(libs.androidx.core)
  implementation(libs.rikkax.multiprocess)
}
