# tucan-plus-android

```
TUCAN_USERNAME=
TUCAN_PASSWORD=
THE_TEAMSCALE_ACCESS_TOKEN=

export $(grep -v '^#' .env | xargs)

./gradlew clean :connector:test teamscaleTestUpload

./gradlew clean mediumPhoneAndroidTest 

./gradlew --info teamscaleIntegrationTestsReportUpload
```

## Setup

https://docs.teamscale.com/howto/integrating-with-your-ide/intellij/

