package de.selfmade4u.tucanplus

import de.selfmade4u.tucanplus.connector.TucanLogin
import io.ktor.client.HttpClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object LoginSingleton {
    @Volatile
    private var Instance: CredentialSettings? = null
    val mutex = Mutex()

    suspend fun getCredentials(): CredentialSettings {
        return Instance ?: mutex.withLock {
            return Instance ?: run {
                val client = HttpClient()
                val response = TucanLogin.doLogin(
                    client,
                    System.getenv("TUCAN_USERNAME")!!,
                    System.getenv("TUCAN_PASSWORD")!!
                )
                val result = when (response) {
                    TucanLogin.LoginResponse.InvalidCredentials -> TODO()
                    is TucanLogin.LoginResponse.Success -> CredentialSettings(System.getenv("TUCAN_USERNAME")!!, System.getenv("TUCAN_PASSWORD")!!, response.sessionId, response.sessionCookie, response.menuLocalizer, lastRequestTime = System.currentTimeMillis())
                    TucanLogin.LoginResponse.TooManyAttempts -> TODO()
                }
                Instance = result
                result
            }
        }
    }
}