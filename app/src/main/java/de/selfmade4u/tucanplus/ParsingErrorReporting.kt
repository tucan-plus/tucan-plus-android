package de.selfmade4u.tucanplus

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity
data class ParsingError(
    @PrimaryKey val uid: Int,
    val url: String?,
    val error: String?,
    val source: String?
)

@Dao
interface ParsingErrorDao {
    @Query("SELECT * FROM parsingerror")
    fun getAll(): List<ParsingError>

    @Insert
    fun insertAll(vararg users: ParsingError)
}

@Database(entities = [ParsingError::class], version = 1)
abstract class ParsingErrorsDatabase : RoomDatabase() {
    abstract fun parsingErrorDao(): ParsingErrorDao
}
