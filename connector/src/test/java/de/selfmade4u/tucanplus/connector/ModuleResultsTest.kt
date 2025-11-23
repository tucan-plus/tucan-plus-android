package de.selfmade4u.tucanplus.connector

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.EnglishLocalizer
import de.selfmade4u.tucanplus.GermanLocalizer
import de.selfmade4u.tucanplus.connector.ModuleResultsConnector.parseModuleResults
import de.selfmade4u.tucanplus.root
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ModuleResultsTest {

    @Tag("DoesNotAccessTucan")
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

    @Tag("DoesNotAccessTucan")
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
}