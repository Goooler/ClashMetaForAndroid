plugins {
    kotlin("android")
    id("com.android.library")
}

dependencies {
    implementation(projects.common)
    implementation(projects.core)
    implementation(projects.service)

    implementation(libs.kotlin.coroutine)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.coordinator)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.viewpager)
    implementation(libs.google.material)
}
