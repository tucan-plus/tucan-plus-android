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
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.connector.AuthenticatedResponse
import de.selfmade4u.tucanplus.connector.MyExamsConnector
import de.selfmade4u.tucanplus.connector.Semesterauswahl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object MyExams {

    // fetch for all semesters and store all at once.
    suspend fun refresh(
        credentialSettingsDataStore: DataStore<OptionalCredentialSettings>,
        database: MyDatabase
    ): AuthenticatedResponse<MyExamsWithExams> {
        when (val response = MyExamsConnector.getUncached(credentialSettingsDataStore, null)) {
            is AuthenticatedResponse.Success<MyExamsConnector.MyExamsResponse> -> {
                val result = coroutineScope {
                    response.response.semesters.map { semester ->
                        async {
                            when (val response = MyExamsConnector.getUncached(
                                credentialSettingsDataStore,
                                semester.id.toString().padStart(15, '0')
                            )) {
                                is AuthenticatedResponse.Success<MyExamsConnector.MyExamsResponse> -> {
                                    AuthenticatedResponse.Success(response.response.exams.map { m ->
                                        MyExamsExam(
                                            0,
                                            semester,
                                            m.id,
                                            m.name,
                                            m.coursedetailsUrl,
                                            m.examType,
                                            m.date
                                        )
                                    })
                                }
                                else -> response.map<List<MyExamsExam>>()
                            }
                        }
                    }
                }.awaitAll()
                val agwef: AuthenticatedResponse<List<MyExamsExam>> = result.reduce { acc, response ->
                    when (acc) {
                        is AuthenticatedResponse.Success<List<MyExamsExam>> -> when (response) {
                            is AuthenticatedResponse.Success<List<MyExamsExam>> -> AuthenticatedResponse.Success(acc.response + response.response)
                            else -> response
                        }
                        else -> acc
                    }
                }
                return when (agwef) {
                    is AuthenticatedResponse.Success<List<MyExamsExam>> -> AuthenticatedResponse.Success(persist(database, agwef.response))
                    else -> agwef.map()
                }
            }
            else -> return response.map()
        }
    }

    // there can be multiple exams with different types for one course
    @Entity(primaryKeys = ["myExamsId", "id", "semester_id", "examType"])
    data class MyExamsExam(
        var myExamsId: Long,
        @Embedded(prefix = "semester_")
        var semester: Semesterauswahl,
        // embedded exam
        var id: String,
        val name: String,
        val coursedetailsUrl: String,
        val examType: String,
        val date: String
    )

    @Entity
    data class MyExams(@PrimaryKey(autoGenerate = true) var id: Long)

    data class MyExamsWithExams(
        @Embedded val myExams: MyExams,
        @Relation(
            parentColumn = "id",
            entityColumn = "myExamsId"
        )
        val exams: List<MyExamsExam>
    )

    @Dao
    interface MyExamsDao {
        @Query("SELECT * FROM myexams")
        suspend fun getAll(): List<MyExams>

        @Transaction
        @Query("SELECT * FROM myexams")
        suspend fun getWith(): List<MyExamsWithExams>

        @Insert
        suspend fun insert(myExams: MyExams): Long

        @Transaction
        @Query("SELECT * FROM myexams ORDER BY id DESC LIMIT 1")
        suspend fun getLast(): MyExamsWithExams?
    }

    @Dao
    interface MyExamsExamDao {
        @Insert
        suspend fun insertAll(vararg modules: MyExamsExam): List<Long>

        @Query("SELECT * FROM MyExamsExam WHERE myExamsId = :myExamsId")
        suspend fun getFor(myExamsId: Long): List<MyExamsExam>
    }

    // only store all once
    suspend fun persist(
        database: MyDatabase,
        result: List<MyExamsExam>
    ): MyExamsWithExams {
        // TODO check whether there were changes?
        return database.useWriterConnection {
            it.immediateTransaction {
                val myExamsId = database.myExamsDao().insert(MyExams(0))
                val exams = result.map { m -> m.copy(myExamsId = myExamsId) }.sortedWith(compareByDescending<MyExamsExam>{it.semester.id}.thenBy { it.id})
                database.myExamsExamDao().insertAll(*exams.toTypedArray())
                MyExamsWithExams(MyExams(myExamsId), exams)
            }
        }
    }

    suspend fun getCached(database: MyDatabase): MyExamsWithExams? {
        val value = database.myExamsDao().getLast()
        return value?.let { value ->
            value.copy(exams = value.exams.sortedWith(compareByDescending<MyExamsExam>{it.semester.id}.thenBy { it.id}))
        }
    }
}