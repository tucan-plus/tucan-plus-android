package de.selfmade4u.tucanplus.destination

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import de.selfmade4u.tucanplus.CipherManager
import de.selfmade4u.tucanplus.CredentialSettings
import de.selfmade4u.tucanplus.DetailedDrawerExample
import de.selfmade4u.tucanplus.MyDatabase
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.connector.ModuleResults
import de.selfmade4u.tucanplus.connector.TucanLogin
import de.selfmade4u.tucanplus.credentialSettingsDataStore
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModuleResultsComposable(backStack: NavBackStack<NavKey>) {
    val modules = loadModules()
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    DetailedDrawerExample(backStack) { innerPadding ->
        PullToRefreshBox(isRefreshing, onRefresh = {
            isRefreshing = true
        }, state = state, indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.Companion.align(Alignment.Companion.TopCenter),
            )
        }, modifier = Modifier.Companion.padding(innerPadding)) {
            Column(Modifier.Companion.fillMaxSize().verticalScroll(rememberScrollState())) {
                LongBasicDropdownMenu()
                when (val values = modules.value) {
                    null -> {
                        Column(
                            Modifier.Companion.fillMaxSize(),
                            horizontalAlignment = Alignment.Companion.CenterHorizontally
                        ) { CircularWavyProgressIndicator() }
                    }

                    is ModuleResults.ModuleResultsResponse.SessionTimeout -> {
                        Text("Session timeout")
                    }

                    is ModuleResults.ModuleResultsResponse.Success -> {
                        values.modules.forEach { module ->
                            key(module.id) {
                                ModuleComposable(module)
                            }
                        }
                    }

                    ModuleResults.ModuleResultsResponse.NetworkLikelyTooSlow -> Text("Your network connection is likely too slow for TUCaN")
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 200)
@Composable
fun ModuleComposable(
    module: ModuleResults.Module = ModuleResults.Module(
        42,
        "id",
        "name",
        ModuleResults.ModuleGrade.G1_0,
        1,
        "url",
        "url"
    )
) {
    // https://developer.android.com/develop/ui/compose/layouts/basics
    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("${module.name}")
            Text("${module.id}", fontSize = 10.sp, color = Color.Companion.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${module.credits} CP")
            Text("Note ${module.grade.representation}")
        }
    }
}

@Composable
fun loadModules(): State<ModuleResults.ModuleResultsResponse?> {
    val context = LocalContext.current
    return produceState(initialValue = null) {
        val credentialSettings: CredentialSettings =
            context.credentialSettingsDataStore.data.first().inner!!
        val client = HttpClient()
        var response = ModuleResults.getModuleResults(
            context, client, CipherManager.decrypt(credentialSettings.encryptedSessionId),
            CipherManager.decrypt(credentialSettings.encryptedSessionCookie)
        )
        if (response is ModuleResults.ModuleResultsResponse.SessionTimeout) {
            Toast.makeText(context, "Reauthenticating", Toast.LENGTH_SHORT).show()
            val loginResponse = TucanLogin.doLogin(
                client,
                CipherManager.decrypt(credentialSettings.encryptedUserName),
                CipherManager.decrypt(credentialSettings.encryptedPassword),
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
                                encryptedUserName = CipherManager.encrypt(
                                    CipherManager.decrypt(credentialSettings.encryptedUserName)
                                ),
                                encryptedPassword = CipherManager.encrypt(
                                    CipherManager.decrypt(credentialSettings.encryptedPassword)
                                ),
                                encryptedSessionId = CipherManager.encrypt(loginResponse.sessionId),
                                encryptedSessionCookie = CipherManager.encrypt(loginResponse.sessionSecret)
                            )
                        )
                    }
                    response = ModuleResults.getModuleResults(
                        context, client, loginResponse.sessionId,
                        loginResponse.sessionSecret
                    )
                }

                is TucanLogin.LoginResponse.TooManyAttempts -> launch {
                    // bad
                    Toast.makeText(context, "Zu viele Anmeldeversuche", Toast.LENGTH_LONG).show()
                }
            }
        }
        value = response
        Log.e("LOADED", value.toString())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LongBasicDropdownMenu() {
    val context = LocalContext.current
    val semesters by produceState(listOf<ModuleResults.Semesterauswahl>()) {
        val db = MyDatabase.Companion.getDatabase(context)
        value = db.semestersDao().getAll()
    }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState("")
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Semester") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            semesters.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${option.semester.name} ${option.year}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd("${option.semester.name} ${option.year}")
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}