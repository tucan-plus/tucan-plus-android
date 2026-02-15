# tucan-plus-android

```
TUCAN_USERNAME=
TUCAN_PASSWORD=
THE_TEAMSCALE_ACCESS_TOKEN=

export $(grep -v '^#' .env | xargs)

https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner

probably the reason why we get multiple coverage files?
https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner#use-android

https://github.com/android/android-test/blob/5b83cd99b2f6df8a7ce910f7b34917b30d73f0ad/runner/android_junit_runner/java/androidx/test/runner/AndroidJUnitRunner.java#L545

https://github.com/android/android-test/blob/main/runner/android_test_orchestrator/java/androidx/test/orchestrator/AndroidTestOrchestrator.java

// maybe we could fork it?

https://developer.android.com/build/extend-agp#variant-api-artifacts-tasks

// but it documents there how the format of the test coverage files is so probably easier to just create the teamscale structure by hand?

https://github.com/android/android-test/blob/5b83cd99b2f6df8a7ce910f7b34917b30d73f0ad/runner/android_test_orchestrator/java/androidx/test/orchestrator/AndroidTestOrchestrator.java#L270
probably calls our test listener to find all tests.

# Unit tests
./gradlew clean :connector:test teamscaleTestUpload

# Integration tests
# mediumPhoneDebugAndroidTest
./gradlew clean mediumPhoneAndroidTest
./gradlew --info teamscaleIntegrationTestsReportUpload # I think this needs to be separate for now

./gradlew mediumPhoneAndroidTest --include-build ~/Documents/teamscale-java-profiler/teamscale-gradle-plugin

createDebugAndroidTestCoverageReport

cat /home/moritz/Documents/tucan-plus-android/app/build/outputs/androidTest-results/managedDevice/debug/mediumPhone/logcat-de.mannodermaus.junit5.ActivityScenarioExtension-initializationError.txt
```

## Setup

https://docs.teamscale.com/howto/integrating-with-your-ide/intellij/

