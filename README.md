# tucan-plus-android

```
TUCAN_USERNAME=
TUCAN_PASSWORD=
THE_TEAMSCALE_ACCESS_TOKEN=

export $(grep -v '^#' .env | xargs)

# Unit tests
./gradlew clean :connector:test teamscaleTestUpload

# Integration tests
# mediumPhoneDebugAndroidTest
./gradlew clean mediumPhoneAndroidTest
./gradlew --info teamscaleIntegrationTestsReportUpload # I think this needs to be separate for now

cat /home/moritz/Documents/tucan-plus-android/app/build/outputs/androidTest-results/managedDevice/debug/mediumPhone/logcat-de.mannodermaus.junit5.ActivityScenarioExtension-initializationError.txt
```

## Setup

https://docs.teamscale.com/howto/integrating-with-your-ide/intellij/

