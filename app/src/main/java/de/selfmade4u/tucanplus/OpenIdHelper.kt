package de.selfmade4u.tucanplus

import android.net.Uri
import android.util.Log
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationServiceConfiguration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import androidx.core.net.toUri

class OpenIdHelper {

    suspend fun fetchServiceConfiguration() = suspendCoroutine { cont ->
        AuthorizationServiceConfiguration.fetchFromIssuer(
            "https://dsf.tucan.tu-darmstadt.de/IdentityServer/".toUri(),
            object : AuthorizationServiceConfiguration.RetrieveConfigurationCallback {
                override fun onFetchConfigurationCompleted(
                    serviceConfiguration: AuthorizationServiceConfiguration?,
                    ex: AuthorizationException?
                ) {
                    if (ex != null) {
                        Log.e(TAG, "failed to fetch configuration")
                        cont.resumeWithException(ex)
                        return
                    }
                    cont.resume(serviceConfiguration)
                }
            })
    }
}