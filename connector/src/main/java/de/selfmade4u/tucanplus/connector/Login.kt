package de.selfmade4u.tucanplus.connector

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
import java.util.logging.Level

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
        // TODO late change to not follow redirects for higher efficiency
        // TODO later manually handle cookies
        var response = client.get("https://dsf.tucan.tu-darmstadt.de/IdentityServer/external/saml/login/dfnshib?ReturnUrl=/IdentityServer/connect/authorize/callback?client_id=ClassicWeb&scope=openid%20DSF%20email&response_mode=query&response_type=code&ui_locales=de&redirect_uri=https%3A%2F%2Fwww.tucan.tu-darmstadt.de%2Fscripts%2Fmgrqispi.dll%3FAPPNAME%3DCampusNet%26PRGNAME%3DLOGINCHECK%26ARGUMENTS%3D-N000000000000001,ids_mode%26ids_mode%3DY")
        println(response)
        val responseText = response.bodyAsText()
        val regex = """<input type="hidden" name="csrf_token" value="(?<csrfToken>_[a-f0-9]+)" />""".toRegex()
        val matchResult = regex.find(responseText)!!
        val csrfToken = matchResult.groups["csrfToken"]?.value!!
        println(csrfToken)
        response = client.submitForm(response.request.url.toString(), formParameters = parameters {
            append("csrf_token", csrfToken)
            append("j_username", username)
            append("j_password", password)
            append("_eventId_proceed", "")
        })
        println(response)
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