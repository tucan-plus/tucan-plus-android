import com.teamscale.extension.TeamscaleTaskExtension

// Put everything in here that does not depend on Android
plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    jacoco
    id("com.teamscale") version "36.1.0"
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
tasks.register<Test>("unitTest") {
    configure<TeamscaleTaskExtension> {
        collectTestwiseCoverage = true
        runImpacted = true
        includeAddedTests = true
        includeFailedAndSkipped = true
        partition = "Unit Tests"
    }
}