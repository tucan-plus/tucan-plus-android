package de.selfmade4u.tucanplus

import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.AuthorizationServiceConfiguration.RetrieveConfigurationCallback
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


public object OpenIdHelper {

    suspend fun fetchServiceConfiguration() = suspendCancellableCoroutine { cont ->
        AuthorizationServiceConfiguration.fetchFromIssuer(
            "https://dsf.tucan.tu-darmstadt.de/IdentityServer/".toUri(),
            RetrieveConfigurationCallback { serviceConfiguration, ex ->
                if (ex != null) {
                    Log.e(TAG, "failed to fetch configuration")
                    cont.resumeWithException(ex)
                    return@RetrieveConfigurationCallback
                }
                cont.resume(serviceConfiguration)
            })
    }

   public suspend fun AuthorizationService.exchangeToken(response: AuthorizationResponse) = suspendCancellableCoroutine { cont ->
        performTokenRequest(
            response.createTokenExchangeRequest()
        ) { resp, ex ->
            if (resp != null) {
                // exchange succeeded
                cont.resume(resp)
            } else {
                // authorization failed, check ex for more details
                cont.resumeWithException(ex!!)
            }
        }
    }
}