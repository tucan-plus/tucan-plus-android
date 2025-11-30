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
```

## Setup

https://docs.teamscale.com/howto/integrating-with-your-ide/intellij/

