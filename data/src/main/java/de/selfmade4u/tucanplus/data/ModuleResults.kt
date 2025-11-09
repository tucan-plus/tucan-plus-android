package de.selfmade4u.tucanplus.data

import androidx.datastore.core.DataStore
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.Upsert
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse
import de.selfmade4u.tucanplus.connector.ModuleGrade
import de.selfmade4u.tucanplus.connector.ModuleResults
import de.selfmade4u.tucanplus.connector.ModuleResults.Semesterauswahl
import de.selfmade4u.tucanplus.connector.ModuleResults.getModuleResultsUncached
import kotlin.text.insert

object ModuleResults {

    // fetch for all semesters and store all at once.
    suspend fun refreshModuleResults(credentialSettingsDataStore: DataStore<OptionalCredentialSettings>,
                                           database: MyDatabase): AuthenticatedResponse<Unit> {
        val modules = mutableListOf<ModuleResults.Module>()
        return when (val response = getModuleResultsUncached(credentialSettingsDataStore, null)) {
            is AuthenticatedResponse.Success<ModuleResults.ModuleResultsResponse> -> {
                response.response.semesters.forEach { semester ->
                    when (val response = getModuleResultsUncached(credentialSettingsDataStore, semester.id.toString().padStart(15, '0'))) {
                        is AuthenticatedResponse.Success<ModuleResults.ModuleResultsResponse> -> {
                            modules += response.response.modules
                        }
                        else -> return response.map()
                    }
                }
                // TODO convert here?
                persist(database, modules.toList())
                AuthenticatedResponse.Success(Unit)
            }
            else -> return response.map()
        }
    }

    class ModuleResultsConverters {
        @TypeConverter
        fun fromModuleGrade(value: ModuleGrade?): String? {
            return value?.representation
        }

        @TypeConverter
        fun toModuleGrade(value: String?): ModuleGrade? {
            return ModuleGrade.entries.find { it.representation == value }
        }
    }

    @Entity
    data class ModuleResultModule(
        var moduleResultId: Long,
        var id: String,
        var semester: Semesterauswahl,
        val name: String,
        val grade: ModuleGrade,
        val credits: Int,
        val resultdetailsUrl: String,
        val gradeoverviewUrl: String
    )

    @Entity
    data class ModuleResult(var id: Long)

    data class ModuleResultWithModules(
        @Embedded val moduleResult: ModuleResult,
        @Relation(
            parentColumn = "id",
            entityColumn = "moduleResultId"
        )
        val modules: List<ModuleResultModule>
    )

    @Dao
    interface ModuleResultsDao {
        @Query("SELECT * FROM moduleresult")
        suspend fun getAll(): List<ModuleResult>

        @Transaction
        @Query("SELECT * FROM moduleresult")
        suspend fun getModuleResultsWithModules(): List<ModuleResultWithModules>

        @Insert
        suspend fun insert(moduleResults: ModuleResult): Long

        @Query("SELECT * FROM moduleresult ORDER BY id DESC LIMIT 1")
        suspend fun getLast(): ModuleResultWithModules?
    }

    @Dao
    interface ModulesDao {
        @Insert
        suspend fun insertAll(vararg modules: ModuleResultModule): List<Long>

        @Query("SELECT * FROM ModuleResultModule WHERE moduleResultId = :moduleResultId")
        suspend fun getForModuleResult(moduleResultId: Long): List<ModuleResultModule>
    }

    // only store all once
    suspend fun persist(database: MyDatabase, result: List<ModuleResultModule>): Long {
        // TODO check whether there were changes?
        return database.useWriterConnection {
            it.immediateTransaction {
                val moduleResultId = database.moduleResultsDao().insert(ModuleResult(0))
                val modules = result.map { m -> m.copy(moduleResultId = moduleResultId) }
                database.modulesDao().insertAll(*modules.toTypedArray())
                moduleResultId
            }
        }
    }

    suspend fun getCached(database: MyDatabase, semester: String?): List<ModuleResultModule>? {
        val lastModuleResult = database.moduleResultsDao().getLast()
        return lastModuleResult?.let { lastModuleResult.modules }
    }
}