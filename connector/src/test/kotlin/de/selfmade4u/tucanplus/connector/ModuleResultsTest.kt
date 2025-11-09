package de.selfmade4u.tucanplus.connector

import androidx.datastore.core.DataStore
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.AccessesTucan
import de.selfmade4u.tucanplus.DoesNotAccessTucan
import de.selfmade4u.tucanplus.EnglishLocalizer
import de.selfmade4u.tucanplus.GermanLocalizer
import de.selfmade4u.tucanplus.LoginSingleton
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.connector.ModuleResults.parseModuleResults
import de.selfmade4u.tucanplus.root
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assume
import org.junit.Test
import org.junit.experimental.categories.Category

class ModuleResultsTest {

    @Category(DoesNotAccessTucan::class)
    @Test
    fun testParseModuleResultsDe() {
        val html =
            this::class.java.getResource("/module_results.html")!!
                .readText()

        val doc: Document = Ksoup.parse(html = html)

        runBlocking {
            val result = root(doc) {
                parseModuleResults("000324", "183985067121045", GermanLocalizer)
            }
            print(result)
        }
    }

    @Category(DoesNotAccessTucan::class)
    @Test
    fun testParseModuleResultsEn() {
        val html =
            this::class.java.getResource("/module_results_en.html")!!
                .readText()

        val doc: Document = Ksoup.parse(html = html)

        runBlocking {
            val result = root(doc) {
                parseModuleResults("000363", "059144925859855", EnglishLocalizer)
            }
            print(result)
        }
    }

    @Category(AccessesTucan::class)
    @Test
    fun testModuleResults() {
        Assume.assumeTrue("Credentials provided", System.getenv("TUCAN_USERNAME") != null && System.getenv("TUCAN_PASSWORD") != null)
        runBlocking {
            val credentials = LoginSingleton.getCredentials()
            ModuleResults.getModuleResultsUncached(
                object : DataStore<OptionalCredentialSettings> {
                    val value = MutableStateFlow(OptionalCredentialSettings(credentials))

                    override val data: Flow<OptionalCredentialSettings>
                        get() = value

                    override suspend fun updateData(transform: suspend (t: OptionalCredentialSettings) -> OptionalCredentialSettings): OptionalCredentialSettings {
                        value.value = transform(value.value)
                        return value.value
                    }
                }
            )
        }
    }
}