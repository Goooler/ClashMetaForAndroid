plugins {
  alias(libs.plugins.tabby.kmp.library)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies { implementation(libs.kotlin.serialization.json) }
  }
}
