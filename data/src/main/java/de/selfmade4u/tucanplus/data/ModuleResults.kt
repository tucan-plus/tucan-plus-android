package de.selfmade4u.tucanplus.data

import androidx.datastore.core.DataStore
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse
import de.selfmade4u.tucanplus.connector.ModuleGrade
import de.selfmade4u.tucanplus.connector.ModuleResultsConnector
import de.selfmade4u.tucanplus.connector.ModuleResultsConnector.getModuleResultsUncached
import de.selfmade4u.tucanplus.connector.Semesterauswahl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object ModuleResults {

    // fetch for all semesters and store all at once.
    suspend fun refreshModuleResults(
        credentialSettingsDataStore: DataStore<OptionalCredentialSettings>,
        database: MyDatabase
    ): AuthenticatedResponse<ModuleResultWithModules> {
        when (val response = getModuleResultsUncached(credentialSettingsDataStore, null)) {
            is AuthenticatedResponse.Success<ModuleResultsConnector.ModuleResultsResponse> -> {
                val result = coroutineScope {
                    response.response.semesters.map { semester ->
                        async {
                            when (val response = getModuleResultsUncached(
                                credentialSettingsDataStore,
                                semester.id.toString().padStart(15, '0')
                            )) {
                                is AuthenticatedResponse.Success<ModuleResultsConnector.ModuleResultsResponse> -> {
                                    AuthenticatedResponse.Success(response.response.modules.map { m ->
                                        ModuleResultModule(
                                            0,
                                            semester,
                                            m.id,
                                            m.name,
                                            m.grade,
                                            m.credits,
                                            m.resultdetailsUrl,
                                            m.gradeoverviewUrl
                                        )
                                    })
                                }
                                else -> response.map<List<ModuleResultModule>>()
                            }
                        }
                    }
                }.awaitAll()
                val agwef: AuthenticatedResponse<List<ModuleResultModule>> = result.reduce { acc, response ->
                    when (acc) {
                        is AuthenticatedResponse.Success<List<ModuleResultModule>> -> when (response) {
                            is AuthenticatedResponse.Success<List<ModuleResultModule>> -> AuthenticatedResponse.Success(acc.response + response.response)
                            else -> response
                        }
                        else -> acc
                    }
                }
                return when (agwef) {
                    is AuthenticatedResponse.Success<List<ModuleResultModule>> -> AuthenticatedResponse.Success(persist(database, agwef.response))
                    else -> agwef.map()
                }
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

    @Entity(primaryKeys = ["moduleResultId", "id", "semester_id"])
    data class ModuleResultModule(
        var moduleResultId: Long,
        @Embedded(prefix = "semester_")
        var semester: Semesterauswahl,
        // embedded module
        var id: String,
        val name: String,
        val grade: ModuleGrade?,
        val credits: Int,
        val resultdetailsUrl: String?,
        val gradeoverviewUrl: String?
    )

    @Entity
    data class ModuleResult(@PrimaryKey(autoGenerate = true) var id: Long)

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

        @Transaction
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
    suspend fun persist(
        database: MyDatabase,
        result: List<ModuleResultModule>
    ): ModuleResultWithModules {
        // TODO check whether there were changes?
        return database.useWriterConnection {
            it.immediateTransaction {
                val moduleResultId = database.moduleResultsDao().insert(ModuleResult(0))
                val modules = result.map { m -> m.copy(moduleResultId = moduleResultId) }
                database.modulesDao().insertAll(*modules.toTypedArray())
                ModuleResultWithModules(ModuleResult(moduleResultId), modules)
            }
        }
    }

    suspend fun getCached(database: MyDatabase): ModuleResultWithModules? {
        return database.moduleResultsDao().getLast()
    }
}