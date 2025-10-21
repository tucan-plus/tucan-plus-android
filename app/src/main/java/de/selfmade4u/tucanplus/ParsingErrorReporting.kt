package de.selfmade4u.tucanplus

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// https://developer.android.com/training/data-storage/app-specific#create-storage-management-activity

// we could automatically recheck if this still fails to parse on updates

@Entity
data class ParsingError(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val url: String?,
    val error: String?,
    val source: String?
)

@Dao
interface ParsingErrorDao {
    @Query("SELECT * FROM parsingerror")
    suspend fun getAll(): List<ParsingError>

    @Insert
    suspend fun insertAll(vararg users: ParsingError)
}

@Database(entities = [ParsingError::class], version = 2)
abstract class ParsingErrorsDatabase : RoomDatabase() {
    abstract fun parsingErrorDao(): ParsingErrorDao
}
