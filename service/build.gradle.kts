plugins {
  alias(libs.plugins.android.library)
}

dependencies {
  implementation(projects.core.model)
  implementation(projects.service.database)
  implementation(projects.service.remote)

  implementation(projects.core)
  implementation(projects.common)

  implementation(libs.kotlin.coroutine.android)
  implementation(libs.androidx.core)
  implementation(libs.rikkax.multiprocess)
}
