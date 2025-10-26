package de.selfmade4u.tucanplus

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.selfmade4u.tucanplus.connector.TucanLogin
import de.selfmade4u.tucanplus.localfirst.WifiDirect
import de.selfmade4u.tucanplus.localfirst.WifiDirectBonjour
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch

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
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { value ->
        Toast.makeText(context, "Permission response $value", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(true) {
        launcher.launch(arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES))
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
            WifiDirect()
            //WifiDirectBonjour()
            //ShowLocalServices()
            TextField(
                state = usernameState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") })
            SecureTextField(
                state = passwordState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") })
            Button(onClick = {
                loading = true
                coroutineScope.launch {
                    // https://ktor.io/docs/client-create-new-application.html
                    // https://ktor.io/docs/client-requests.html#body
                    val client = HttpClient()
                    val response = TucanLogin.doLogin(
                        client,
                        usernameState.text.toString(),
                        passwordState.text.toString(),
                        context,
                    )
                    when (response) {
                        is TucanLogin.LoginResponse.InvalidCredentials -> launch {
                            snackbarHostState.showSnackbar(
                                "Falscher Nutzername oder Passwort"
                            )
                        }
                        is TucanLogin.LoginResponse.Success -> {
                            context.credentialSettingsDataStore.updateData { currentSettings ->
                                OptionalCredentialSettings(
                                    CredentialSettings(
                                        encryptedUserName = CipherManager.encrypt(
                                            usernameState.text.toString()
                                        ),
                                        encryptedPassword = CipherManager.encrypt(passwordState.text.toString()),
                                        encryptedSessionId = CipherManager.encrypt(response.sessionId),
                                        encryptedSessionCookie = CipherManager.encrypt(response.sessionSecret)
                                    )
                                )
                            }
                            backStack[backStack.size - 1] = MainNavKey
                        }

                        is TucanLogin.LoginResponse.TooManyAttempts -> launch {
                            snackbarHostState.showSnackbar(
                                "Zu viele Anmeldeversuche"
                            )
                        }
                    }
                    loading = false
                }
            }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
        }
    }
}
