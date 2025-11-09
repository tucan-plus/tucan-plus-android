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
import de.selfmade4u.tucanplus.connector.ModuleResults.getModuleResultsUncached
import kotlin.text.insert

object ModuleResults {

    suspend fun getModuleResultsStoreCache(credentialSettingsDataStore: DataStore<OptionalCredentialSettings>,
                                           database: MyDatabase, semester: String?): AuthenticatedResponse<ModuleResultsResponse> {
        return when (val response = getModuleResultsUncached(credentialSettingsDataStore, semester)) {
            is AuthenticatedResponse.Success<ModuleResultsResponse> -> {
                AuthenticatedResponse.Success(persist(database, response.response))
            }
            else -> response
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

    data class ModuleResultWithModules(
        @Embedded val moduleResult: ModuleResult,
        @Relation(
            parentColumn = "id",
            entityColumn = "moduleResultId"
        )
        val modules: List<Module>
    )

    @Dao
    interface ModuleResultsDao {
        @Query("SELECT * FROM module_results")
        suspend fun getAll(): List<ModuleResult>

        @Transaction
        @Query("SELECT * FROM module_results")
        suspend fun getModuleResultsWithModules(): List<ModuleResultWithModules>

        @Insert
        suspend fun insert(moduleResults: ModuleResult): Long

        @Query("SELECT * FROM module_results ORDER BY id DESC LIMIT 1")
        suspend fun getLast(): ModuleResultWithModules?
    }

    @Dao
    interface ModulesDao {
        @Insert
        suspend fun insertAll(vararg modules: Module): List<Long>

        @Query("SELECT * FROM module WHERE moduleResultId = :moduleResultId")
        suspend fun getForModuleResult(moduleResultId: Long): List<Module>
    }

    @Dao
    interface SemestersDao {
        @Upsert
        suspend fun insertAll(vararg modules: Semesterauswahl)

        @Query("SELECT * FROM semesters")
        suspend fun getAll(): List<Semesterauswahl>
    }

    // only store all once
    suspend fun persist(database: MyDatabase, result: List<ModuleResultsResponse>): ModuleResultsResponse {
        // TODO check whether there were changes?

        val moduleResult = database.useWriterConnection {
            it.immediateTransaction {
                database.semestersDao().insertAll(*result.first().semesters.toTypedArray())
                val moduleResult = result.moduleResult
                val moduleResultId = database.moduleResultsDao().insert(moduleResult)
                moduleResult.id = moduleResultId
                val modules = result.modules.map { m -> m.moduleResultId = moduleResultId; m }
                database.modulesDao().insertAll(*modules.toTypedArray())
                /*modules.zip(moduleIds) { a, b ->
                a.id = b
            }*/
                moduleResult
            }
        }
        return ModuleResultsResponse(moduleResult, result.semesters, result.modules)
    }

    suspend fun getCached(database: MyDatabase, semester: String?): ModuleResultsResponse? {
        val semesters = database.semestersDao().getAll()
        val lastModuleResult = database.moduleResultsDao().getLast()
        return lastModuleResult?.let {  ModuleResultsResponse(lastModuleResult.moduleResult, semesters, lastModuleResult.modules) }
    }
}