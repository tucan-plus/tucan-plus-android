// Put everything in here that does not depend on Android
plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
tasks.withType<Test>().configureEach {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}
tasks.withType<Test>().configureEach {
    forkEvery = 100
}
dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ksoup)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.ktor.client.java)
    implementation(project(":common"))
}