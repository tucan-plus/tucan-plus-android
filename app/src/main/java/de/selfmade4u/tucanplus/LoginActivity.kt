package de.selfmade4u.tucanplus

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
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
import net.openid.appauth.AuthorizationRequest
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
    // https://developer.android.com/develop/ui/compose/libraries#requesting-runtime-permissions
    /*val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { value ->
            Toast.makeText(context, "Permission response $value", Toast.LENGTH_SHORT).show()
        }
    LaunchedEffect(true) {
        launcher.launch(arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES))
    }*/
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
                val authService = AuthorizationService(context)
                val authIntent = authService.getAuthorizationRequestIntent(authRequest)
                launcher.launch(authIntent)
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
