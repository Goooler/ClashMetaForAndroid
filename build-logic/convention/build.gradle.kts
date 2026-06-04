import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { `kotlin-dsl` }

group = "com.github.kr328.clash.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

dependencies {
  implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}
