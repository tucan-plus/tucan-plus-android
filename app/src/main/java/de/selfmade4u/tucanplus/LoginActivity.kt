package de.selfmade4u.tucanplus

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.core.net.toUri


// https://developer.android.com/jetpack/androidx/releases/compose-material3
// https://developer.android.com/develop/ui/compose/designsystems/material3

class NavBackStackPreviewParameterProvider : PreviewParameterProvider<NavBackStack<NavKey>> {
    override val values: Sequence<NavBackStack<NavKey>> = sequenceOf(NavBackStack())
}

@Composable
@Preview
fun LoginForm(@PreviewParameter(NavBackStackPreviewParameterProvider::class) backStack: NavBackStack<NavKey>) {
    val usernameState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
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
                val url = "https://dsf.tucan.tu-darmstadt.de/IdentityServer/External/Challenge?provider=dfnshib&returnUrl=%2FIdentityServer%2Fconnect%2Fauthorize%2Fcallback%3Fclient_id%3DClassicWeb%26scope%3Dopenid%2520DSF%2520email%26response_mode%3Dquery%26response_type%3Dcode%26ui_locales%3Dde%26redirect_uri%3Dhttps%253A%252F%252Fwww.tucan.tu-darmstadt.de%252Fscripts%252Fmgrqispi.dll%253FAPPNAME%253DCampusNet%2526PRGNAME%253DLOGINCHECK%2526ARGUMENTS%253D-N000000000000001,ids_mode%2526ids_mode%253DY"
                val intent = CustomTabsIntent.Builder()
                    .build()
                intent.launchUrl(context, url.toUri())
            }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
        }
    }
}
