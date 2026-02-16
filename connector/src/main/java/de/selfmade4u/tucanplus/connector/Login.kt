package de.selfmade4u.tucanplus.connector

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Entities
import com.fleeksoft.ksoup.nodes.TextNode
import de.selfmade4u.tucanplus.EnglishLocalizer
import de.selfmade4u.tucanplus.GermanLocalizer
import de.selfmade4u.tucanplus.Localizer
import de.selfmade4u.tucanplus.Root
import de.selfmade4u.tucanplus.TAG
import de.selfmade4u.tucanplus.body
import de.selfmade4u.tucanplus.connector.Common.parseBase
import de.selfmade4u.tucanplus.h1
import de.selfmade4u.tucanplus.head
import de.selfmade4u.tucanplus.header
import de.selfmade4u.tucanplus.html
import de.selfmade4u.tucanplus.p
import de.selfmade4u.tucanplus.peek
import de.selfmade4u.tucanplus.response
import de.selfmade4u.tucanplus.script
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.logging.Level
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object TucanLogin {

    sealed class LoginResponse {
        data class Success(val sessionId: String, val sessionCookie: String, val menuLocalizer: Localizer) : LoginResponse()
        data object InvalidCredentials : LoginResponse()
        data object TooManyAttempts : LoginResponse()
    }

    private suspend fun doFetch(
        client: HttpClient,
        username: String,
        password: String
    ): HttpResponse {
        var i = 0
        while (true) {
            try {
                val r = client.submitForm(
                    "https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll",
                    formParameters = parameters {
                        append("usrname", username)
                        append("pass", password)
                        append("APPNAME", "CampusNet")
                        append("PRGNAME", "LOGINCHECK")
                        append("ARGUMENTS", "clino,usrname,pass,menuno,menu_type,browser,platform")
                        append("clino", "000000000000001")
                        append("menuno", "000344")
                        append("menu_type", "classic")
                        append("browser", "")
                        append("platform", "")
                    })
                return r
            } catch (e: ConnectTimeoutException) {
                // TODO FIXME generalize for all requests (very important)
                if (i == 5) {
                    throw e
                }
            } catch (e: Throwable) {
                java.util.logging.Logger.getLogger(TAG).log(Level.WARNING, "Request failed", e)
                throw e
            }
            i += 1
        }
    }

    suspend fun doLogin(
        client: HttpClient,
        username: String,
        password: String,
    ): LoginResponse {
        return response(doFetch(client, username, password)) {
            status(HttpStatusCode.OK)
            header(
                "Content-Security-Policy",
                "frame-src https://dsf.tucan.tu-darmstadt.de; frame-ancestors 'self' https://dsf.tucan.tu-darmstadt.de;"
            )
            header("Content-Type", "text/html")
            header("X-Content-Type-Options", "nosniff")
            header("X-XSS-Protection", "1; mode=block")
            header("Referrer-Policy", "strict-origin")
            header("X-Frame-Options", "SAMEORIGIN")
            maybeHeader("X-Powered-By", listOf("ASP.NET"))
            header("Server", "Microsoft-IIS/10.0")
            header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            ignoreHeader("mgxpamiddlewarewaittime") // 0 or 16
            ignoreHeader("Date")
            maybeIgnoreHeader("dl-served-by") // Srv3
            maybeIgnoreHeader("vary")
            maybeIgnoreHeader("x-android-received-millis")
            maybeIgnoreHeader("x-android-response-source")
            maybeIgnoreHeader("x-android-selected-protocol")
            maybeIgnoreHeader("x-android-sent-millis")
            maybeIgnoreHeader("content-length")
            if (hasHeader("Set-cookie")) {
                val cookie = extractHeader("Set-cookie")[0].removePrefix("cnsc =")
                val refreshHeader = extractHeader("REFRESH")[0]
                val a = Regex("""0; URL=/scripts/mgrqispi\.dll\?APPNAME=CampusNet&PRGNAME=STARTPAGE_DISPATCH&ARGUMENTS=-N(\d+),-N000(019|350),-N000000000000000""").matchEntire(
                    refreshHeader
                )
                val sessionIdMatch = a ?: throw NullPointerException(refreshHeader)
                root {
                    parseLoginSuccess()
                }
                LoginResponse.Success(sessionIdMatch.groupValues[1], cookie, when (sessionIdMatch.groupValues[2]) {
                    "019" -> GermanLocalizer
                    "350" -> EnglishLocalizer
                    else -> throw IllegalStateException("Unknown language ${sessionIdMatch.groupValues[2]}")
                })
            } else {
                root {
                    parseLoginFailure()
                }
            }
        }
    }

    suspend fun doNewLogin(client: HttpClient,
                           username: String,
                           password: String) {
        var url = "https://dsf.tucan.tu-darmstadt.de/IdentityServer/external/saml/login/dfnshib?ReturnUrl=%2FIdentityServer%2Fconnect%2Fauthorize%2Fcallback%3Fclient_id%3DClassicWeb%26scope%3Dopenid%2520DSF%2520email%26response_mode%3Dquery%26response_type%3Dcode%26ui_locales%3Dde%26redirect_uri%3Dhttps%253A%252F%252Fwww.tucan.tu-darmstadt.de%252Fscripts%252Fmgrqispi.dll%253FAPPNAME%253DCampusNet%2526PRGNAME%253DLOGINCHECK%2526ARGUMENTS%253D-N000000000000001%2Cids_mode%2526ids_mode%253DY"
        println(url)
        var response = client.get(url)
        println(response)
        println(response.headers)
        var responseText = response.bodyAsText()
        println(responseText)
        url = response.headers["Location"]!!
        println(url)
        response = client.get(url)
        println(response)
        println(response.headers)
        responseText = response.bodyAsText()
        println(responseText)
        url = "https://login.tu-darmstadt.de" + response.headers["Location"]!!
        println(url)
        response = client.get(url)
        println(response)
        responseText = response.bodyAsText()
        println(responseText)
        var regex = """<input type="hidden" name="csrf_token" value="(?<csrfToken>_[a-f0-9]+)" />""".toRegex()
        var matchResult = regex.find(responseText)!!
        var csrfToken = matchResult.groups["csrfToken"]?.value!!
        println(csrfToken)
        url = response.request.url.toString()
        println(url)
        response = client.submitForm(url, formParameters = parameters {
            append("csrf_token", csrfToken)
            append("j_username", username)
            append("j_password", password)
            append("_eventId_proceed", "")
        })
        println(response)
        responseText = response.bodyAsText()
        println(responseText)
        url = "https://login.tu-darmstadt.de" + response.headers["Location"]!!
        println(url)
        response = client.get(url)
        println(response)
        responseText = response.bodyAsText()
        println(responseText)
        regex = """<input type="hidden" name="csrf_token" value="(?<csrfToken>_[a-f0-9]+)" />""".toRegex()
        matchResult = regex.find(responseText)!!
        csrfToken = matchResult.groups["csrfToken"]?.value!!
        println(csrfToken)
        url = response.request.url.toString()
        println(url)
        response = client.submitForm(url, formParameters = parameters {
            append("csrf_token", csrfToken)
            append("fudis_selected_token_ids_input", "TOTP21665900")
            append("_eventId_proceed", "")
        })
        println(response)
        responseText = response.bodyAsText()
        println(responseText)
        url = "https://login.tu-darmstadt.de" + response.headers["Location"]!!
        println(url)
        response = client.get(url)
        println(response)
        responseText = response.bodyAsText()
        println(responseText)
        regex = """<input type="hidden" name="csrf_token" value="(?<csrfToken>_[a-f0-9]+)" />""".toRegex()
        matchResult = regex.find(responseText)!!
        csrfToken = matchResult.groups["csrfToken"]?.value!!
        println(csrfToken)
        url = response.request.url.toString()
        println(url)
        response = client.submitForm(url, formParameters = parameters {
            append("csrf_token", csrfToken)
            append("fudis_otp_input", System.getenv("TUCAN_TOTP")!!)
            append("_eventId_proceed", "")
        })
        println(response)
        responseText = response.bodyAsText()
        println(responseText)
        regex = """<input type="hidden" name="RelayState" value="(?<RelayState>[^"]+)"/>""".toRegex()
        matchResult = regex.find(responseText)!!
        var RelayState = matchResult.groups["RelayState"]?.value!!
        println(RelayState)
        RelayState = Entities.unescape(RelayState)
        println(RelayState)
        regex = """<input type="hidden" name="SAMLResponse" value="(?<SAMLResponse>[^"]+)"/>""".toRegex()
        matchResult = regex.find(responseText)!!
        var SAMLResponse = matchResult.groups["SAMLResponse"]?.value!!
        println(SAMLResponse)
        SAMLResponse = Entities.unescape(SAMLResponse)
        println(SAMLResponse)
        url = "https://dsf.tucan.tu-darmstadt.de/IdentityServer/external/saml/acs/dfnshib"
        println(url)
        response = client.submitForm(url, formParameters = parameters {
            append("RelayState", RelayState)
            append("SAMLResponse", SAMLResponse)
        })
        println(response)
        responseText = response.bodyAsText()
        println(responseText)
        println(response.headers)
        url = "https://dsf.tucan.tu-darmstadt.de" + response.headers["Location"]!!
        println(url)
        response = client.get(url)
        println(response)
        println(response.headers)
        println(response.headers["Location"])
        responseText = response.bodyAsText()
        println(responseText)
        url = response.headers["Location"]!!
        println(url)
        response = client.get(url)
        println(response)
        println(response.headers)
        println(response.headers["Location"])
        responseText = response.bodyAsText()
        println(responseText)
        val id = Regex("""0; URL=/scripts/mgrqispi\.dll\?APPNAME=CampusNet&PRGNAME=STARTPAGE_DISPATCH&ARGUMENTS=-N(?<id>\d+),-N000(019|350),-N000000000000000""").matchEntire(
            response.headers["refresh"]!!
        )!!.groups["id"]?.value!!
        println(id)
        url = "https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MLSSTART&ARGUMENTS=-N${id}%2C-N000019%2C"
        while (true) {
            val current = LocalDateTime.now()
            println(current)
            response = client.get(url)
            println(response)
            println(response.headers)
            println(response.headers["Location"])
            responseText = response.bodyAsText()
            println(responseText)
            assert(responseText.contains("Eingegangene Nachrichten:"))
            delay(10.minutes)
        }
    }

    fun Root.parseLoginFailure(): LoginResponse {
        return parseBase("000000000000001", GermanLocalizer, "000000", {}) { localizer, pageType ->
            check(pageType == "accessdenied")
            script { attribute("type", "text/javascript"); }
            val child = peek()?.firstChild()
            if (child is TextNode && child.text()
                    .trim() == "Sie konnten nicht angemeldet werden"
            ) {
                h1 { text("Sie konnten nicht angemeldet werden") }
                p { text("Bitte versuchen Sie es erneut. Überprüfen Sie ggf. Ihre Zugangsdaten.") }
                LoginResponse.InvalidCredentials
            } else {
                h1 { text("Anmeldung zur Zeit nicht möglich") }
                p { text("Aufgrund einer Häufung fehlgeschlagener Einloggversuche ist dieses Benutzerkonto vorübergehend gesperrt.") }
                LoginResponse.TooManyAttempts
            }
        }
    }

    fun Root.parseLoginSuccess() {
        html {
            head {
            }
            body {
                header {
                }
            }
        }
    }
}