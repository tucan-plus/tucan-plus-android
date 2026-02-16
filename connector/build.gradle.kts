import com.teamscale.extension.TeamscaleTaskExtension
import com.teamscale.reporting.testwise.TestwiseCoverageReport

plugins {
    id("java-library")
    kotlin("jvm")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.teamscale") version "36.3.0"
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ksoup)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.ktor.client.java)
    testImplementation(libs.ktor.client.cio)
    implementation(project(":common"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    implementation(kotlin("stdlib-jdk8"))
}
tasks.register<TestwiseCoverageReport>("testwiseCoverageReport") {
    executionData(tasks.test)
}
// https://docs.teamscale.com/tutorial/tia-java/
tasks.test {
    inputs.property("TUCAN_USERNAME", System.getenv("TUCAN_USERNAME"))
    inputs.property("TUCAN_PASSWORD", System.getenv("TUCAN_PASSWORD"))
    useJUnitPlatform()
    maxParallelForks = 1
    configure<JacocoTaskExtension> {
        includes = listOf("de.selfmade4u.*")
    }
    //finalizedBy(tasks.named("testwiseCoverageReport"))
    configure<TeamscaleTaskExtension> {
        collectTestwiseCoverage = true
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