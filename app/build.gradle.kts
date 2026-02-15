import com.teamscale.TeamscaleUpload
import com.teamscale.extension.TeamscaleTaskExtension
import com.teamscale.reporting.testwise.TestwiseCoverageReport
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.google.devtools.ksp")
    alias(libs.plugins.baselineprofile)
    //id("dev.reformator.stacktracedecoroutinator") version "2.5.7"
    id("de.mannodermaus.android-junit5") version "1.14.0.0"
    //id("com.teamscale")
    //jacoco
}
/*
stacktraceDecoroutinator {
    embedDebugProbesForAndroid = true
}*/

android {
    namespace = "de.selfmade4u.tucanplus"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "de.selfmade4u.tucanplus"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        testInstrumentationRunnerArguments["TUCAN_USERNAME"] = System.getenv("TUCAN_USERNAME")
        testInstrumentationRunnerArguments["TUCAN_PASSWORD"] = System.getenv("TUCAN_PASSWORD")
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    packaging {
        resources {
            excludes.add("/org/**")
        }
    }
    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                //decoroutinatorAndroidProGuardRules,
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("debug").assets.directories.add("$projectDir/schemas") // Room
    }
    baselineProfile {
        dexLayoutOptimization = true
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        managedDevices {
            localDevices {
                create("mediumPhone") {
                    device = "Medium Phone"
                    apiLevel = 34
                    systemImageSource = "google_apis"
                    testedAbi = "x86_64"
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.splashscreen)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore)
    implementation(libs.room)
    implementation(project(":common"))
    implementation(project(":data"))
    implementation(project(":connector"))
    implementation(libs.androidx.profileinstaller)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    androidTestImplementation(platform(libs.junit.bom))
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.junit.jupiter.params)
    androidTestRuntimeOnly(libs.junit.jupiter.engine)
    androidTestRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    //androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    "baselineProfile"(project(":baselineprofile"))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.runner)
    androidTestUtil(libs.androidx.orchestrator)
    androidTestImplementation(libs.android.test.compose)
    androidTestImplementation(libs.androidx.compose.ui.test.android)
    debugImplementation(libs.androidx.ui.test.manifest)
}
/*
project.tasks.withType<Test> {
    configure<TeamscaleTaskExtension> {
        collectTestwiseCoverage = true
    }
}

tasks.register<TestwiseCoverageReport>("testwiseCoverageReport") {
    executionData(tasks.withType<Test>())
}

junitPlatform {
    jacocoOptions {

    }
}
*/