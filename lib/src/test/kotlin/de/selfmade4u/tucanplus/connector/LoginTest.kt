package de.selfmade4u.tucanplus.connector

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.AccessesTucan
import de.selfmade4u.tucanplus.LoginSingleton
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginFailure
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginSuccess
import de.selfmade4u.tucanplus.root
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category

class LoginTest {
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

    @Category(AccessesTucan::class)
    @Test
    fun testLoginWrongUsernameWrongPassword() {
        val client = HttpClient()
        runBlocking {
            TucanLogin.doLogin(client, "wrongusername", "wrongpassword")
        }
    }

    @Category(AccessesTucan::class)
    @Test
    fun testLoginCorrectUsernameAndPassword() {
        runBlocking {
            LoginSingleton.getCredentials()
        }
    }
}