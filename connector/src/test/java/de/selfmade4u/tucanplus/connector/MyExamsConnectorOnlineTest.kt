package de.selfmade4u.tucanplus.connector

class MyExamsConnectorOnlineTest {

    companion object {
        @JvmStatic
        fun abc(): List<Int> {
            return (-10..10).toList()
        }
    }

    /*
    @Disabled
    @ParameterizedTest
    @MethodSource("abc")
    @Tag("AccessesTucan")
    fun testMyExams(offset: Int) {
        assumeTrue(System.getenv("TUCAN_USERNAME") != null && System.getenv("TUCAN_PASSWORD") != null, "Credentials provided")
        runBlocking {
            val semester = 15086000 + offset * 10000
            val credentials = LoginSingleton.getCredentials()
            val store = object : DataStore<OptionalCredentialSettings> {
                val value = MutableStateFlow(OptionalCredentialSettings(credentials))

                override val data: Flow<OptionalCredentialSettings>
                    get() = value

                override suspend fun updateData(transform: suspend (t: OptionalCredentialSettings) -> OptionalCredentialSettings): OptionalCredentialSettings {
                    value.value = transform(value.value)
                    return value.value
                }
            }
            MyExamsConnector.getUncached(
                store, semester.toString()
            )
        }
    }*/
}