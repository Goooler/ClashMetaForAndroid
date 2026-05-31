plugins { alias(libs.plugins.android.library) }

dependencies {
  implementation(libs.kotlin.coroutine)
  implementation(libs.androidx.core)

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
}
