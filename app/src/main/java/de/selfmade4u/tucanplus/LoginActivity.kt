package de.selfmade4u.tucanplus

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import de.selfmade4u.tucanplus.OpenIdHelper.exchangeToken
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues


// https://developer.android.com/jetpack/androidx/releases/compose-material3
// https://developer.android.com/develop/ui/compose/designsystems/material3

class NavBackStackPreviewParameterProvider : PreviewParameterProvider<NavBackStack<NavKey>> {
    override val values: Sequence<NavBackStack<NavKey>> = sequenceOf(NavBackStack())
}

@Composable
@Preview
fun LoginForm(@PreviewParameter(NavBackStackPreviewParameterProvider::class) backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
        // val authService = AuthorizationService(context)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
        Log.e(TAG, "login form intent ${intent.toString()}")
        /*val resp = AuthorizationResponse.fromIntent(intent.data!!)
        val ex = AuthorizationException.fromIntent(intent.data!!)
        if (resp != null) {
            Log.i(TAG, "Authorization success")
            // authorization completed
            coroutineScope.launch {
                val token = authService.exchangeToken(resp)
                Log.i(TAG, "Token ${token}")
            }
        } else {
            // authorization failed, check ex for more details
            Log.e(TAG, "failed to authorize", ex)
        }*/
    }
    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //ShowLocalServices()
            //WifiDirect()
            //WifiDirectBonjour()
            Button(onClick = {
                // https://dsf.tucan.tu-darmstadt.de/IdentityServer/.well-known/openid-configuration
                val serviceConfig =
                    AuthorizationServiceConfiguration(
                        "https://dsf.tucan.tu-darmstadt.de/IdentityServer/connect/authorize".toUri(),  // authorization endpoint
                        "https://dsf.tucan.tu-darmstadt.de/IdentityServer/connect/token".toUri()
                    ) // token endpoint
                val authRequest =
                    AuthorizationRequest.Builder(
                        serviceConfig,  // the authorization service configuration
                        "MobileApp",  // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE,  // the response_type value: we want a code
                        "de.datenlotsen.campusnet.tuda:/oauth2redirect".toUri() // maybe without the path or other path and it still works?
                    ) // the redirect URI to which the auth response is sent
                        .setScope("openid DSF profile offline_access")
                        .build()
                //val authIntent = authService.getAuthorizationRequestIntent(authRequest)
                //launcher.launch(authIntent)
               /* val url = "https://dsf.tucan.tu-darmstadt.de/IdentityServer/connect/authorize?client_id=MobileApp&scope=openid+DSF+profile+offline_access&response_mode=query&response_type=code&ui_locales=de&redirect_uri=de.datenlotsen.campusnet.tuda:/oauth2redirect"
                val intent = CustomTabsIntent.Builder()
                    .build()
                intent.launchUrl(context, url.toUri())*/
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
        }
    }
}
