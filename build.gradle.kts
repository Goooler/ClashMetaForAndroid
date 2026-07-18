plugins {
  id("tabby.conventions")
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.multiplatform) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.jb.compose) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.golang) apply false
  alias(libs.plugins.spotless) apply false
}
