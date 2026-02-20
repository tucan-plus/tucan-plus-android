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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import de.selfmade4u.tucanplus.destination.ModuleResultsComposable
import de.selfmade4u.tucanplus.destination.MyExamsComposable
import de.selfmade4u.tucanplus.ui.theme.TUCaNPlusTheme
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import de.selfmade4u.tucanplus.OpenIdHelper.exchangeToken
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

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

@Serializable
data object MyExamsNavKey : NavKey

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
        // TODO store crashes
        val systemExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        val myHandler: Thread.UncaughtExceptionHandler =
            Thread.UncaughtExceptionHandler { thread, ex ->
                Log.e(
                    TAG,
                    "Uncaught exception in thread ${thread.name} ${ex.suppressedExceptions} ${ex.stackTraceToString()}",
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
        val isLoading = mutableStateOf(true)
        //lifecycleScope.launch {
         /*   Log.e(TAG, intent.data.toString());
            Log.e(TAG, intent.action.toString());
            if (intent.data != null) {
                // code
                // session_state
            }
            val credentialSettingsFlow: OptionalCredentialSettings =
                this@MainActivity.credentialSettingsDataStore.data.first()
            val prepareDb = MyDatabaseProvider.getDatabase(this@MainActivity)*/
            setContent {
                TUCaNPlusTheme {
                    Entrypoint(OptionalCredentialSettings(null), isLoading)
                }
            }
       // }
        splashScreen.apply {
            this.setKeepOnScreenCondition {
                isLoading.value
            }
        }
    }
}

@Composable
fun Entrypoint(credentialSettingsFlow: OptionalCredentialSettings, isLoading: MutableState<Boolean>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authService = remember {
        AuthorizationService(context)
    }
    DisposableEffect(Unit) {
        onDispose {
            authService.dispose()
        }
    }
    val backStack = rememberNavBackStack(
        *(if (credentialSettingsFlow.inner == null) arrayOf(LoginNavKey) else arrayOf(
            MainNavKey,
            MyExamsNavKey
        ))
    )
    /*LaunchedEffect(true) {
        if (credentialSettingsFlow.inner != null) {
            // TODO FIXME do this also after you log in, not only on app startup
            setupBackgroundTasks(context)
        }
    }*/
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
        Log.e(TAG, "mainactivity intent ${intent.data?.data}")
        val resp = AuthorizationResponse.fromIntent(intent.data!!)
        val ex = AuthorizationException.fromIntent(intent.data!!)
        if (resp != null) {
            Log.i(TAG, "Authorization success ${resp}")
            // authorization completed
            coroutineScope.launch {
                val token = authService.exchangeToken(resp)
                Log.i(TAG, "Token ${token}")
            }
        } else {
            // authorization failed, check ex for more details
            Log.e(TAG, "failed to authorize", ex)
        }
    }
    LaunchedEffect(true) {
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
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        launcher.launch(authIntent)
    }
    val entryProvider = entryProvider {
        entry<MainNavKey> {
            isLoading.value = false
            Main(backStack)
        }
        entry<LoginNavKey> {
            isLoading.value = false
            LoginForm(backStack)
        }
        entry<ModuleResultsNavKey> { ModuleResultsComposable(backStack, isLoading) }
        entry<MyExamsNavKey> { MyExamsComposable(backStack, isLoading) }
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
    title: String,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(drawerState) {
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
                    NavigationDrawerItem(
                        label = { Text("Meine Prüfungen") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                backStack.add(MyExamsNavKey)
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
                    title = { Text(title) },
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Main(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // TODO https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary all material expressive?
    DetailedDrawerExample(backStack, "TUCaN Plus") { innerPadding ->
        Button(
            shapes = ButtonDefaults.shapes(),
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


// page with much more components
// https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DropdownMenuItem(kotlin.Function0,kotlin.Function0,androidx.compose.ui.Modifier,kotlin.Function0,kotlin.Function0,kotlin.Boolean,androidx.compose.material3.MenuItemColors,androidx.compose.foundation.layout.PaddingValues,androidx.compose.foundation.interaction.MutableInteractionSource)

