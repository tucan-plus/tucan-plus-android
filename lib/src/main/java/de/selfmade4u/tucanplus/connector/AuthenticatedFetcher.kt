package de.selfmade4u.tucanplus.connector

import androidx.datastore.core.DataStore
import de.selfmade4u.tucanplus.CredentialSettings
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.TAG
import de.selfmade4u.tucanplus.connector.AuthenticatedHttpResponse.NetworkLikelyTooSlow
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse.InvalidCredentials
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse.SessionTimeout
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse.Success
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse.TooManyAttempts
import io.ktor.client.HttpClient
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.first
import java.net.SocketException
import java.util.logging.Level

sealed class AuthenticatedHttpResponse<T> {
    data class Success<T>(var response: T) :
        AuthenticatedHttpResponse<T>()
    class NetworkLikelyTooSlow<T>() : AuthenticatedHttpResponse<T>()

    fun <O> map(): AuthenticatedResponse<O> {
        return when (this) {
            is Success<*> -> throw UnsupportedOperationException()
            is NetworkLikelyTooSlow<*> -> AuthenticatedResponse.NetworkLikelyTooSlow()
        }
    }
}

sealed class ParserResponse<T> {
    data class Success<T>(var response: T) :
        ParserResponse<T>()
    class SessionTimeout<T>() : ParserResponse<T>()

    fun <O> map(): AuthenticatedResponse<O> {
        return when (this) {
            is Success<*> -> throw UnsupportedOperationException()
            is SessionTimeout<*> -> AuthenticatedResponse.SessionTimeout()
        }
    }
}

sealed class AuthenticatedResponse<T> {
    data class Success<T>(var response: T) :
        AuthenticatedResponse<T>()

    class SessionTimeout<T>() : AuthenticatedResponse<T>()

    class InvalidCredentials<T>() : AuthenticatedResponse<T>()

    class TooManyAttempts<T>() : AuthenticatedResponse<T>()
    class NetworkLikelyTooSlow<T>() : AuthenticatedResponse<T>()

    fun <O> map(): AuthenticatedResponse<O> {
        return when (this) {
            is Success<*> -> throw UnsupportedOperationException()
            is SessionTimeout<*> -> SessionTimeout()
            is InvalidCredentials<*> -> InvalidCredentials()
            is TooManyAttempts<*> -> TooManyAttempts()
            is NetworkLikelyTooSlow<*> -> NetworkLikelyTooSlow()
        }
    }
}

suspend fun fetchAuthenticated(sessionCookie: String, url: String): AuthenticatedHttpResponse<HttpResponse> {
    val client = HttpClient()
    val r = try {
        client.get(url) {
            cookie("cnsc", sessionCookie)
        }
    } catch (e: IllegalStateException) {
        if (e.message?.contains("Content-Length mismatch") ?: true) {
            return NetworkLikelyTooSlow()
        }
        return NetworkLikelyTooSlow()
    } catch (e: SocketException) {
        java.util.logging.Logger.getLogger(TAG).log(Level.WARNING, "Request failed", e)
        return AuthenticatedHttpResponse.NetworkLikelyTooSlow()
    }
    return AuthenticatedHttpResponse.Success(r)
}

suspend fun <T> fetchAuthenticatedWithReauthentication(credentialSettingsDataStore: DataStore<OptionalCredentialSettings>, url: (sessionId: String) -> String, parser: suspend (sessionId: String, response: HttpResponse) -> ParserResponse<T>): AuthenticatedResponse<T> {
    val client = HttpClient()
    var settings = credentialSettingsDataStore.data.first().inner!!
    if (System.currentTimeMillis() < settings.lastRequestTime + 30*60*1000) {
        val response = fetchAuthenticated(
            settings.sessionCookie, url(settings.sessionId)
        )
        when (response) {
            is AuthenticatedHttpResponse.Success<HttpResponse> -> {
                when (val parserResponse = parser(settings.sessionId, response.response)) {
                    is ParserResponse.Success<T> -> {
                        credentialSettingsDataStore.updateData { currentSettings ->
                            OptionalCredentialSettings(settings.copy(lastRequestTime = System.currentTimeMillis()))
                        }
                        return Success<T>(parserResponse.response)
                    }
                    is ParserResponse.SessionTimeout<*> -> {
                        // fall through
                    }
                }
            }
            is NetworkLikelyTooSlow<*> -> AuthenticatedResponse.NetworkLikelyTooSlow<T>()
        }
    } else {
    }
    val loginResponse = TucanLogin.doLogin(
        client,
        settings.username,
        settings.password,
    )
    when (loginResponse) {
        is TucanLogin.LoginResponse.InvalidCredentials -> {
            // backStack[backStack.size - 1] = MainNavKey
            // TODO clear store
            return InvalidCredentials()
        }
        is TucanLogin.LoginResponse.Success -> {
            settings = CredentialSettings(
                username = settings.username,
                password = settings.password,
                sessionId = loginResponse.sessionId,
                sessionCookie = loginResponse.sessionCookie,
                lastRequestTime = System.currentTimeMillis(),
                menuLocalizer = loginResponse.menuLocalizer
            )
            credentialSettingsDataStore.updateData { currentSettings ->
                OptionalCredentialSettings(
                    settings
                )
            }
            val response = fetchAuthenticated(
                loginResponse.sessionCookie, url(loginResponse.sessionId)
            )
            return when (response) {
                is AuthenticatedHttpResponse.Success<HttpResponse> -> {
                    when (val parserResponse = parser( loginResponse.sessionId, response.response)) {
                        is ParserResponse.Success<T> -> {
                            credentialSettingsDataStore.updateData { currentSettings ->
                                OptionalCredentialSettings(settings.copy(lastRequestTime = System.currentTimeMillis()))
                            }
                            return Success<T>(parserResponse.response)
                        }
                        is ParserResponse.SessionTimeout<*> -> {
                            return SessionTimeout() // should be unreachable
                        }
                    }
                }
                is NetworkLikelyTooSlow<*> -> AuthenticatedResponse.NetworkLikelyTooSlow<T>()
            }
        }
        is TucanLogin.LoginResponse.TooManyAttempts -> {
            // bad
            return TooManyAttempts()
        }
    }
}