import com.teamscale.TeamscaleUpload
import com.teamscale.reporting.testwise.TestwiseCoverageReport
import org.gradle.api.tasks.testing.logging.TestLogEvent

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.1" apply false
    id("androidx.room") version "2.8.4" apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    id("com.teamscale") version "36.1.0"
}
allprojects {
    tasks.withType<Test>().configureEach {
        testLogging {
            events = setOf(
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STARTED,
                TestLogEvent.FAILED
            )
        }
    }
}
evaluationDependsOn(":connector")
evaluationDependsOn(":app")
tasks.register<TeamscaleUpload>("teamscaleTestUpload") {
    partition = "Unit Tests"
    //from(project(":connector").tasks.jacocoTestReport)
    from(project(":connector").tasks.named("testwiseCoverageReport"))
}
tasks.register<TeamscaleUpload>("teamscaleIntegrationTestsReportUpload") {
    partition = "Integration Tests"
    addReport("JUNIT", project(":app").layout.buildDirectory.file("outputs/androidTest-results/managedDevice/debug/mediumPhone/TEST-mediumPhone-_app-.xml"))
    project(":app").tasks.withType(JacocoReport::class).forEach { from(it) }
}
teamscale {
    server {
        url = "https://teamscale.selfmade4u.de"
        project = "tucan-plus-android"
        userName = "admin"
        userAccessToken = System.getenv("THE_TEAMSCALE_ACCESS_TOKEN")
    }
}