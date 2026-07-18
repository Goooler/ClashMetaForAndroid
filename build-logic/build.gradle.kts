plugins {
  `groovy-gradle-plugin`
}

dependencies {
  implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
  implementation("com.diffplug.spotless:spotless-plugin-gradle:${libs.plugins.spotless.get().version}")
  implementation("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.cmp.get()}")
  implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}

gradlePlugin {
  plugins {
    register("tabbyConventions") {
      id = "tabby.conventions"
      implementationClass = "TabbyConventionsPlugin"
    }
  }
}
