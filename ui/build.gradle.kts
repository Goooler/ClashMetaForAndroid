plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
}

dependencies { implementation(projects.glue) }

tasks.withType<Test> {
  // There is no any test for this project yet.
  failOnNoDiscoveredTests = false
}
