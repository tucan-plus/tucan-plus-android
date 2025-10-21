package de.selfmade4u.tucanplus.connector

import android.content.Context
import de.selfmade4u.tucanplus.Root
import de.selfmade4u.tucanplus.a
import de.selfmade4u.tucanplus.b
import de.selfmade4u.tucanplus.connector.Common.parseBase
import de.selfmade4u.tucanplus.connector.TucanLogin.LoginResponse
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginFailure
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginSuccess
import de.selfmade4u.tucanplus.div
import de.selfmade4u.tucanplus.doctype
import de.selfmade4u.tucanplus.form
import de.selfmade4u.tucanplus.h1
import de.selfmade4u.tucanplus.input
import de.selfmade4u.tucanplus.label
import de.selfmade4u.tucanplus.option
import de.selfmade4u.tucanplus.peek
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
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters

object ModuleResults {

    suspend fun getModuleResults(context: Context, client: HttpClient, sessionId: String, sessionCookie: String): List<Module> {
        val r = client.get("https://www.tucan.tu-darmstadt.de/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=COURSERESULTS&ARGUMENTS=-N$sessionId,-N000324,") {
            cookie("cnsc", sessionCookie)
        }
        return response(context, r) {
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
            ignoreHeader("Content-Length")
            header("Connection", "close")
            header("Pragma", "no-cache")
            header("Expires", "0")
            header("Cache-Control", "private, no-cache, no-store")
            root {
                parseModuleResults(sessionId)
            }
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

    data class Module(val id: String, val name: String, val grade: ModuleGrade, val credits: Int, val resultdetailsUrl: String, val gradeoverviewUrl: String)

    fun Root.parseModuleResults(sessionId: String): List<Module> {
        val modules = mutableListOf<Module>()
        parseBase("course_results", sessionId,"000324", {
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
        }) {
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

                                    option {
                                        attribute("value", "000000015176000")
                                        attribute("selected", "selected")
                                        text("SoSe 2025")
                                    }
                                    option { attribute("value", "000000015166000"); text("WiSe 2024/25") }
                                    option { attribute("value", "000000015156000"); text("SoSe 2024") }
                                    option { attribute("value", "000000015136000"); text("SoSe 2023") }
                                    option { attribute("value", "000000015126000"); text("WiSe 2022/23") }
                                    option { attribute("value", "000000015116000"); text("SoSe 2022") }
                                    option { attribute("value", "000000015106000"); text("WiSe 2021/22") }
                                    option { attribute("value", "000000015096000"); text("SoSe 2021") }
                                    option { attribute("value", "000000015086000"); text("WiSe 2020/21") }
                                }

                                input {
                                    attribute("name", "Refresh")
                                    attribute("type", "submit")
                                    attribute("value", "Aktualisieren")
                                    attribute("class", "img img_arrowReload")
                                }
                            }
                        }

                        input { attribute("name", "APPNAME"); attribute("type", "hidden"); attribute("value", "CampusNet") }
                        input { attribute("name", "PRGNAME"); attribute("type", "hidden"); attribute("value", "COURSERESULTS") }
                        input { attribute("name", "ARGUMENTS"); attribute("type", "hidden"); attribute("value", "sessionno,menuno,semester") }
                        input { attribute("name", "sessionno"); attribute("type", "hidden"); attribute("value",
                            sessionId
                        ) }
                        input { attribute("name", "menuno"); attribute("type", "hidden"); attribute("value", "000324") }
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
                        while (peek()?.childNodes()?.filterNot(::shouldIgnore)?.first()?.normalName() == "td") {
                            val moduleId: String;
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
                                    moduleGrade = ModuleGrade.valueOf(extractText())
                                }
                                td { attribute("class", "tbdata_numeric"); moduleCredits = extractText().toInt() }
                                td { attribute("class", "tbdata"); val status = extractText() }
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
                            val module = Module(moduleId, moduleName, moduleGrade, moduleCredits, resultdetailsUrl, gradeoverviewUrl)
                            modules.add(module)
                        }

                        tr {
                            th {
                                attribute("colspan", "2")
                                text("Semester-GPA")
                            }
                            th {
                                attribute("class", "tbdata")
                                val gpa = extractText()
                            }
                            th { val credits = extractText() }
                            th {
                                attribute("class", "tbdata")
                                attribute("colspan", "4")
                            }
                        }
                    }
                }
            }
        }
        return modules
    }
}