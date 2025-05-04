plugins {
    kotlin("android")
    id("com.android.library")
}

dependencies {
    compileOnly(projects.hideapi)

    implementation(libs.kotlin.coroutine)
    implementation(libs.androidx.core)
}
