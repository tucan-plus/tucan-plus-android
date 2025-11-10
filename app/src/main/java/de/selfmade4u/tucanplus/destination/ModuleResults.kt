package de.selfmade4u.tucanplus.destination

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import de.selfmade4u.tucanplus.DetailedDrawerExample
import de.selfmade4u.tucanplus.MyDatabaseProvider
import de.selfmade4u.tucanplus.TAG
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse
import de.selfmade4u.tucanplus.connector.ModuleGrade
import de.selfmade4u.tucanplus.connector.Semester
import de.selfmade4u.tucanplus.connector.Semesterauswahl
import de.selfmade4u.tucanplus.credentialSettingsDataStore
import de.selfmade4u.tucanplus.data.ModuleResults

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModuleResultsComposable(backStack: NavBackStack<NavKey>, isLoading: MutableState<Boolean>) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    var updateCounter by remember { mutableStateOf(false) }
    val modules by produceState<AuthenticatedResponse<ModuleResults.ModuleResultWithModules>?>(initialValue = null, updateCounter) {
        Log.i(TAG, "Loading")
        ModuleResults.getCached(MyDatabaseProvider.getDatabase(context))?.let { value = AuthenticatedResponse.Success(it) }
        isLoading.value = false
        value = ModuleResults.refreshModuleResults(context.credentialSettingsDataStore, MyDatabaseProvider.getDatabase(context))
        isRefreshing = false
        Log.e(TAG, "Loaded ${value.toString()}")
    }
    val state = rememberPullToRefreshState()
    DetailedDrawerExample(backStack) { innerPadding ->
        PullToRefreshBox(isRefreshing, onRefresh = {
            isRefreshing = true
            updateCounter = !updateCounter;
        }, state = state, indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }, modifier = Modifier.padding(innerPadding)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                //LongBasicDropdownMenu()
                when (val value = modules) {
                    null -> {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) { CircularWavyProgressIndicator() }
                    }

                    is AuthenticatedResponse.SessionTimeout -> {
                        Text("Session timeout")
                    }

                    is AuthenticatedResponse.Success -> {
                        value.response.modules.forEach { module ->
                            key(module.id) {
                                ModuleComposable(module)
                            }
                        }
                    }
                    is AuthenticatedResponse.NetworkLikelyTooSlow -> Text("Your network connection is likely too slow for TUCaN")
                    is AuthenticatedResponse.InvalidCredentials<*> -> Text("Invalid credentials")
                    is AuthenticatedResponse.TooManyAttempts<*> -> Text("Too many login attempts. Try again later")
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 200)
@Composable
fun ModuleComposable(
    module: ModuleResults.ModuleResultModule = ModuleResults.ModuleResultModule(
        42,
        Semesterauswahl(1, 2025, Semester.Wintersemester),
        "id",
        "Tin one ewfwf wefwe ewfw efw efwe wfwe fewfwe fweline",
        ModuleGrade.NOCH_NICHT_GESETZT,
        1,
        "url",
        "url"
    )
) {
    // https://developer.android.com/develop/ui/compose/layouts/basics
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(module.name)
            Text(module.id, fontSize = 10.sp, color = Color.Gray)
        }
        Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.End) {
            Text("${module.credits} CP")
            Text("Note ${module.grade?.representation}")
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LongBasicDropdownMenu() {
    val context = LocalContext.current
    val semesters by produceState(listOf(), "key") {
        val db = MyDatabaseProvider.getDatabase(context)
        value = db.semestersDao().getAll()
    }
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState("")
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
}*/