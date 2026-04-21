plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
}

android { buildFeatures { viewBinding = true } }

dependencies {
  implementation(projects.common)
  implementation(projects.core)
  implementation(projects.service)

  implementation(libs.kotlin.coroutine)

  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.ui.util)
  debugImplementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  implementation(libs.androidx.core)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.coordinator)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.viewpager)
  implementation(libs.google.material)
  implementation(libs.composePreference)
}
