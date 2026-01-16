import com.teamscale.TeamscaleUpload
import org.gradle.api.tasks.testing.logging.TestLogEvent

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
    id("androidx.room") version "2.8.4" apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    id("com.teamscale") version "36.2.0"
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
    from(project(":connector").tasks.named("testwiseCoverageReport"))
}
/*
tasks.register<TeamscaleUpload>("teamscaleIntegrationTestsReportUpload") {
    partition = "Integration Tests"
    from(project(":app").tasks.named("testwiseCoverageReport"))
}
*/
teamscale {
    server {
        url = "https://teamscale.selfmade4u.de"
        project = "tucan-plus-android"
        userName = "admin"
        userAccessToken = System.getenv("THE_TEAMSCALE_ACCESS_TOKEN")
    }
}