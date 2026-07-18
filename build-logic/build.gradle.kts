plugins {
  alias(libs.plugins.kotlin.jvm)
  `java-gradle-plugin`
}

dependencies {
  implementation(libs.gradlePlugin.android)
  implementation(libs.gradlePlugin.compose)
  implementation(libs.gradlePlugin.kotlin)
  implementation(libs.gradlePlugin.kotlin.composeCompiler)
  implementation(libs.gradlePlugin.spotless)

  // TODO: https://github.com/gradle/gradle/issues/15383
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
  plugins {
    register("tabbyConventions") {
      id = "tabby.conventions"
      implementationClass = "TabbyConventionsPlugin"
    }
  }
}
