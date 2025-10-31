package de.selfmade4u.tucanplus.connector

import android.content.Context
import android.util.Log
import com.fleeksoft.ksoup.nodes.TextNode
import de.selfmade4u.tucanplus.Root
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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters

object TucanLogin {

    sealed class LoginResponse {
        data class Success(val sessionId: String, val sessionSecret: String) : LoginResponse()
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
                Log.e("TucanLogin", "ConnectTimeoutException", e)
                // TODO FIXME generalize for all requests (very important)
                if (i == 5) {
                    throw e
                }
            }
            i += 1
        }
    }

    suspend fun doLogin(
        client: HttpClient,
        username: String,
        password: String,
        context: Context? = null
    ): LoginResponse {
        return response(context, doFetch(client, username, password)) {
            status(HttpStatusCode.OK)
            header(
                "Content-Security-Policy",
                "default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval';"
            )
            header("Content-Type", "text/html")
            header("X-Content-Type-Options", "nosniff")
            header("X-XSS-Protection", "1; mode=block")
            header("Referrer-Policy", "strict-origin")
            header("X-Frame-Options", "SAMEORIGIN")
            maybeHeader("X-Powered-By", listOf("ASP.NET"))
            header("Server", "Microsoft-IIS/10.0")
            header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            ignoreHeader("MgMiddlewareWaitTime") // 0 or 16
            ignoreHeader("Date")
            ignoreHeader("Content-Length")
            if (hasHeader("Set-cookie")) {
                val cookie = extractHeader("Set-cookie")[0].removePrefix("cnsc =")
                val sessionId = extractHeader("REFRESH")[0]
                val sessionIdMatch =
                    Regex("""0; URL=/scripts/mgrqispi\.dll\?APPNAME=CampusNet&PRGNAME=STARTPAGE_DISPATCH&ARGUMENTS=-N(\d+),-N000019,-N000000000000000""").matchEntire(
                        sessionId
                    )!!
                root {
                    parseLoginSuccess()
                }
                LoginResponse.Success(sessionIdMatch.groupValues[1], cookie)
            } else {
                root {
                    parseLoginFailure()
                }
            }
        }
    }

    suspend fun Root.parseLoginFailure(): LoginResponse {
        return parseBase("000000000000001", "000000", {}) { pageType ->
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

    suspend fun Root.parseLoginSuccess() {
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