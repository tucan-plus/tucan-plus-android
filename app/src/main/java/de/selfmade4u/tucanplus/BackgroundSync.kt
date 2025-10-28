package de.selfmade4u.tucanplus

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// https://developer.android.com/develop/background-work/background-tasks/persistent#workmanager-features

// https://developer.android.com/reference/androidx/work/WorkInfo#getStopReason()

fun startup(context: Context) {
    val saveRequest =
        PeriodicWorkRequestBuilder<CoroutineDownloadWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()

    WorkManager
        .getInstance(context)
        .enqueueUniquePeriodicWork("updateGrades", ExistingPeriodicWorkPolicy.KEEP, saveRequest)
}

class CoroutineDownloadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        withContext(Dispatchers.IO) {
            val jobs = (0 until 100).map {
                async {
                    print("TODO")
                }
            }

            // awaitAll will throw an exception if a download fails, which
            // CoroutineWorker will treat as a failure
            jobs.awaitAll()
            Result.success()
            Result.retry()
        }
    }
}