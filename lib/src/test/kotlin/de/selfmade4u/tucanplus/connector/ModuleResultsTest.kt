package de.selfmade4u.tucanplus.connector

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.connector.ModuleResults.parseModuleResults
import de.selfmade4u.tucanplus.root
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ModuleResultsTest {

    @Test
    fun testParseModuleResults2() {
        val html =
            this::class.java.getResource("/module_results.html")!!
                .readText()

        val doc: Document = Ksoup.parse(html = html)

        runBlocking {
            val result = root(doc) {
                parseModuleResults("183985067121045")
            }
            print(result)
        }
    }

}