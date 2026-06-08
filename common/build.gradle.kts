plugins { alias(libs.plugins.android.library) }

dependencies {
  api(platform(libs.koin.bom))
  api(libs.koin.core)

  implementation(libs.kotlin.coroutine)
  implementation(libs.androidx.core)
}
