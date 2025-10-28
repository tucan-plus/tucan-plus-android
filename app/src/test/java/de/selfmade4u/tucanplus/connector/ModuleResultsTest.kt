package de.selfmade4u.tucanplus.connector

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.connector.ModuleResults.parseModuleResults
import de.selfmade4u.tucanplus.root
import org.junit.Test

class ModuleResultsTest {

    @Test
    fun testParseModuleResults() {
        val html = this::class.java.getResource("/module_results.html")!!.readText()

        val doc: Document = Ksoup.parse(html = html)

        val result = root(doc) {
            parseModuleResults("283072090184954")
        }
        print(result)
    }

    @Test
    fun testParseModuleResults2() {
        val html =
            this::class.java.getResource("/parsingerrors/errorc140e046-9932-42fe-ad99-2d4c8d4bf2ab.html")!!
                .readText()

        val doc: Document = Ksoup.parse(html = html)

        val result = root(doc) {
            parseModuleResults("283072090184954")
        }
        print(result)
    }
}