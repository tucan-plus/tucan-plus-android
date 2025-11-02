// Put everything in here that does not depend on Android
plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.google.devtools.ksp")
    id("androidx.room")
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
room {
    schemaDirectory("$projectDir/schemas")
}
dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ksoup)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room)
    testImplementation(libs.junit)
    ksp(libs.room.compiler)
}