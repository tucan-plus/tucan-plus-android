import com.teamscale.extension.TeamscaleTaskExtension
import com.teamscale.reporting.testwise.TestwiseCoverageReport

// Put everything in here that does not depend on Android
plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.teamscale") version "36.1.0"
}

val testwiseCoverageReport by tasks.registering(TestwiseCoverageReport::class) {
    executionData(tasks.test)
}

tasks.test {
    maxParallelForks = 1
    inputs.property("TUCAN_USERNAME", System.getenv("TUCAN_USERNAME"))
    inputs.property("TUCAN_PASSWORD", System.getenv("TUCAN_PASSWORD"))
    useJUnitPlatform()
    finalizedBy(testwiseCoverageReport)
    configure<JacocoTaskExtension> {
        includes = listOf("tia.*")
    }
    configure<TeamscaleTaskExtension> {
        collectTestwiseCoverage = true
        runImpacted = true
        includeAddedTests = true
        includeFailedAndSkipped = true
        partition = "Unit Tests"
    }
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