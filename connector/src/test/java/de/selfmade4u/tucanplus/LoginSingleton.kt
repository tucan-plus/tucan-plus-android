package de.selfmade4u.tucanplus

import de.selfmade4u.tucanplus.connector.TucanLogin
import io.ktor.client.HttpClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object LoginSingleton {
    @Volatile
    private var Instance: Result<CredentialSettings>? = null
    val mutex = Mutex()

    suspend fun getCredentials(): Result<CredentialSettings> {
        return Instance ?: mutex.withLock {
            return Instance ?: run {
                val client = HttpClient()
                val response = TucanLogin.doLogin(
                    client,
                    System.getenv("TUCAN_USERNAME")!!,
                    System.getenv("TUCAN_PASSWORD")!!
                )
                Instance = when (response) {
                    TucanLogin.LoginResponse.InvalidCredentials -> Result.failure(RuntimeException("the stored credentials were invalid"))
                    is TucanLogin.LoginResponse.Success -> Result.success(CredentialSettings(System.getenv("TUCAN_USERNAME")!!, System.getenv("TUCAN_PASSWORD")!!, response.sessionId, response.sessionCookie, response.menuLocalizer, lastRequestTime = System.currentTimeMillis()))
                    TucanLogin.LoginResponse.TooManyAttempts -> Result.failure(RuntimeException("the stored credentials had too many login attempts"))
                }
                Instance!!
            }
        }
    }
}