package de.selfmade4u.tucanplus

import android.content.Context
import android.os.Debug
import androidx.room.Room
import androidx.room.useWriterConnection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object MyDatabaseProvider {
    @Volatile
    private var Instance: MyDatabase? = null
    val mutex = Mutex()

    suspend fun getDatabase(context: Context): MyDatabase {
        return Instance ?: mutex.withLock {
            return Instance ?: run {
                val db = if (Debug.isDebuggerConnected()) {
                    val db: MyDatabase = Room.databaseBuilder(context, MyDatabase::class.java, "tucan-plus.db").build();
                    try {
                        db.useWriterConnection { _ -> } // check that schema identity hash has no mismatch
                        db
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                        db.close()
                        context.deleteDatabase("tucan-plus.db")
                        val db = Room.databaseBuilder(context, MyDatabase::class.java, "tucan-plus.db").build()
                        db.useWriterConnection { _ -> }
                        db
                    }
                } else {
                    val db = Room.databaseBuilder(context, MyDatabase::class.java, "tucan-plus.db").build()
                    db.useWriterConnection { _ -> }
                    db
                }
                Instance = db
                db
            }
        }
    }
}