package de.selfmade4u.tucanplus.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }
}

@Entity(tableName = "cache", indices = [Index(
    value = ["normalizedUrl", "updated"],
    unique = true
)])
data class CacheEntry(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val originalUrl: String,
    val normalizedUrl: String?,
    val source: String,
    val updated: LocalDateTime, // todo probably use instant here?
    val parsingError: String?,
)

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache")
    suspend fun getAll(): List<CacheEntry>

    @Insert
    suspend fun insertAll(vararg cacheEntries: CacheEntry)
}

// https://developer.android.com/studio/inspect/database

@Database(entities = [ModuleResults.ModuleResult::class, ModuleResults.ModuleResultModule::class], version = 2)
@TypeConverters(Converters::class, ModuleResults.ModuleResultsConverters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun moduleResultsDao(): ModuleResults.ModuleResultsDao
    abstract fun modulesDao(): ModuleResults.ModulesDao
}
