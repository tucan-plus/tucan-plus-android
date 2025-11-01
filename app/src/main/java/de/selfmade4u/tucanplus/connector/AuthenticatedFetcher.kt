package de.selfmade4u.tucanplus.connector

import android.content.Context
import android.util.Log
import android.widget.Toast
import de.selfmade4u.tucanplus.CipherManager
import de.selfmade4u.tucanplus.CredentialSettings
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.TAG
import de.selfmade4u.tucanplus.credentialSettingsDataStore
import io.ktor.client.HttpClient
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthenticatedResponse<T> {
    data class Success<T>(var response: T) :
        AuthenticatedResponse<T>()

    class SessionTimeout<T>() : AuthenticatedResponse<T>()
    class NetworkLikelyTooSlow<T>() : AuthenticatedResponse<T>()

    fun <O> map(): AuthenticatedResponse<O> {
        return when (this) {
            is NetworkLikelyTooSlow<*> -> NetworkLikelyTooSlow()
            is SessionTimeout<*> -> SessionTimeout()
            is Success<*> -> TODO()
        }
    }
}

suspend fun fetchAuthenticated(sessionCookie: String, url: String): AuthenticatedResponse<HttpResponse> {
    val client = HttpClient()
    val r = try {
        client.get(url) {
            cookie("cnsc", sessionCookie)
        }
    } catch (e: IllegalStateException) {
        if (e.message?.contains("Content-Length mismatch") ?: true) {
            return AuthenticatedResponse.NetworkLikelyTooSlow()
        }
        Log.e(TAG, "Failed to fetch request", e)
        return AuthenticatedResponse.SessionTimeout()
    }
    return AuthenticatedResponse.Success(r)
}

suspend fun <T> fetchAuthenticatedWithReauthentication(context: Context, sessionCookie: String, url: String, parser: suspend (HttpResponse) -> AuthenticatedResponse<T>): AuthenticatedResponse<T> {
    val client = HttpClient()
    val settings = context.credentialSettingsDataStore.data.first().inner!!
    if (System.currentTimeMillis() < settings.lastRequestTime + 30*60*1000) {
        Log.d(TAG, "No predictive session timeout, trying to fetch")
        val response = fetchAuthenticated(
            sessionCookie, url
        )
        when (response) {
            is AuthenticatedResponse.NetworkLikelyTooSlow<*> -> return AuthenticatedResponse.NetworkLikelyTooSlow()
            is AuthenticatedResponse.SessionTimeout<*> -> { }
            is AuthenticatedResponse.Success<HttpResponse> -> return parser(response.response)
        }
    } else {
        Log.d(TAG, "Predictive session timeout, directly reauthenticating")
    }
    Toast.makeText(context, "Reauthenticating", Toast.LENGTH_SHORT).show()
    val loginResponse = TucanLogin.doLogin(
        client,
        settings.username,
        settings.password,
        context,
    )
    when (loginResponse) {
        is TucanLogin.LoginResponse.InvalidCredentials -> launch {
            Toast.makeText(context, "Ungültiger Username oder Password", Toast.LENGTH_LONG)
                .show()
            // backStack[backStack.size - 1] = MainNavKey
            // TODO clear store
        }
        is TucanLogin.LoginResponse.Success -> {
            context.credentialSettingsDataStore.updateData { currentSettings ->
                OptionalCredentialSettings(
                    CredentialSettings(
                        username = settings.username,
                        password = settings.password,
                        sessionId = loginResponse.sessionId,
                        sessionCookie = loginResponse.sessionSecret,
                    )
                )
            }
            val response = fetchAuthenticated(
                sessionCookie, url
            )
            return parser(response)
        }

        is TucanLogin.LoginResponse.TooManyAttempts -> launch {
            // bad
            Toast.makeText(context, "Zu viele Anmeldeversuche", Toast.LENGTH_LONG).show()
        }
    }
}