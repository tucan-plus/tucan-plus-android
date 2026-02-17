package de.selfmade4u.tucanplus

import android.content.Context
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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import de.selfmade4u.tucanplus.connector.PersistentCookiesStorage
import de.selfmade4u.tucanplus.connector.TucanLogin
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes


// https://developer.android.com/jetpack/androidx/releases/compose-material3
// https://developer.android.com/develop/ui/compose/designsystems/material3

class NavBackStackPreviewParameterProvider : PreviewParameterProvider<NavBackStack<NavKey>> {
    override val values: Sequence<NavBackStack<NavKey>> = sequenceOf(NavBackStack())
}

class KeepTucanSessionAliveWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        withContext(Dispatchers.IO) {
            val appSpecificExternalFile = File(context.getExternalFilesDir(null), "tucan-session.log")
            FileOutputStream(appSpecificExternalFile, true).bufferedWriter().use {
                it.appendLine("${LocalDateTime.now()} KeepTucanSessionAliveWorker is starting....")

                val client = HttpClient(Android) {
                    followRedirects = false
                    install(HttpCookies) {
                        storage = PersistentCookiesStorage(File(context.getExternalFilesDir(null),"tucan-cookies.log"))
                    }
                }

                val tucanId: String = inputData.getString("tucanId")!!
                var url =
                    "https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MLSSTART&ARGUMENTS=-N${tucanId}%2C-N000019%2C"
                val current = LocalDateTime.now()
                it.appendLine(current.toString())
                var response = client.get(url)
                it.appendLine(response.toString())
                it.appendLine(response.headers.toString())
                var responseText = response.bodyAsText()
                it.appendLine(responseText)
                assert(responseText.contains("Eingegangene Nachrichten:"))

                it.appendLine("${LocalDateTime.now()} KeepTucanSessionAliveWorker is stopping....")
            }
            Result.success()
        }
    }
}

@Composable
@Preview
fun LoginForm(@PreviewParameter(NavBackStackPreviewParameterProvider::class) backStack: NavBackStack<NavKey>) {
    val usernameState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    val totpState = rememberTextFieldState()
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
            TextField(
                state = usernameState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") })
            SecureTextField(
                state = passwordState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") })
            SecureTextField(
                state = totpState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("TOTP") })
            Button(onClick = {
                loading = true
                /*val url = "https://dsf.tucan.tu-darmstadt.de/IdentityServer/External/Challenge?provider=dfnshib&returnUrl=%2FIdentityServer%2Fconnect%2Fauthorize%2Fcallback%3Fclient_id%3DClassicWeb%26scope%3Dopenid%2520DSF%2520email%26response_mode%3Dquery%26response_type%3Dcode%26ui_locales%3Dde%26redirect_uri%3Dhttps%253A%252F%252Fwww.tucan.tu-darmstadt.de%252Fscripts%252Fmgrqispi.dll%253FAPPNAME%253DCampusNet%2526PRGNAME%253DLOGINCHECK%2526ARGUMENTS%253D-N000000000000001,ids_mode%2526ids_mode%253DY"
                val intent = CustomTabsIntent.Builder()
                    .build()
                intent.launchUrl(context, url.toUri())*/
                coroutineScope.launch {
                    val client = HttpClient(Android) {
                        followRedirects = false
                        install(HttpCookies) {
                            storage = PersistentCookiesStorage(File(context.getExternalFilesDir(null), "tucan-cookies.log"))
                        }
                    }
                    val tucanId = TucanLogin.doNewLogin(
                        client,
                        usernameState.text.toString(),
                        passwordState.text.toString(),
                        totpState.text.toString()
                    )

                    val keepTucanSessionAlive =
                        PeriodicWorkRequestBuilder<KeepTucanSessionAliveWorker>(15, TimeUnit.MINUTES)
                            .setInputData(workDataOf(
                                "tucanId" to tucanId
                            ))
                            .setConstraints(
                                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                            )
                            .build()

                    WorkManager
                        .getInstance(context)
                        .enqueueUniquePeriodicWork(
                            "keepTucanSessionAlive",
                            ExistingPeriodicWorkPolicy.KEEP,
                            keepTucanSessionAlive
                        )
                    snackbarHostState.showSnackbar(
                        "Scheduled for id $tucanId"
                    )
                    loading = false
                }
            }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
        }
    }
}
