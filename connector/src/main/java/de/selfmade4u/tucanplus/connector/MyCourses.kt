package de.selfmade4u.tucanplus.connector

import androidx.datastore.core.DataStore
import de.selfmade4u.tucanplus.OptionalCredentialSettings
import de.selfmade4u.tucanplus.Root
import de.selfmade4u.tucanplus.a
import de.selfmade4u.tucanplus.b
import de.selfmade4u.tucanplus.br
import de.selfmade4u.tucanplus.connector.Common.parseBase
import de.selfmade4u.tucanplus.connector.ModuleResults.Module
import de.selfmade4u.tucanplus.connector.ModuleResults.ModuleResultsResponse
import de.selfmade4u.tucanplus.connector.ModuleResults.Semesterauswahl
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

// TODO multilingual (at least allow using both languages but don't support switching)
// TODO common interface between all these fetchers

object MyCourses {
    suspend fun get(
        credentialSettingsDataStore: DataStore<OptionalCredentialSettings>,
    ): AuthenticatedResponse<ModuleResultsResponse> {
        return fetchAuthenticatedWithReauthentication(
            credentialSettingsDataStore,
            { sessionId -> "https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=PROFCOURSES&ARGUMENTS=-N$sessionId,-N000274," },
            parser = ::parseResponse
        )
    }

    suspend fun parseResponse(sessionId: String, menuLocalizer: Localizer, response: HttpResponse): ParserResponse<ModuleResultsResponse> {
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

            header("vary", "Accept-Encoding")
            ignoreHeader("x-android-received-millis")
            ignoreHeader("x-android-response-source")
            ignoreHeader("x-android-selected-protocol")
            ignoreHeader("x-android-sent-millis")
            root {
                parseContent(sessionId, menuLocalizer)
            }
        }
    }

    fun Root.parseContent(sessionId: String, menuLocalizer: Localizer): ParserResponse<ModuleResultsResponse> {
        val modules = mutableListOf<Module>()
        val semesters = mutableListOf<Semesterauswahl>()
        var selectedSemester: Semesterauswahl? = null
        val response = parseBase(sessionId, menuLocalizer, "000324", {
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
            return@parseBase ParserResponse.Success(ModuleResultsResponse(TODO(), semesters, modules))
        }
        return response
    }
}