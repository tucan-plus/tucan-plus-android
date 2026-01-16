package de.selfmade4u.tucanplus.connector

import androidx.datastore.core.DataStore
import de.selfmade4u.tucanplus.LoginSingleton
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class MyExamsConnectorOnlineTest {

    companion object {
        @JvmStatic
        fun abc(): List<Int> {
            return (-10..10).toList()
        }
    }

    @MethodSource("abc")
    @Tag("AccessesTucan")
    @ParameterizedTest
    fun testMyExams(offset: Int) {
        assumeTrue(System.getenv("TUCAN_USERNAME") != null && System.getenv("TUCAN_PASSWORD") != null, "Credentials provided")
        runBlocking {
            val semester = 15086000 + offset * 10000
            val credentials = LoginSingleton.getCredentials().getOrThrow()
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
    }
}