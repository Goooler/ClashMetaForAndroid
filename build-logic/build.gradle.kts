plugins {
  alias(libs.plugins.kotlin.jvm)
  `java-gradle-plugin`
}

dependencies {
  implementation(gradleKotlinDsl())
  implementation(libs.gradlePlugin.android)
  implementation(libs.gradlePlugin.compose)
  implementation(libs.gradlePlugin.kotlin)
  implementation(libs.gradlePlugin.kotlin.composeCompiler)
  implementation(libs.gradlePlugin.spotless)
}

gradlePlugin {
  plugins {
    register("tabbyConventions") {
      id = "tabby.conventions"
      implementationClass = "TabbyConventionsPlugin"
    }
  }
}
