package de.selfmade4u.tucanplus.connector

import androidx.datastore.core.DataStore
import com.fleeksoft.ksoup.nodes.TextNode
import de.selfmade4u.tucanplus.Localizer
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.Root
import de.selfmade4u.tucanplus.a
import de.selfmade4u.tucanplus.b
import de.selfmade4u.tucanplus.br
import de.selfmade4u.tucanplus.connector.Common.parseBase
import de.selfmade4u.tucanplus.connector.ModuleResultsConnector.ModuleResultsResponse
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

// loop semester by semester because otherwise we can't really associate entries with their semester. maybe just not support the "all"?
object MyExamsConnector {

    suspend fun getUncached(
        credentialSettingsDataStore: DataStore<OptionalCredentialSettings>,
        semester: String?
    ): AuthenticatedResponse<ModuleResultsResponse> {
        return fetchAuthenticatedWithReauthentication(
            credentialSettingsDataStore,
            { sessionId -> "https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MYEXAMS&ARGUMENTS=-N$sessionId,-N000318,${if (semester != null) { "-N$semester" } else { "" }}" },
            parser = { sessionId, menuLocalizer, response -> parse("000318", sessionId, menuLocalizer, response) }
        )
    }

    suspend fun parse(menuId: String, sessionId: String, menuLocalizer: Localizer, response: HttpResponse): ParserResponse<ModuleResultsResponse> {
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
                parseResults(menuId, sessionId, menuLocalizer)
            }
        }
    }

    fun Root.parseResults(menuId: String, sessionId: String, menuLocalizer: Localizer): ParserResponse<ModuleResultsResponse> {
        val modules = mutableListOf<ModuleResultsConnector.Module>()
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
            } else {
                print("not the normal page")
            }
        }) { localizer: Localizer, pageType ->
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
            check(pageType == "myexams") { pageType }
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
                            text(localizer.exams)
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
                                    text(localizer.course_module_semester)
                                }
                                select {
                                    attribute("id", "semester")
                                    attribute("name", "semester")
                                    attribute(
                                        "onchange",
                                        "reloadpage.createUrlAndReload('/scripts/mgrqispi.dll','CampusNet','MYEXAMS','$sessionId','$menuId','-N'+this.value);"
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
                                            if (semesterName == localizer.all) {
                                                return@option;
                                            }
                                            if (semesterName.startsWith(("SoSe "))) {
                                                year = semesterName.removePrefix("SoSe ").toInt()
                                                semester = Semester.Sommersemester
                                            } else {
                                                year = semesterName.removePrefix("WiSe ")
                                                    .substringBefore("/").toInt()
                                                semester = Semester.Wintersemester
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
                        ); attribute("value", "MYEXAMS")
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
                            attribute("class", "tbcontrol");
                            td {
                                attribute("colspan", "5")
                                a {
                                    attribute("href", "/scripts/mgrqispi.dll?APPNAME=CampusNet&amp;PRGNAME=EXAMREGISTRATION&amp;ARGUMENTS=-N$sessionId,-N000318,-N${selectedSemester!!.id.toString().padStart(15, '0')}")
                                    attribute("class", "arrow")
                                    text(localizer.exam_registration)
                                }
                            }
                        }
                        tr {
                            th { attribute("scope", "col"); attribute("id", localizer.module_results_no); text(localizer.module_results_no) }
                            th { attribute("scope", "col"); attribute("id", "Course_event_module"); text(localizer.my_exams_course_or_module)}
                            th { attribute("scope", "col"); attribute("id", "Name"); text(localizer.my_exams_name) }
                            th { attribute("scope", "col"); attribute("id", "Date"); text(localizer.my_exams_date) }
                            th {
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
                                td {
                                    attribute("class", "tbdata");
                                    // id
                                    moduleId = extractText()
                                }
                                td {
                                    attribute("class", "tbdata");
                                    a {
                                        attribute("class", "link");
                                        if (peekAttribute()?.key == "name") {
                                            attribute("name", "eventLink");
                                        }
                                        // link coursedetails
                                        attributeValue("href");
                                        // module title
                                        moduleName = extractText()
                                    }
                                    if (peek() != null) {
                                        br {

                                        }
                                        // list of courses
                                        extractText()
                                    }
                                }
                                td {
                                    attribute("class", "tbdata")
                                    a {
                                        attribute("class", "link");
                                        // examdetails
                                        attributeValue("href");
                                        /// type of exam
                                        extractText()
                                    }
                                }
                                td {
                                    attribute("class", "tbdata")
                                    if (peek() is TextNode) {
                                        extractText()
                                    } else {
                                        a {
                                            attribute("class", "link");
                                            // courseprep date link
                                            attributeValue("href");
                                            // date text
                                            extractText()
                                        }
                                    }
                                }
                                td {
                                    attribute("class", "tbdata")
                                    if (peek() is TextNode) {
                                        extractText()
                                    } else {
                                        a {
                                            // EXAMUNREG link
                                            attributeValue("href");
                                            attribute("class", "img img_arrowLeftRed");
                                            text(localizer.unregister)
                                        }
                                    }
                                }
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