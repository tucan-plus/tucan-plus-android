plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("androidx.room")
    id("com.google.devtools.ksp")
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
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.room)
    ksp(libs.room.compiler)
    implementation(project(":connector"))
    implementation(project(":common"))
    implementation(libs.androidx.datastore)
}