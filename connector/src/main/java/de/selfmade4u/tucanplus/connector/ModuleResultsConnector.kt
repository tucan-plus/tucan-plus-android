package de.selfmade4u.tucanplus.connector

import androidx.datastore.core.DataStore
import de.selfmade4u.tucanplus.Localizer
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.Root
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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

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
    B("b"),
    NB("nb"),
    NOCH_NICHT_GESETZT("noch nicht gesetzt")
}

enum class Semester {
    Sommersemester,
    Wintersemester
}

data class Semesterauswahl(
    val id: Long,
    val year: Int,
    val semester: Semester
)


object ModuleResultsConnector {

    data class Module(
        var id: String,
        val name: String,
        val grade: ModuleGrade?,
        val credits: Int,
        val resultdetailsUrl: String?,
        val gradeoverviewUrl: String?
    )

    data class ModuleResultsResponse(var selectedSemester: Semesterauswahl, var semesters: List<Semesterauswahl>, var modules: List<Module>)

    suspend fun getModuleResultsUncached(
        credentialSettingsDataStore: DataStore<OptionalCredentialSettings>,
        semester: String?
    ): AuthenticatedResponse<ModuleResultsResponse> {
        return fetchAuthenticatedWithReauthentication(
            credentialSettingsDataStore,
            { sessionId -> "https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=COURSERESULTS&ARGUMENTS=-N$sessionId,-N000324,${if (semester != null) { "-N$semester" } else { "" }}" },
            parser = { sessionId, menuLocalizer, response -> parseModuleResponse("000324", sessionId, menuLocalizer, response) }
        )
    }

    suspend fun parseModuleResponse(menuId: String, sessionId: String, menuLocalizer: Localizer, response: HttpResponse): ParserResponse<ModuleResultsResponse> {
        return response(response) {
            status(HttpStatusCode.OK)
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
            maybeIgnoreHeader("vary")
            maybeIgnoreHeader("x-android-received-millis")
            maybeIgnoreHeader("x-android-response-source")
            maybeIgnoreHeader("x-android-selected-protocol")
            maybeIgnoreHeader("x-android-sent-millis")
            maybeIgnoreHeader("content-length")
            root {
                parseModuleResults(menuId, sessionId, menuLocalizer)
            }
        }
    }

    fun Root.parseModuleResults(menuId: String, sessionId: String, menuLocalizer: Localizer): ParserResponse<ModuleResultsResponse> {
        val modules = mutableListOf<Module>()
        val semesters = mutableListOf<Semesterauswahl>()
        var selectedSemester: Semesterauswahl? = null
        // menu id changes depending on language
        val response = parseBase(sessionId, menuLocalizer, menuId, {
            if (peek() != null) {
                style {
                    attribute("type", "text/css")
                    extractData()
                }
                style {
                    attribute("type", "text/css")
                    extractData()
                }
                style {
                    attribute("type", "text/css")
                    extractData()
                }
            } else {
                print("not the normal page")
            }
        }) { localizer, pageType ->
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
                return@parseBase ParserResponse.SessionTimeout<ModuleResultsResponse>()
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
                            text(localizer.choose_semester)
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
                                        "reloadpage.createUrlAndReload('/scripts/mgrqispi.dll','CampusNet','COURSERESULTS','$sessionId','$menuId','-N'+this.value);"
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
                                    attribute("value", localizer.refresh)
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
                            menuId
                        )
                        }
                    }
                }

                table {
                    attribute("class", "nb list")

                    thead {
                        tr {
                            td { attribute("class", "tbsubhead"); text(localizer.module_results_no) }
                            td { attribute("class", "tbsubhead"); text(localizer.module_results_course_name)}
                            td { attribute("class", "tbsubhead"); text(localizer.module_results_final_grade) }
                            td { attribute("class", "tbsubhead"); text(localizer.module_results_credits) }
                            td { attribute("class", "tbsubhead"); text(localizer.module_results_status) }
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
                            val moduleGrade: ModuleGrade?
                            val moduleCredits: Int
                            val resultdetailsUrl: String?
                            val gradeoverviewUrl: String?
                            tr {
                                td { attribute("class", "tbdata"); moduleId = extractText() }
                                moduleName = td { attribute("class", "tbdata"); extractText() }
                                td {
                                    attribute("class", "tbdata_numeric")
                                    attribute("style", "vertical-align:top;")
                                    if (peek() != null) {
                                        val moduleGradeText = extractText()
                                        moduleGrade =
                                            ModuleGrade.entries.find { it.representation == moduleGradeText }
                                                ?: run {
                                                    throw IllegalStateException("Unknown grade `$moduleGradeText`")
                                                }
                                    } else {
                                        moduleGrade = null;
                                    }
                                }
                                td {
                                    attribute("class", "tbdata_numeric"); moduleCredits =
                                    extractText().replace(",0", "").toInt()
                                }
                                td {
                                    attribute("class", "tbdata")
                                    if (peek() != null) {
                                        extractText()
                                    }
                                }
                                td {
                                    attribute("class", "tbdata")
                                    attribute("style", "vertical-align:top;")
                                    if (peek() != null) {
                                        a {
                                            attributeValue("id")
                                            resultdetailsUrl = attributeValue(
                                                "href",
                                            )
                                            text(localizer.module_results_exams)
                                        }
                                        script {
                                            attribute("type", "text/javascript")
                                            extractData()
                                        }
                                    } else {
                                        resultdetailsUrl = null
                                    }
                                }
                                td {
                                    attribute("class", "tbdata")
                                    if (peek() != null) {
                                        a {
                                            attributeValue("id")
                                            gradeoverviewUrl = attributeValue(
                                                "href",
                                            )
                                            attribute("class", "link")
                                            attribute(
                                                "title",
                                                localizer.module_results_grade_statistics
                                            )
                                            b { text("Ø") }
                                        }
                                        script {
                                            attribute("type", "text/javascript")
                                            extractData()
                                        }
                                    } else {
                                        gradeoverviewUrl = null
                                    }
                                }
                            }
                            val module = Module(
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
                                text(localizer.module_results_semester_gpa)
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
            return@parseBase ParserResponse.Success(ModuleResultsResponse(selectedSemester!!, semesters, modules))
        }
        return response
    }
}

