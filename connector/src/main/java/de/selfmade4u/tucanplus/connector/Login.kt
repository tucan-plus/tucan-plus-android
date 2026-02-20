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
        data class Success(
            val sessionId: String,
            val sessionCookie: String,
            val menuLocalizer: Localizer
        ) : LoginResponse()

        data object InvalidCredentials : LoginResponse()
        data object TooManyAttempts : LoginResponse()
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