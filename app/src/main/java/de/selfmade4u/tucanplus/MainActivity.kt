package de.selfmade4u.tucanplus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import de.selfmade4u.tucanplus.connector.ModuleResults
import de.selfmade4u.tucanplus.ui.theme.TUCaNPlusTheme
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// main or login
// https://proandroiddev.com/mastering-navigation-in-jetpack-compose-a-guide-to-using-the-inclusive-attribute-b66916a5f15c
// https://stackoverflow.com/questions/71268683/jetpack-compose-navigation-login-screen-and-different-screen-with-bottom-naviga
// https://developer.android.com/develop/ui/compose/navigation
// https://developer.android.com/guide/navigation/navigation-3

@Serializable
data object MainNavKey : NavKey
@Serializable
data object LoginNavKey : NavKey

@Serializable
data object ModuleResultsNavKey : NavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val test = lifecycleScope.launch {
            val credentialSettingsFlow: OptionalCredentialSettings = this@MainActivity.credentialSettingsDataStore.data.first()
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
    val backStack = rememberNavBackStack(if (credentialSettingsFlow.inner == null) LoginNavKey else MainNavKey)
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
                    modifier = Modifier.padding(horizontal = 16.dp)
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
                            Icon(painter = painterResource(R.drawable.menu_24px), contentDescription = "Menu")
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
    DetailedDrawerExample(backStack) { innerPadding ->
        Button(
            modifier = Modifier.padding(innerPadding),
            onClick = {
                backStack.add(LoginNavKey)
            }) { Text("Logout") }
    }
}



@Composable
fun loadModules(): State<List<ModuleResults.Module>> {
    val context = LocalContext.current
    return produceState(initialValue = listOf()) {
        val credentialSettings: CredentialSettings = context.credentialSettingsDataStore.data.first().inner!!
        val client = HttpClient()
        value = ModuleResults.getModuleResults(client, credentialSettings.sessionId,
            CipherManager.decrypt(credentialSettings.encryptedSessionCookie))
        Log.e("LOADED", value.toString())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleResultsComposable(backStack: NavBackStack<NavKey>) {
    val modules = loadModules()
    DetailedDrawerExample(backStack) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            modules.value.forEach { module ->
                Text("module $module.name")
            }
        }
    }
}