package de.selfmade4u.tucanplus.connector

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.LoginSingleton
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginFailure
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginSuccess
import de.selfmade4u.tucanplus.root
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.java.Java
import io.ktor.client.engine.jetty.jakarta.Jetty
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LoginTest {
    @Tag("DoesNotAccessTucan")
    @Test
    fun testParsingWrongPasswordResponse() {
        val html = this::class.java.getResource("/login_wrong_password.html")!!.readText()

        val doc: Document = Ksoup.parse(html = html)

        runBlocking {
            val result = root(doc) {
                parseLoginFailure()
            }
            print(result)
        }
    }

    @Tag("DoesNotAccessTucan")
    @Test
    fun testParsingTooManyFailedAttemptsResponse() {
        val html = this::class.java.getResource("/login_too_many_failed_attempts.html")!!.readText()

        val doc: Document = Ksoup.parse(html = html)

        runBlocking {
            val result = root(doc) {
                parseLoginFailure()
            }
            print(result)
        }
    }

    @Tag("DoesNotAccessTucan")
    @Test
    fun testParsingCorrectPasswordResponse() {
        val html = this::class.java.getResource("/login_correct_password.html")!!.readText()

        val doc: Document = Ksoup.parse(html = html)

        runBlocking {
            val result = root(doc) {
                parseLoginSuccess()
            }
            print(result)
        }
    }

    @Tag("AccessesTucan")
    @Test
    fun testLoginWrongUsernameWrongPassword() {
        val client = HttpClient()
        runBlocking {
            TucanLogin.doLogin(client, "wrongusername", "wrongpassword")
        }
    }

    @Tag("AccessesTucan")
    @Test
    fun testLoginCorrectUsernameAndPassword() {
        assumeTrue(System.getenv("TUCAN_USERNAME") != null && System.getenv("TUCAN_PASSWORD") != null, "Credentials provided")
        runBlocking {
            LoginSingleton.getCredentials()
        }
    }

    @Tag("AccessesTucan")
    @Test
    fun newLogin() {
        println(System.getProperty("java.version"))
        assumeTrue(System.getenv("TUCAN_USERNAME") != null && System.getenv("TUCAN_PASSWORD") != null, "Credentials provided")
        val client = HttpClient(Java) {
            followRedirects = false
            install(HttpCookies) {
                storage = PersistentCookiesStorage()
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
        }
        runBlocking {
            TucanLogin.doNewLogin(client, System.getenv("TUCAN_USERNAME")!!, System.getenv("TUCAN_PASSWORD")!!)
        }
    }
}