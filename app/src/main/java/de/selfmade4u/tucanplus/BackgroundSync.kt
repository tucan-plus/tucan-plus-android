package de.selfmade4u.tucanplus

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.selfmade4u.tucanplus.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// https://developer.android.com/develop/background-work/background-tasks/persistent#workmanager-features

// https://developer.android.com/reference/androidx/work/WorkInfo#getStopReason()

fun setupBackgroundTasks(context: Context) {
    Log.d(TAG, "Setting up background tasks")
    val updateGrades =
        PeriodicWorkRequestBuilder<UpdateGradesWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()

    WorkManager
        .getInstance(context)
        .enqueueUniquePeriodicWork("updateGrades", ExistingPeriodicWorkPolicy.KEEP, updateGrades)
}

class UpdateGradesWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "UpdateGradesWorker is starting....");

            //Result.retry()

            Log.d(TAG, "UpdateGradesWorker finished");
            Result.success()
        }
    }
}