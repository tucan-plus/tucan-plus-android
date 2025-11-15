package de.selfmade4u.tucanplus.connector

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.DoesNotAccessTucan
import de.selfmade4u.tucanplus.GermanLocalizer
import de.selfmade4u.tucanplus.connector.MyExamsConnector.parseResults
import de.selfmade4u.tucanplus.root
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category

class MyExamsConnectorTest {

    @Category(DoesNotAccessTucan::class)
    @Test
    fun testParseExams() {
        val html =
            this::class.java.getResource("/my_exams.html")!!
                .readText()

        val doc: Document = Ksoup.parse(html = html)

        runBlocking {
            val result = root(doc) {
                parseResults("000318", "390052152696349", GermanLocalizer)
            }
            print(result)
        }
    }
}