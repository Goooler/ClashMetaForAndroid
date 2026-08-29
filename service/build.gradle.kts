plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

dependencies {
  implementation(projects.core.model)

  implementation(projects.core)
  implementation(projects.common)

  implementation(libs.kotlin.coroutine.android)
  implementation(libs.kotlin.serialization.json)
  implementation(libs.androidx.core)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kaidl.runtime)
  implementation(libs.rikkax.multiprocess)

  ksp(libs.kaidl.compiler)
  ksp(libs.androidx.room.compiler)
}
