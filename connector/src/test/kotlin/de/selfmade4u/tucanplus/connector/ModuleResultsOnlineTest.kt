package de.selfmade4u.tucanplus.connector

import androidx.datastore.core.DataStore
import de.selfmade4u.tucanplus.AccessesTucan
import de.selfmade4u.tucanplus.LoginSingleton
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assume
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class ModuleResultsOnlineTest(var offset: Int) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "semester {0}")
        fun data(): List<Int> {
            return (-10..10).toList()
        }
    }

    @Category(AccessesTucan::class)
    @Test
    fun testModuleResults() {
        Assume.assumeTrue("Credentials provided", System.getenv("TUCAN_USERNAME") != null && System.getenv("TUCAN_PASSWORD") != null)
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
            val semesters = ModuleResultsConnector.getModuleResultsUncached(
                store, semester.toString()
            )
        }
    }
}