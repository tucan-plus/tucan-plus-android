package de.selfmade4u.tucanplus.destination

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SyncRequest
import android.content.res.Configuration
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

fun showEvents(context: Context): Boolean {
    var somethingDirty = false
    val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Events._ID,                     // 0
        CalendarContract.Events.TITLE,            // 1
        CalendarContract.Events.DESCRIPTION,   // 2
        CalendarContract.Events.EVENT_LOCATION,            // 3
        CalendarContract.Events.CUSTOM_APP_PACKAGE,            // 4
        CalendarContract.Events.CUSTOM_APP_URI,            // 5
        CalendarContract.Events.DIRTY,            // 6
        CalendarContract.Events._SYNC_ID,            // 7
    )
    val PROJECTION_ID_INDEX: Int = 0
    val PROJECTION_TITLE_INDEX: Int = 1
    val PROJECTION_DESCRIPTION_INDEX: Int = 2
    val PROJECTION_EVENT_LOCATION_INDEX: Int = 3
    val PROJECTION_CUSTOM_APP_PACKAGE_INDEX: Int = 4
    val PROJECTION_CUSTOM_APP_URI_INDEX: Int = 5
    val PROJECTION_DIRTY_INDEX: Int = 6
    val PROJECTION_SYNC_ID_INDEX: Int = 7
    val cur = context.contentResolver.query(CalendarContract.Events.CONTENT_URI, EVENT_PROJECTION, null, null, null)!!
    while (cur.moveToNext()) {
        val eventId: Long = cur.getLong(PROJECTION_ID_INDEX)
        val title: String? = cur.getStringOrNull(PROJECTION_TITLE_INDEX)
        val description: String? = cur.getStringOrNull(PROJECTION_DESCRIPTION_INDEX)
        val location: String? = cur.getStringOrNull(PROJECTION_EVENT_LOCATION_INDEX)
        val customAppPackage: String? = cur.getStringOrNull(PROJECTION_CUSTOM_APP_PACKAGE_INDEX)
        val customAppUri: String? = cur.getStringOrNull(PROJECTION_CUSTOM_APP_URI_INDEX)
        val dirty: Int = cur.getInt(PROJECTION_DIRTY_INDEX)
        if (dirty == 1) {
            somethingDirty = true
        }
        val syncId: String? = cur.getStringOrNull(PROJECTION_SYNC_ID_INDEX)
        if (customAppPackage != null) {
            Log.w(
                TAG,
                "GOT AN EVENT $eventId $title $description $location $customAppPackage $customAppUri $dirty $syncId"
            )
        }
    }
    cur.close()
    return somethingDirty
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyExamsComposable(backStack: NavBackStack<NavKey>, isLoading: MutableState<Boolean>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { value ->
            Toast.makeText(context, "Permission response $value", Toast.LENGTH_SHORT).show()

            Log.e(TAG, "DELETING ALL OUR OLD EVENTS")
            Log.e(TAG, "deleted ${context.contentResolver.delete(CalendarContract.Events.CONTENT_URI, "${CalendarContract.Events.CUSTOM_APP_PACKAGE} = ?", arrayOf(context.packageName))}")

            val CALENDAR_PROJECTION: Array<String> = arrayOf(
                CalendarContract.Calendars._ID,                     // 0
                CalendarContract.Calendars.NAME,            // 1
                CalendarContract.Calendars.ACCOUNT_NAME, // 2
                CalendarContract.Calendars.ACCOUNT_TYPE // 3
            )
            val PROJECTION_CALENDAR_ID_INDEX: Int = 0
            val PROJECTION_CALENDAR_NAME_INDEX: Int = 1
            val PROJECTION_CALENDAR_ACCOUNT_NAME_INDEX: Int = 2
            val PROJECTION_CALENDAR_ACCOUNT_TYPE_INDEX: Int = 3
            var cur: Cursor = context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null, null)!!
            Log.w(TAG, "CALENDARS")
            while (cur.moveToNext()) {
                val calendarId: Long = cur.getLong(PROJECTION_CALENDAR_ID_INDEX)
                val name: String = cur.getString(PROJECTION_CALENDAR_NAME_INDEX)
                val accountName: String = cur.getString(PROJECTION_CALENDAR_ACCOUNT_NAME_INDEX)
                val accountType: String = cur.getString(PROJECTION_CALENDAR_ACCOUNT_TYPE_INDEX)
                Log.w(
                    TAG,
                    "GOT calendar $calendarId $name $accountName $accountType"
                )
            }
            cur.close()

            val calID: Long = 3
            val startMillis: Long = Calendar.getInstance().run {
                set(2025, 9, 14, 7, 30)
                timeInMillis
            }
            val endMillis: Long = Calendar.getInstance().run {
                set(2025, 9, 14, 8, 45)
                timeInMillis
            }
            val eventName = "abcdef ${System.currentTimeMillis()}"
            Log.e(TAG, "EVENT NAME $eventName")
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, eventName)
                put(CalendarContract.Events.DESCRIPTION, "Group workout")
                put(CalendarContract.Events.CALENDAR_ID, calID) // FIXME
                put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles")
                put(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.packageName)
                put(CalendarContract.Events.CUSTOM_APP_URI, "${context.packageName}://test")
            }
            var uri: Uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)!!
            showEvents(context)

            context.contentResolver.registerContentObserver(uri, false, object : ContentObserver(
                Handler()) {
                override fun onChange(selfChange: Boolean, changeUrl: Uri?) {
                    Log.e(TAG, "ONCHANGE $changeUrl")
                    if (!showEvents(context)) {
                        // maybe the insertion could also use a callback or so? but the query lists it so idk
                        //uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, 174)
                        val intent = Intent(Intent.ACTION_VIEW).setData(uri)
                        context.startActivity(intent)
                    }
                }
            })

            // get the event ID that is the last element in the Uri
            val eventID: Long = uri.lastPathSegment!!.toLong()
            //
            // ... do something with event ID
            //
            //
            Log.w(
                TAG,
                "inserted $eventID $uri"
            )

            // https://stackoverflow.com/questions/61012807/android-calendar-provider-does-not-return-latest-data
            // https://stackoverflow.com/questions/79314548/how-to-get-updated-information-from-a-contentprovider-android-development
            //Log.e(TAG, "authority ${uri.authority}")
            // this is required
            context.contentResolver.notifyChange(uri, null)
            // does not work when offline...
            // we're fucked, even opening google calendar only seems to work when having network access
            ContentResolver.requestSync(SyncRequest.Builder().setSyncAdapter(null, uri.authority).setIgnoreSettings(true).setIgnoreBackoff(true).setExpedited(true).setManual(true).syncOnce().build());

            scope.launch {
                delay(5000)
                val intent = Intent(Intent.ACTION_VIEW).setData(uri)
                context.startActivity(intent)
            }
        }
    LaunchedEffect(true) {
        launcher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
    }
/*
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

 */
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
