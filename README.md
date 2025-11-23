# tucan-plus-android

```
TUCAN_USERNAME= TUCAN_PASSWORD= ./gradlew -Dteamscale.access-token= clean :connector:test teamscaleTestUpload


https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner
./gradlew -Pandroid.testInstrumentationRunnerArguments.username= -Pandroid.testInstrumentationRunnerArguments.password= clean mediumPhoneAndroidTest createDebugAndroidTestCoverageReport
Android Test Orchestrator
https://medium.com/stepstone-tech/android-test-orchestrator-unmasked-83b8879928fa
https://stackoverflow.com/questions/48053005/coverage-for-android-tests-using-orchestrator
https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner
https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:build-system/gradle-core/src/main/java/com/android/build/gradle/internal/coverage/JacocoReportTask.kt
./gradlew -Pandroid.testInstrumentationRunnerArguments.username= -Pandroid.testInstrumentationRunnerArguments.password= clean mediumPhoneAndroidTest createManagedDeviceDebugAndroidTestCoverageReport

./gradlew  -Pandroid.testInstrumentationRunnerArguments.username= -Pandroid.testInstrumentationRunnerArguments.password= -Dteamscale.access-token= --stacktrace --debug  --rerun-tasks jacocoReportAll
find | grep successfulLogin

./app/build/reports/jacoco/de.selfmade4u.tucanplus.ComposeTest#wrongUsernameAndPassword.xml
```

## Setup

https://docs.teamscale.com/howto/integrating-with-your-ide/intellij/

