package de.selfmade4u.tucanplus

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SyncRequest
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

@Composable
private fun CalendarExperiment(context: Context, scope: CoroutineScope) {
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.all { it.value }) {
                Toast.makeText(context, "Calendar permissions granted", Toast.LENGTH_SHORT).show()
                // Launch a background coroutine to handle all blocking calendar operations
                scope.launch(Dispatchers.IO) {
                    performCalendarOperations(context)
                }
            } else {
                Toast.makeText(context, "Calendar permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Trigger the permission request when the composable enters the composition
    LaunchedEffect(Unit) {
        calendarPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
        )
    }
}

/**
 * A container function to orchestrate all calendar-related background tasks.
 */
private suspend fun performCalendarOperations(context: Context) {
    // 1. Delete old events created by this app
    deleteOldAppEvents(context)

    // 2. Log available calendars for debugging purposes
    logAvailableCalendars(context)

    // 3. Insert a new event and get its URI
    val newEventUri = insertNewCalendarEvent(context)

    // 4. Process the newly created event
    newEventUri?.let { uri ->
        Log.w(TAG, "Successfully inserted event: $uri")
        val eventID = uri.lastPathSegment?.toLongOrNull()
        if (eventID != null) {
            // ... do something with event ID ...
        }

        // 5. Register an observer to watch for changes to the event
        registerEventObserver(context, uri)

        // 6. Notify content resolver and request a sync
        requestCalendarSync(context, uri)

        // 7. Open the event in the calendar app after a delay
        delay(5000)
        viewEventInCalendar(context, uri)
    }
}

/**
 * Deletes all calendar events previously created by this application.
 */
private fun deleteOldAppEvents(context: Context) {
    Log.d(TAG, "Deleting old application events from calendar")
    val deleteCount = context.contentResolver.delete(
        CalendarContract.Events.CONTENT_URI,
        "${CalendarContract.Events.CUSTOM_APP_PACKAGE} = ?",
        arrayOf(context.packageName)
    )
    Log.d(TAG, "Deleted $deleteCount event(s)")
}

/**
 * Queries and logs all available calendars on the device for debugging.
 */
private fun logAvailableCalendars(context: Context) {
    val calendarProjection: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.NAME,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.ACCOUNT_TYPE
    )

    context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        calendarProjection,
        null, null, null
    )?.use { cursor ->
        Log.w(TAG, "Available Calendars:")
        while (cursor.moveToNext()) {
            val calendarId: Long = cursor.getLong(0)
            val name: String = cursor.getString(1)
            val accountName: String = cursor.getString(2)
            val accountType: String = cursor.getString(3)
            Log.w(TAG, "-> ID: $calendarId, Name: $name, Account: $accountName ($accountType)")
        }
    }
}

/**
 * Inserts a new test event into the calendar.
 *
 * @return The [Uri] of the newly created event, or null if insertion fails.
 */
private fun insertNewCalendarEvent(context: Context): Uri? {
    val calID: Long = 3
    val eventName = "Test Event ${System.currentTimeMillis()}"
    Log.d(TAG, "Creating new event: $eventName")

    val startMillis = Calendar.getInstance().run {
        set(2025, Calendar.OCTOBER, 14, 7, 30)
        timeInMillis
    }
    val endMillis = Calendar.getInstance().run {
        set(2025, Calendar.OCTOBER, 14, 8, 45)
        timeInMillis
    }

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, startMillis)
        put(CalendarContract.Events.DTEND, endMillis)
        put(CalendarContract.Events.TITLE, eventName)
        put(CalendarContract.Events.DESCRIPTION, "Group workout")
        put(CalendarContract.Events.CALENDAR_ID, calID)
        put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles") // Tip: Consider using TimeZone.getDefault().id
        put(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.packageName)
        put(CalendarContract.Events.CUSTOM_APP_URI, "${context.packageName}://test")
    }

    return context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
}

/**
 * Registers a ContentObserver to react to changes on a specific event URI.
 */
private fun registerEventObserver(context: Context, eventUri: Uri) {
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, changeUri: Uri?) {
            Log.d(TAG, "ContentObserver onChange triggered for: $changeUri")
            if (!showEvents(context)) { // showEvents presumably checks the 'DIRTY' flag
                Log.d(TAG, "Event is no longer dirty. Opening it.")
                viewEventInCalendar(context, eventUri)
            }
        }
    }
    context.contentResolver.registerContentObserver(eventUri, false, observer)
}

/**
 * Notifies the ContentResolver of a change and requests an immediate sync.
 */
private fun requestCalendarSync(context: Context, eventUri: Uri) {
    // Notify that the URI has been changed to trigger observers
    context.contentResolver.notifyChange(eventUri, null)

    // Request an immediate sync for the calendar provider authority
    eventUri.authority?.let { authority ->
        Log.d(TAG, "Requesting immediate sync for authority: $authority")
        ContentResolver.requestSync(
            SyncRequest.Builder()
                .setSyncAdapter(null, authority)
                .setIgnoreSettings(true)
                .setIgnoreBackoff(true)
                .setExpedited(true)
                .setManual(true)
                .syncOnce()
                .build()
        )
    }
}

/**
 * Starts an activity to view a specific calendar event.
 */
private fun viewEventInCalendar(context: Context, eventUri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).setData(eventUri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required when starting activity from non-activity context
    context.startActivity(intent)
}
