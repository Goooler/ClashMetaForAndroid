plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
}

dependencies {
  implementation(projects.glue)

  compileOnly(platform(libs.koin.bom))
  compileOnly(libs.koin.android)
  compileOnly(libs.koin.viewMdeol)
}
