package de.selfmade4u.tucanplus.destination

import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.core.database.getStringOrNull
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import de.selfmade4u.tucanplus.DetailedDrawerExample
import de.selfmade4u.tucanplus.MyDatabaseProvider
import de.selfmade4u.tucanplus.TAG
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse
import de.selfmade4u.tucanplus.connector.Semester
import de.selfmade4u.tucanplus.connector.Semesterauswahl
import de.selfmade4u.tucanplus.credentialSettingsDataStore
import de.selfmade4u.tucanplus.data.MyExams
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyExamsComposable(backStack: NavBackStack<NavKey>, isLoading: MutableState<Boolean>) {
    val context = LocalContext.current
    val uri: Uri = CalendarContract.Events.CONTENT_URI
    val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Events._ID,                     // 0
        CalendarContract.Events.TITLE,            // 1
        CalendarContract.Events.DESCRIPTION,   // 2
        CalendarContract.Events.EVENT_LOCATION,            // 3
        CalendarContract.Events.CUSTOM_APP_PACKAGE,            // 4
        CalendarContract.Events.CUSTOM_APP_URI,            // 5
    )
    val PROJECTION_ID_INDEX: Int = 0
    val PROJECTION_TITLE_INDEX: Int = 1
    val PROJECTION_DESCRIPTION_INDEX: Int = 2
    val PROJECTION_EVENT_LOCATION_INDEX: Int = 3
    val PROJECTION_CUSTOM_APP_PACKAGE_INDEX: Int = 4
    val PROJECTION_CUSTOM_APP_URI_INDEX: Int = 5
    val cur: Cursor = context.contentResolver.query(uri, EVENT_PROJECTION, null, null, null)!!
    while (cur.moveToNext()) {
        val eventId: Long = cur.getLong(PROJECTION_ID_INDEX)
        val title: String = cur.getString(PROJECTION_TITLE_INDEX)
        val description: String = cur.getString(PROJECTION_DESCRIPTION_INDEX)
        val location: String = cur.getString(PROJECTION_EVENT_LOCATION_INDEX)
        val customAppPackage: String? = cur.getStringOrNull(PROJECTION_CUSTOM_APP_PACKAGE_INDEX)
        val customAppUri: String? = cur.getStringOrNull(PROJECTION_CUSTOM_APP_URI_INDEX)
        if (customAppPackage != null) {
            Log.w(
                TAG,
                "GOT AN EVENT $eventId $title $description $location $customAppPackage $customAppUri"
            )
        }
    }
    cur.close()

    val beginTime: Calendar = Calendar.getInstance()
    beginTime.set(2012, 0, 19, 7, 30)
    val endTime: Calendar = Calendar.getInstance()
    endTime.set(2012, 0, 19, 8, 30)
    val intent: Intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
        .putExtra(CalendarContract.Events.TITLE, "Yoga")
        .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
        .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
        // maybe this doesn't work over the intent?
        .putExtra(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.packageName)
        .putExtra(CalendarContract.Events.CUSTOM_APP_URI, "${context.packageName}://test")
    context.startActivity(intent)
    var isRefreshing by remember { mutableStateOf(false) }
    var updateCounter by remember { mutableStateOf(false) }
    val modules by produceState<AuthenticatedResponse<MyExams.MyExamsWithExams>?>(initialValue = null, updateCounter) {
        Log.i(TAG, "Loading")
        MyExams.getCached(MyDatabaseProvider.getDatabase(context))?.let { value = AuthenticatedResponse.Success(it) }
        isLoading.value = false
        value = MyExams.refresh(context.credentialSettingsDataStore, MyDatabaseProvider.getDatabase(context))
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
            Column(Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())) {
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
                        value.response.exams.forEach { exam ->
                            key(exam.id) {
                                ExamComposable(exam)
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
fun ExamComposable(
    exam: MyExams.MyExamsExam = MyExams.MyExamsExam(
        1,
        Semesterauswahl(1, 2025, Semester.Sommersemester),
        "some-id",
        "The name",
        "random url",
        "Fachprüfung",
        "Heute"
    )
) {
    // https://developer.android.com/develop/ui/compose/layouts/basics
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(exam.name)
            Text(exam.examType)
            Text(exam.date)
            Text(exam.id, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
