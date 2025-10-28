package de.selfmade4u.tucanplus

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import de.selfmade4u.tucanplus.connector.ModuleResults
import de.selfmade4u.tucanplus.connector.TucanLogin
import de.selfmade4u.tucanplus.ui.theme.TUCaNPlusTheme
import io.ktor.client.HttpClient
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// main or login
// https://proandroiddev.com/mastering-navigation-in-jetpack-compose-a-guide-to-using-the-inclusive-attribute-b66916a5f15c
// https://stackoverflow.com/questions/71268683/jetpack-compose-navigation-login-screen-and-different-screen-with-bottom-naviga
// https://developer.android.com/develop/ui/compose/navigation
// https://developer.android.com/guide/navigation/navigation-3

const val TAG: String = "TucanPlus"

@Serializable
data object MainNavKey : NavKey

@Serializable
data object LoginNavKey : NavKey

@Serializable
data object ModuleResultsNavKey : NavKey

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupExceptionHandling() {
        System.setProperty("io.ktor.development", "true")
        System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
        // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-debug/
        // https://dev.to/theplebdev/how-to-debug-kotlin-coroutines-in-android-jc
        // https://android.googlesource.com/platform/external/kotlinx.coroutines/+/refs/heads/android11-d1-s6-release/kotlinx-coroutines-debug/#debug-agent-and-android
        // great
        // https://blog.joetr.com/better-stack-traces-with-coroutines
        // https://dev.to/anamorphosee/kotlin-coroutines-stack-trace-issue-15dh claims it does not work for explicitly thrown exceptions anyways
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2550
        // https://github.com/Kotlin/kotlinx.coroutines/issues/74
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2327#issuecomment-716102034
        // https://discuss.kotlinlang.org/t/kotlin-coroutines-stack-trace-problem/23847
        // TODO try resumeWithException and see whether it has a good stacktrace
        val systemExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        val myHandler: Thread.UncaughtExceptionHandler =
            Thread.UncaughtExceptionHandler { thread, ex ->
                Log.e(
                    TAG,
                    "Uncaught exception in thread ${thread.name} ${ex.suppressedExceptions}",
                    ex
                )
                systemExceptionHandler?.uncaughtException(thread, ex)
            }
        // Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(myHandler)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupExceptionHandling()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val test = lifecycleScope.launch {
            val credentialSettingsFlow: OptionalCredentialSettings =
                this@MainActivity.credentialSettingsDataStore.data.first()
            setContent {
                TUCaNPlusTheme {
                    Entrypoint(credentialSettingsFlow)
                }
            }
        }
        splashScreen.apply {
            this.setKeepOnScreenCondition {
                !test.isCompleted
            }
        }
    }
}

@Composable
fun Entrypoint(credentialSettingsFlow: OptionalCredentialSettings) {
    val backStack = rememberNavBackStack(
        *(if (credentialSettingsFlow.inner == null) arrayOf(LoginNavKey) else arrayOf(
            MainNavKey,
            ModuleResultsNavKey
        ))
    )
    val context = LocalContext.current
    LaunchedEffect(true) {
        if (credentialSettingsFlow.inner != null) {
            setupBackgroundTasks(context)
        }
    }
    val entryProvider = entryProvider {
        entry<MainNavKey> { Main(backStack) }
        entry<LoginNavKey> { LoginForm(backStack) }
        entry<ModuleResultsNavKey> { ModuleResultsComposable(backStack) }
    }
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider
    )
}

// https://developer.android.com/develop/ui/compose/components/drawer
// https://developer.android.com/develop/ui/compose/graphics/images/material
// https://fonts.google.com/icons?icon.set=Material+Symbols
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedDrawerExample(
    backStack: NavBackStack<NavKey>,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    NavigationDrawerItem(
                        label = { Text("Modulergebnisse") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                backStack.add(ModuleResultsNavKey)
                            }
                        }
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TUCaN Plus") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.menu_24px),
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    DetailedDrawerExample(backStack) { innerPadding ->
        Button(
            modifier = Modifier.padding(innerPadding),
            onClick = {
                coroutineScope.launch {
                    context.credentialSettingsDataStore.updateData { old ->
                        OptionalCredentialSettings(
                            null
                        )
                    }
                    backStack[backStack.size - 1] = LoginNavKey
                }
            }) { Text("Logout") }
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

// page with much more components
// https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DropdownMenuItem(kotlin.Function0,kotlin.Function0,androidx.compose.ui.Modifier,kotlin.Function0,kotlin.Function0,kotlin.Boolean,androidx.compose.material3.MenuItemColors,androidx.compose.foundation.layout.PaddingValues,androidx.compose.foundation.interaction.MutableInteractionSource)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LongBasicDropdownMenu() {
    // 2010 - now
    // load from any latest cache entry and if there is none, show loading
    val options: List<String> = listOf(
        "Option 1",
        "Option 2",
        "Option 3",
        "Option 4",
        "Option 5",
        "Option 6",
        "Option 7",
        "Option 8",
        "Option 9",
        "Option 10",
        "Option 11",
        "Option 12",
        "Option 13",
        "Option 14",
        "Option 15",
        "Option 16",
        "Option 17",
        "Option 18",
        "Option 19",
        "Option 20",
        "Option 21",
        "Option 22",
        "Option 23",
        "Option 24",
        "Option 25"
    )
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(options[0])

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Label") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleResultsComposable(backStack: NavBackStack<NavKey>) {
    val modules = loadModules()
    DetailedDrawerExample(backStack) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LongBasicDropdownMenu()
            when (val values = modules.value) {
                null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator() }
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
            }

        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 200)
@Composable
fun ModuleComposable(
    module: ModuleResults.Module = ModuleResults.Module(
        "id",
        "name",
        ModuleResults.ModuleGrade.G1_0,
        1,
        "url",
        "url"
    )
) {
    // https://developer.android.com/develop/ui/compose/layouts/basics
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("${module.name}")
            Text("${module.id}", fontSize = 10.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${module.credits} CP")
            Text("Note ${module.grade.representation}")
        }
    }
}