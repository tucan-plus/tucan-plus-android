# tucan-plus-android

```
TUCAN_USERNAME= TUCAN_PASSWORD= ./gradlew -Dteamscale.access-token= clean :connector:test teamscaleTestUpload

./gradlew -Pandroid.testInstrumentationRunnerArguments.username= -Pandroid.testInstrumentationRunnerArguments.password= clean mediumPhoneAndroidTest createManagedDeviceDebugAndroidTestCoverageReport

app/build/outputs/androidTest-results/managedDevice/debug/mediumPhone/TEST-mediumPhone-_app-.xml

./gradlew  -Pandroid.testInstrumentationRunnerArguments.username= -Pandroid.testInstrumentationRunnerArguments.password= -Dteamscale.access-token= --info jacocoReportAll teamscaleIntegrationTestsReportUpload
```

## Setup

https://docs.teamscale.com/howto/integrating-with-your-ide/intellij/

