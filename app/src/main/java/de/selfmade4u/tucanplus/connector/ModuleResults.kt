package de.selfmade4u.tucanplus.connector

import android.content.Context
import android.util.Log
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
import androidx.room.withTransaction
import de.selfmade4u.tucanplus.MyDatabase
import de.selfmade4u.tucanplus.Root
import de.selfmade4u.tucanplus.TAG
import de.selfmade4u.tucanplus.a
import de.selfmade4u.tucanplus.b
import de.selfmade4u.tucanplus.br
import de.selfmade4u.tucanplus.connector.Common.parseBase
import de.selfmade4u.tucanplus.div
import de.selfmade4u.tucanplus.form
import de.selfmade4u.tucanplus.h1
import de.selfmade4u.tucanplus.input
import de.selfmade4u.tucanplus.label
import de.selfmade4u.tucanplus.option
import de.selfmade4u.tucanplus.p
import de.selfmade4u.tucanplus.peek
import de.selfmade4u.tucanplus.peekAttribute
import de.selfmade4u.tucanplus.response
import de.selfmade4u.tucanplus.script
import de.selfmade4u.tucanplus.select
import de.selfmade4u.tucanplus.shouldIgnore
import de.selfmade4u.tucanplus.style
import de.selfmade4u.tucanplus.table
import de.selfmade4u.tucanplus.tbody
import de.selfmade4u.tucanplus.td
import de.selfmade4u.tucanplus.th
import de.selfmade4u.tucanplus.thead
import de.selfmade4u.tucanplus.tr
import io.ktor.client.HttpClient
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

object ModuleResults {

    suspend fun getModuleResults(
        context: Context,
        client: HttpClient,
        sessionId: String,
        sessionCookie: String
    ): ModuleResultsResponse {
        val r = try {
            client.get("https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=COURSERESULTS&ARGUMENTS=-N$sessionId,-N000324,") {
                cookie("cnsc", sessionCookie)
            }
        } catch (e: IllegalStateException) {
            if (e.message?.contains("Content-Length mismatch") ?: true) {
                return ModuleResultsResponse.NetworkLikelyTooSlow
            }
            Log.e(TAG, "Failed to fetch request", e)
            return ModuleResultsResponse.SessionTimeout
        }
        return response(context, r) {
            status(HttpStatusCode.OK)
            Log.d(TAG, "${r.headers}")
            header(
                "Content-Security-Policy",
                "default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval';"
            )
            header("Content-Type", "text/html")
            header("X-Content-Type-Options", "nosniff")
            header("X-XSS-Protection", "1; mode=block")
            header("Referrer-Policy", "strict-origin")
            header("X-Frame-Options", "SAMEORIGIN")
            maybeHeader("X-Powered-By", listOf("ASP.NET"))
            header("Server", "Microsoft-IIS/10.0")
            header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            ignoreHeader("MgMiddlewareWaitTime") // 0 or 16
            ignoreHeader("Date")
            //ignoreHeader("Content-Length")
            header("Connection", "close")
            header("Pragma", "no-cache")
            header("Expires", "0")
            header("Cache-Control", "private, no-cache, no-store")

            header("vary", "Accept-Encoding")
            ignoreHeader("x-android-received-millis")
            ignoreHeader("x-android-response-source")
            ignoreHeader("x-android-selected-protocol")
            ignoreHeader("x-android-sent-millis")
            root {
                parseModuleResults(context, sessionId)
            }
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

    // https://github.com/tucan-plus/tucan-plus/blob/640bb9cbb9e3f8d22e8b9d6ddaabb5256b2eb0e6/crates/tucan-types/src/lib.rs#L366
    enum class ModuleGrade(val representation: String) {
        G1_0("1,0"),
        G1_3("1,3"),
        G1_7("1,7"),
        G2_0("2,0"),
        G2_3("2,3"),
        G2_7("2,7"),
        G3_0("3,0"),
        G3_3("3,3"),
        G3_7("3,7"),
        G4_0("4,0"),
        G5_0("5,0"),
    }

    enum class Semester {
        Sommersemester,
        Wintersemester
    }

    @Entity(tableName = "semesters")
    data class Semesterauswahl(
        @PrimaryKey
        @ColumnInfo(name = "semester_id")
        val id: Long,
        val year: Int,
        val semester: Semester
    )

    @Dao
    interface SemestersDao {
        @Upsert
        suspend fun insertAll(vararg modules: Semesterauswahl)

        @Query("SELECT * FROM semesters")
        suspend fun getAll(): List<Semesterauswahl>
    }

    @Entity(tableName = "module", primaryKeys = ["moduleResultId", "id"])
    data class Module(
        var moduleResultId: Long,
        var id: String,
        val name: String,
        val grade: ModuleGrade,
        val credits: Int,
        val resultdetailsUrl: String,
        val gradeoverviewUrl: String
    )

    @Entity(tableName = "module_results")
    data class ModuleResult(@PrimaryKey(autoGenerate = true) var id: Long, @Embedded var selectedSemester: Semesterauswahl)

    data class ModuleResultWithModules(
        @Embedded val moduleResult: ModuleResult,
        @Relation(
            parentColumn = "id",
            entityColumn = "moduleResultId"
        )
        val modules: List<Module>
    )

    sealed class ModuleResultsResponse {
        data class Success(var moduleResult: ModuleResult, var semesters: List<Semesterauswahl>, var modules: List<Module>) :
            ModuleResultsResponse()

        data object SessionTimeout : ModuleResultsResponse()
        data object NetworkLikelyTooSlow : ModuleResultsResponse()
    }

    // https://stackoverflow.com/questions/60928706/android-room-how-to-save-an-entity-with-one-of-the-variables-being-a-sealed-cla/72535888#72535888

    @Dao
    interface ModuleResultsDao {
        @Query("SELECT * FROM module_results")
        suspend fun getAll(): List<ModuleResult>

        @Transaction
        @Query("SELECT * FROM module_results")
        suspend fun getModuleResultsWithModules(): List<ModuleResultWithModules>

        @Insert
        suspend fun insert(moduleResults: ModuleResult): Long
    }

    @Dao
    interface ModulesDao {
        @Insert
        suspend fun insertAll(vararg modules: Module): List<Long>
    }

    suspend fun Root.parseModuleResults(context: Context, sessionId: String): ModuleResultsResponse {
        val modules = mutableListOf<Module>()
        val semesters = mutableListOf<Semesterauswahl>()
        var selectedSemester: Semesterauswahl? = null;
        val response = parseBase(sessionId, "000324", {
            if (peek() != null) {
                style {
                    attribute("type", "text/css")
                    dataHash("8359c082fbd232a6739e5e2baec433802e3a0e2121ff2ff7d5140de3955fa905")
                }
                style {
                    attribute("type", "text/css")
                    dataHash("c7cbaeb4c3ca010326679083d945831e5a08d230c5542d335e297a524f1e9b61")
                }
                style {
                    attribute("type", "text/css")
                    dataHash("7fa9b301efd5a3e8f3a1c11c4283cbcf24ab1d7090b1bad17e8246e46bc31c45")
                }
            } else {
                print("not the normal page")
            }
        }) { pageType ->
            if (pageType == "timeout") {
                script {
                    attribute("type", "text/javascript")
                    // empty
                }
                h1 { text("Timeout!") }
                p {
                    b {
                        text("Es wurde seit den letzten 30 Minuten keine Abfrage mehr abgesetzt.")
                        br {}
                        text("Bitte melden Sie sich erneut an.")
                    }
                }
                return@parseBase ModuleResultsResponse.SessionTimeout
            }
            check(pageType == "course_results")
            script {
                attribute("type", "text/javascript")
                // empty
            }
            h1 { extractText() }
            div {
                attribute("class", "tb")

                form {
                    attribute("id", "semesterchange")
                    attribute("action", "/scripts/mgrqispi.dll")
                    attribute("method", "post")
                    attribute("class", "pageElementTop")

                    div {
                        div {
                            attribute("class", "tbhead")
                        }

                        div {
                            attribute("class", "tbsubhead")
                            text("Wählen Sie ein Semester")
                        }

                        div {
                            attribute("class", "formRow")
                            div {
                                attribute("class", "inputFieldLabel long")
                                label {
                                    attribute("for", "semester")
                                    text("Semester:")
                                }
                                select {
                                    attribute("id", "semester")
                                    attribute("name", "semester")
                                    attribute(
                                        "onchange",
                                        "reloadpage.createUrlAndReload('/scripts/mgrqispi.dll','CampusNet','COURSERESULTS','$sessionId','000324','-N'+this.value);"
                                    )
                                    attribute("class", "tabledata")

                                    // we can predict the value so we could use this at some places do directly get correct value
                                    // maybe do everywhere for consistency
                                    while (peek() != null) {
                                        val value: Long
                                        val selected: Boolean
                                        val semester: Semester
                                        val year: Int
                                        option {
                                            value = attributeValue("value").trimStart('0').toLong()
                                            selected = if (peekAttribute()?.key == "selected") {
                                                attribute("selected", "selected")
                                                true
                                            } else {
                                                false
                                            }
                                            val semesterName =
                                                extractText() // SoSe 2025; WiSe 2024/25
                                            if (semesterName.startsWith(("SoSe "))) {
                                                year = semesterName.removePrefix("SoSe ").toInt()
                                                semester = Semester.Sommersemester
                                            } else {
                                                year = semesterName.removePrefix("WiSe ")
                                                    .substringBefore("/").toInt()
                                                semester = Semester.Wintersemester
                                            }
                                        }
                                        if (selected) {
                                            selectedSemester = Semesterauswahl(
                                                value,
                                                year,
                                                semester
                                            )
                                        }
                                        semesters.add(
                                            Semesterauswahl(
                                                value,
                                                year,
                                                semester
                                            )
                                        )
                                    }
                                }

                                input {
                                    attribute("name", "Refresh")
                                    attribute("type", "submit")
                                    attribute("value", "Aktualisieren")
                                    attribute("class", "img img_arrowReload")
                                }
                            }
                        }

                        input {
                            attribute("name", "APPNAME"); attribute(
                            "type",
                            "hidden"
                        ); attribute("value", "CampusNet")
                        }
                        input {
                            attribute("name", "PRGNAME"); attribute(
                            "type",
                            "hidden"
                        ); attribute("value", "COURSERESULTS")
                        }
                        input {
                            attribute("name", "ARGUMENTS"); attribute(
                            "type",
                            "hidden"
                        ); attribute("value", "sessionno,menuno,semester")
                        }
                        input {
                            attribute("name", "sessionno"); attribute("type", "hidden"); attribute(
                            "value",
                            sessionId
                        )
                        }
                        input {
                            attribute("name", "menuno"); attribute("type", "hidden"); attribute(
                            "value",
                            "000324"
                        )
                        }
                    }
                }

                table {
                    attribute("class", "nb list")

                    thead {
                        tr {
                            td { attribute("class", "tbsubhead"); text("Nr.") }
                            td { attribute("class", "tbsubhead"); text("Kursname") }
                            td { attribute("class", "tbsubhead"); text("Endnote") }
                            td { attribute("class", "tbsubhead"); text("Credits") }
                            td { attribute("class", "tbsubhead"); text("Status") }
                            td {
                                attribute("class", "tbsubhead")
                                attribute("colspan", "2")
                            }
                        }
                    }

                    tbody {
                        while (peek()?.childNodes()?.filterNot(::shouldIgnore)?.first()
                                ?.normalName() == "td"
                        ) {
                            val moduleId: String
                            val moduleName: String
                            val moduleGrade: ModuleGrade
                            val moduleCredits: Int
                            val resultdetailsUrl: String
                            val gradeoverviewUrl: String
                            tr {
                                td { attribute("class", "tbdata"); moduleId = extractText() }
                                moduleName = td { attribute("class", "tbdata"); extractText() }
                                td {
                                    attribute("class", "tbdata_numeric")
                                    attribute("style", "vertical-align:top;")
                                    val moduleGradeText = extractText()
                                    moduleGrade =
                                        ModuleGrade.entries.find { it.representation == moduleGradeText }
                                            ?: run {
                                                throw IllegalStateException("Unknown grade `$moduleGradeText`")
                                            }
                                }
                                td {
                                    attribute("class", "tbdata_numeric"); moduleCredits =
                                    extractText().replace(",0", "").toInt()
                                }
                                td { attribute("class", "tbdata"); extractText() }
                                td {
                                    attribute("class", "tbdata")
                                    attribute("style", "vertical-align:top;")
                                    a {
                                        attributeValue("id")
                                        resultdetailsUrl = attributeValue(
                                            "href",
                                        )
                                        text("Prüfungen")
                                    }
                                    script {
                                        attribute("type", "text/javascript")
                                        extractData()
                                    }
                                }
                                td {
                                    attribute("class", "tbdata")
                                    a {
                                        attributeValue("id")
                                        gradeoverviewUrl = attributeValue(
                                            "href",
                                        )
                                        attribute("class", "link")
                                        attribute("title", "Notenspiegel")
                                        b { text("Ø") }
                                    }
                                    script {
                                        attribute("type", "text/javascript")
                                        extractData()
                                    }
                                }
                            }
                            val module = Module(
                                0,
                                moduleId,
                                moduleName,
                                moduleGrade,
                                moduleCredits,
                                resultdetailsUrl,
                                gradeoverviewUrl
                            )
                            modules.add(module)
                        }

                        tr {
                            th {
                                attribute("colspan", "2")
                                text("Semester-GPA")
                            }
                            th {
                                attribute("class", "tbdata")
                                extractText()
                            }
                            th { extractText() }
                            th {
                                attribute("class", "tbdata")
                                attribute("colspan", "4")
                            }
                        }
                    }
                }
            }
            // TODO separate parsing from caching
            val db = MyDatabase.getDatabase(context);
            val moduleResult = db.withTransaction {
                db.semestersDao().insertAll(*semesters.toTypedArray())
                val moduleResult = ModuleResult(0, selectedSemester!!)
                val moduleResultId = db.moduleResultsDao().insert(moduleResult)
                moduleResult.id = moduleResultId
                val modules = modules.map { m -> m.moduleResultId = moduleResultId; m }
                val moduleIds = db.modulesDao().insertAll(*modules.toTypedArray())
                /*modules.zip(moduleIds) { a, b ->
                    a.id = b
                }*/
                moduleResult
            }

            return@parseBase ModuleResultsResponse.Success(moduleResult, semesters, modules)
        }
        return response
    }
}