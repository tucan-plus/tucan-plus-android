package de.selfmade4u.tucanplus.connector

import de.selfmade4u.tucanplus.Body
import de.selfmade4u.tucanplus.EnglishLocalizer
import de.selfmade4u.tucanplus.GermanLocalizer
import de.selfmade4u.tucanplus.Head
import de.selfmade4u.tucanplus.Localizer
import de.selfmade4u.tucanplus.Root
import de.selfmade4u.tucanplus.TextAndId
import de.selfmade4u.tucanplus.a
import de.selfmade4u.tucanplus.b
import de.selfmade4u.tucanplus.body
import de.selfmade4u.tucanplus.div
import de.selfmade4u.tucanplus.doctype
import de.selfmade4u.tucanplus.fieldset
import de.selfmade4u.tucanplus.form
import de.selfmade4u.tucanplus.head
import de.selfmade4u.tucanplus.html
import de.selfmade4u.tucanplus.img
import de.selfmade4u.tucanplus.input
import de.selfmade4u.tucanplus.label
import de.selfmade4u.tucanplus.legend
import de.selfmade4u.tucanplus.li
import de.selfmade4u.tucanplus.link
import de.selfmade4u.tucanplus.meta
import de.selfmade4u.tucanplus.peek
import de.selfmade4u.tucanplus.script
import de.selfmade4u.tucanplus.span
import de.selfmade4u.tucanplus.title
import de.selfmade4u.tucanplus.ul
import java.time.LocalDateTime
import java.time.Month


object Common {
    fun <T> Root.parseBase(
        sessionId: String,
        menuLocalizer: Localizer,
        menuId: String,
        headInit: Head.() -> Unit,
        inner: Body.(localizer: Localizer, pageType: String) -> T
    ): T {
        var sessionId = sessionId
        var menuId = menuId
        doctype {
            attribute("#doctype", "html")
            attribute("name", "html")
            attribute("publicId", "-//W3C//DTD XHTML 1.0 Strict//EN")
            attribute("systemId", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd")
            attribute("pubsyskey", "PUBLIC")
        }
        return html {
            attribute("xmlns", "http://www.w3.org/1999/xhtml")
            val language = attributeValue("xml:lang")
            val localizer = when (language) {
                "de" -> GermanLocalizer
                "en" -> EnglishLocalizer
                else -> throw IllegalStateException()
            }
            attribute("lang", localizer.language)
            head {
                title {
                    text("Technische Universität Darmstadt")
                }
                meta {
                    attribute("http-equiv", "X-UA-Compatible")
                    attribute("content", "IE=edge")
                }
                meta {
                    attribute("http-equiv", "cache-control")
                    attribute("content", "no-cache")
                }
                meta {
                    attribute("http-equiv", "expires")
                    attribute("content", "-1")
                }
                meta {
                    attribute("http-equiv", "pragma")
                    attribute("content", "no-cache")
                }
                meta {
                    attribute("http-equiv", "Content-Type")
                    attribute("content", "text/html; charset=utf-8")
                }
                meta {
                    attribute("http-equiv", "Content-Script-Type")
                    attribute("content", "text/javascript")
                }
                meta {
                    attribute("name", "referrer")
                    attribute("content", "origin")
                }
                meta {
                    attribute("name", "keywords")
                    attribute(
                        "content",
                        "Datenlotsen,Datenlotsen Informationssysteme GmbH,CampusNet,Campus Management"
                    )
                }
                link {
                    attribute("rel", "shortcut icon")
                    attribute("type", "image/x-icon")
                    attribute("href", "/gfx/tuda/icons/favicon.ico")
                }
                script {
                    attribute("src", "/js/jquery.js")
                    attribute("type", "text/javascript")
                }
                script {
                    attribute("src", "/js/checkDate.js")
                    attribute("type", "text/javascript")
                }
                script {
                    attribute("src", "/js/edittext.js")
                    attribute("type", "text/javascript")
                }
                script {
                    attribute("src", "/js/skripts.js")
                    attribute("type", "text/javascript")
                }
                script {
                    attribute("src", "/js/x.js")
                    attribute("type", "text/javascript")
                }
                script {
                    attribute("type", "text/javascript")
                    extractData()
                }
                link {
                    attribute("id", "defLayout")
                    attribute("href", "/css/_default/def_layout.css")
                    attribute("rel", "stylesheet")
                    attribute("type", "text/css")
                    attribute("media", "screen")
                }
                link {
                    attribute("id", "defMenu")
                    attribute("href", "/css/_default/def_menu.css")
                    attribute("rel", "stylesheet")
                    attribute("type", "text/css")
                    attribute("media", "screen")
                }
                link {
                    attribute("id", "defStyles")
                    attribute("href", "/css/_default/def_styles.css")
                    attribute("rel", "stylesheet")
                    attribute("type", "text/css")
                }
                link {
                    attribute("id", "pagePrint")
                    attribute("href", "/css/_default/def_print.css")
                    attribute("rel", "stylesheet")
                    attribute("type", "text/css")
                    attribute("media", "print")
                }
                link {
                    attribute("id", "pageStyle")
                    attribute("href", "/css/styles.css")
                    attribute("rel", "stylesheet")
                    attribute("type", "text/css")
                }
                link {
                    attribute("id", "pageColors")
                    attribute("href", "/css/colors.css")
                    attribute("rel", "stylesheet")
                    attribute("type", "text/css")
                    attribute("media", "screen")
                }
                headInit()
            }
            body {
                val pageType = attributeValue("class")
                if (pageType == "timeout") {
                    sessionId = "000000000000001"
                    menuId = "000000"
                }

                div {
                    attribute("id", "Cn-system-desc")
                }

                script {
                    attribute("type", "text/javascript")
                    val _unused = extractData()
                }

                div {
                    attribute("id", "acc_pageDescription")
                    attribute("class", "hidden")
                    a {
                        attribute("name", "keypadDescription")
                        attribute("class", "hidden")
                        text("keypadDescription")
                    }
                    text(localizer.javascript_message)
                    a {
                        attribute("href", "#mainNavi"); attribute(
                        "accesskey",
                        "1"
                    ); text("1 Hauptmenü")
                    }
                    a {
                        attribute("href", "#mainContent"); attribute(
                        "accesskey",
                        "2"
                    ); text("2 Inhalt")
                    }
                    a {
                        attribute("href", "#keypadDescription"); attribute(
                        "accesskey",
                        "3"
                    ); text("3 Zurück zu dieser Anleitung")
                    }
                }

                val result = div {
                    attribute("id", "pageContainer")
                    attribute("class", "pageElementTop")

                    div {
                        attribute("class", "invAnchor"); a {
                        attribute(
                            "name",
                            "top"
                        ); attribute("class", "invAnchor")
                    }
                    }

                    div {
                        attribute("id", "pageHead")
                        attribute("class", "pageElementTop")

                        div {
                            attribute("id", "pageHeadTop")
                            attribute("class", "pageElementTop")
                            a {
                                attribute(
                                    "href",
                                    "?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$menuId,-Aimprint"
                                ); attribute(
                                "class",
                                "img img_arrowImprint pageElementLeft"
                            ); text(
                                localizer.imprint
                            )
                            }
                            a {
                                attribute(
                                    "href",
                                    "?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$menuId,-Acontact"
                                ); attribute(
                                "class",
                                "img img_arrowContact pageElementLeft"
                            ); text(
                                localizer.contact
                            )
                            }
                            a {
                                attribute("href", "#"); attribute(
                                "onclick",
                                "window.print();"
                            ); attribute(
                                "class",
                                "img img_arrowPrint pageElementLeft"
                            ); text(localizer.print)
                            }
                            a {
                                attribute("href", "#bottom"); attribute(
                                "class",
                                "img img_arrowDown pageElementRight"
                            ); text(localizer.move_to_bottom)
                            }
                        }

                        div {
                            attribute("id", "pageHeadCenter")
                            attribute("class", "pageElementTop")
                            div {
                                attribute("id", "pageHeadLeft")
                                attribute("class", "pageElementLeft")
                                a {
                                    attribute(
                                        "href",
                                        "http://www.tu-darmstadt.de"
                                    ); attribute("title", "extern http://www.tu-darmstadt.de")
                                    img {
                                        attribute("id", "imagePageHeadLeft"); attribute(
                                        "src",
                                        "/gfx/tuda/logo.gif"
                                    ); attribute("alt", "Logo Technische Universität Darmstadt")
                                    }
                                }
                            }
                            div {
                                attribute("id", "pageHeadRight"); attribute(
                                "class",
                                "pageElementRight"
                            )
                            }
                        }

                        div {
                            attribute("id", "pageHeadBottom_1")
                            attribute("class", "pageElementTop")
                            div {
                                attribute("id", "pageHeadControlsLeft")
                                attribute("class", "pageElementLeft")
                                a {
                                    attribute("class", "img pageHeadLink"); attribute(
                                    "href",
                                    "#"
                                ); attribute("id", "extraNav_link1"); attribute(
                                    "target",
                                    "_blank"
                                ); text("Homepage")
                                }
                                a {
                                    attribute("class", "img pageHeadLink"); attribute(
                                    "href",
                                    "#"
                                ); attribute("id", "extraNav_link2"); attribute(
                                    "target",
                                    "_blank"
                                ); text("standardLink undef")
                                }
                            }
                            div {
                                attribute("id", "pageHeadControlsRight")
                                attribute("class", "pageElementRight")
                                a {
                                    attribute("class", "img"); attribute(
                                    "href",
                                    "#"
                                ); attribute("id", "extraNav_link3"); attribute(
                                    "target",
                                    "_blank"
                                ); text("standardLink undef")
                                }
                                a {
                                    attribute("class", "img"); attribute(
                                    "href",
                                    "#"
                                ); attribute("id", "extraNav_link4"); attribute(
                                    "target",
                                    "_blank"
                                ); text("standardLink undef")
                                }
                                a {
                                    attribute("class", "img"); attribute(
                                    "href",
                                    "#"
                                ); attribute("id", "extraNav_link5"); attribute(
                                    "target",
                                    "_blank"
                                )
                                }
                            }
                        }

                        div {
                            attribute("id", "pageHeadBottom_2"); attribute(
                            "class",
                            "pageElementTop"
                        )
                            div {
                                attribute("id", "pageHeadBottom_2sub_1"); attribute(
                                "class",
                                "pageElementTop"
                            )
                            }
                            div {
                                attribute("id", "pageHeadBottom_2sub_2"); attribute(
                                "class",
                                "pageElementTop"
                            )
                            }
                        }

                        div {
                            attribute("id", "pageTopNavi"); attribute("class", "pageElementTop")
                            a { attribute("name", "mainNavi"); attribute("class", "hidden"); }
                            ul {
                                attribute("class", "nav depth_1 linkItemContainer")

                                if (peek()?.attr("class")?.trim() == "intern depth_1 linkItem") {
                                    parseLoggedOutNavigation(menuLocalizer, localizer, sessionId)
                                } else {
                                    parseLoggedInNavigation(menuLocalizer, localizer, sessionId)
                                }
                            }
                        }

                        div {
                            attribute("id", "pageHeadBottom_3"); attribute(
                            "class",
                            "pageElementTop"
                        )
                            div {
                                attribute("id", "pageHeadSwitchLang"); attribute(
                                "class",
                                "pageElementRight"
                            )
                                a {
                                    attribute(
                                        "href",
                                        "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=CHANGELANGUAGE&ARGUMENTS=-N${if (sessionId == "000000000000001") "000000000000002" else sessionId},-N${localizer.other_language_id}"
                                    ); attribute(
                                    "class",
                                    "img ${localizer.other_language_css} pageElementLeft"
                                ); attribute(
                                    "title",
                                    localizer.other_language
                                ); text(localizer.other_language)
                                }

                                if (sessionId != "000000000000001") {
                                    a {
                                        attribute(
                                            "href",
                                            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=LOGOUT&ARGUMENTS=-N$sessionId,-N001"
                                        )
                                        attribute("id", "logoutButton")
                                        attribute("class", "img img_arrowLogout logout")
                                        attribute("title", localizer.logout)
                                        text(localizer.logout)
                                    }
                                }
                            }

                            if (sessionId == "000000000000001") {
                                div {
                                    attribute("id", "cn_loginForm")
                                    div {
                                        a {
                                            attribute("id", "logIn_btn")
                                            attribute("class", "img img_arrowSubmit")
                                            attribute("title", "Anmelden")
                                            attribute("href", "https://dsf.tucan.tu-darmstadt.de/IdentityServer/connect/authorize?client_id=ClassicWeb&amp;scope=openid%20DSF%20email&amp;response_mode=query&amp;response_type=code&amp;ui_locales=de&amp;redirect_uri=https%3a%2f%2fwww.tucan.tu-darmstadt.de%2Fscripts%2Fmgrqispi.dll%3FAPPNAME%3DCampusNet%26PRGNAME%3DLOGINCHECK%26ARGUMENTS%3D-N000000000000001%2Cids_mode%26ids_mode%3DY")
                                            text("Anmelden")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val result = div {
                        attribute("id", "pageContentContainer"); attribute(
                        "class",
                        "pageElementTop"
                    )
                        div {
                            attribute("id", "pageLeft"); attribute(
                            "class",
                            "pageElementLeft"
                        ); div { attribute("id", "pageLeftTop") }
                        }
                        val result = div {
                            attribute("id", "pageContent"); attribute(
                            "class",
                            "pageElementLeft"
                        )
                            div { attribute("id", "featureBanner") }
                            a {
                                attribute("name", "mainContent"); attribute(
                                "class",
                                "hidden"
                            )
                            }
                            div {
                                attribute("id", "pageContentTop"); attribute(
                                "class",
                                "pageElementTop"
                            )
                                if (sessionId != "000000000000001") {
                                    div {
                                        attribute("id", "loginData")
                                        span {
                                            attribute("class", "loginDataLoggedAs")
                                            b {
                                                text(localizer.youre_logged_in_as)
                                                span { attribute("class", "colon"); text(":") }
                                            }
                                        }
                                        span {
                                            attribute("class", "loginDataName")
                                            attribute("id", "loginDataName")
                                            b {
                                                text("Name")
                                                span { attribute("class", "colon"); text(":") }
                                            }
                                            extractText()
                                        }
                                        span {
                                            attribute("class", "loginDataDate")
                                            b {
                                                text(localizer.on)
                                                span {
                                                    attribute("class", "colon")
                                                    text(":")
                                                }
                                            }
                                            extractText()
                                        }
                                        span {
                                            attribute("class", "loginDataTime")
                                            b {
                                                text(localizer.at)
                                                span {
                                                    attribute("class", "colon time_colon")
                                                    text(":")
                                                }
                                            }
                                            extractText()
                                        }
                                    }
                                }
                            }
                            div {
                                attribute("id", "contentSpacer_IE"); attribute(
                                "class",
                                "pageElementTop"
                            )
                                inner(localizer, pageType)
                            }
                        }
                        result
                    }

                    div {
                        attribute("id", "pageFoot"); attribute("class", "pageElementTop")
                        div {
                            attribute("id", "pageFootControls"); attribute(
                            "class",
                            "pageElementTop"
                        )
                            div {
                                attribute("id", "pageFootControlsLeft")
                                a {
                                    attribute(
                                        "href",
                                        "?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$menuId,-Aimprint"
                                    ); attribute(
                                    "class",
                                    "img img_arrowImprint pageElementLeft"
                                ); attribute("id", "pageFootControl_imp"); text(localizer.imprint)
                                }
                                a {
                                    attribute(
                                        "href",
                                        "?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$menuId,-Acontact"
                                    ); attribute(
                                    "class",
                                    "img img_arrowContact pageElementLeft"
                                ); attribute("id", "pageFootControl_con"); text(localizer.contact)
                                }
                                a {
                                    attribute("href", "#"); attribute(
                                    "onclick",
                                    "window.print();"
                                ); attribute(
                                    "class",
                                    "img img_arrowPrint pageElementLeft"
                                ); attribute("id", "pageFootControl_pri"); text(localizer.print)
                                }
                            }
                            div {
                                attribute(
                                    "id",
                                    "pageFootControlsRight"
                                )
                                a {
                                    attribute("href", "#top")
                                    attribute(
                                        "class",
                                        "img img_arrowUp pageElementRight"
                                    )
                                    attribute("id", "pageFootControl_up")
                                }
                            }
                        }
                    }
                    result
                }

                div { attribute("id", "IEdiv"); }
                div {
                    attribute("class", "invAnchor")
                    a {
                        attribute(
                            "name",
                            "bottom"
                        ); attribute("class", "invAnchor")
                    }
                }
                result
            }
        }
    }

    fun Body.parseLoggedInNavigation(menuLocalizer: Localizer, contentLocalizer: Localizer, sessionId: String) {
        parseLiWithChildren(
            menuLocalizer.my_tucan.text,
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MLSSTART&ARGUMENTS=-N$sessionId,-N${menuLocalizer.my_tucan.id6()},",
            menuLocalizer.my_tucan.id
        ) {
            parseLiHref(
                menuLocalizer.messages.text,
                menuLocalizer.messages.id
            )
        }

        parseLiWithChildrenHref(
            menuLocalizer.vorlesungsverzeichnis.text,
            menuLocalizer.vorlesungsverzeichnis.id
        ) {
            parseVV(menuLocalizer, sessionId, menuLocalizer.course_search.id, menuLocalizer.room_search.id, menuLocalizer.archive.id)
        }

        parseLiWithChildren(
            menuLocalizer.schedule.text,
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER&ARGUMENTS=-N$sessionId,-N${menuLocalizer.schedule.id6()},-A,-A,-N1",
            menuLocalizer.schedule.id
        ) {
            parseLi(menuLocalizer.schedule_day) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER&ARGUMENTS=-N$sessionId,-N$id6,-A,-A,-N0" }
            parseLi(menuLocalizer.schedule_week) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER&ARGUMENTS=-N$sessionId,-N$id6,-A,-A,-N1" }
            parseLi(menuLocalizer.schedule_month) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MONTH&ARGUMENTS=-N$sessionId,-N$id6,-A" }
            parseLi(menuLocalizer.schedule_export) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER_EXPORT&ARGUMENTS=-N$sessionId,-N$id6," }
        }

        parseLiWithChildren(
            menuLocalizer.courses.text,
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N${menuLocalizer.courses.id6()},-A${menuLocalizer.courses_html}",
            menuLocalizer.courses.id
        ) {
            parseLi(menuLocalizer.my_modules) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MYMODULES&ARGUMENTS=-N$sessionId,-N$id6," }
            parseLi(menuLocalizer.my_courses) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=PROFCOURSES&ARGUMENTS=-N$sessionId,-N$id6," }
            parseLi(menuLocalizer.my_elective_subjects) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=STUDENTCHOICECOURSES&ARGUMENTS=-N$sessionId,-N$id6," }
            parseLi(menuLocalizer.registration) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=REGISTRATION&ARGUMENTS=-N$sessionId,-N$id6,-A" }
            parseLi(menuLocalizer.my_current_registrations) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MYREGISTRATIONS&ARGUMENTS=-N$sessionId,-N$id6,-N000000000000000" }
        }

        parseLiWithChildren(
            menuLocalizer.examinations.text,
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N${menuLocalizer.examinations.id6()},-A${menuLocalizer.examinations_html}",
            menuLocalizer.examinations.id
        ) {
            parseLi(menuLocalizer.my_examinations) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MYEXAMS&ARGUMENTS=-N$sessionId,-N$id6," }
            parseLiWithChildren(
                menuLocalizer.my_examination_schedule.text,
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCPCHOICE&ARGUMENTS=-N$sessionId,-N${menuLocalizer.my_examination_schedule.id6()},",
                menuLocalizer.my_examination_schedule.id,
                depth = 2
            ) {
                parseLi(menuLocalizer.my_examination_schedule_important_notes, depth = 3) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$id6,-A${menuLocalizer.my_examination_schedule_important_notes_html}" }
            }
            parseLiWithChildren(
                menuLocalizer.semester_results.text,
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N${menuLocalizer.semester_results.id6()},-A${menuLocalizer.semester_results_html}",
                menuLocalizer.semester_results.id,
                depth = 2,
            ) {
                parseLi(menuLocalizer.module_results, depth = 3) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=COURSERESULTS&ARGUMENTS=-N$sessionId,-N$id6," }
                parseLi(menuLocalizer.examination_results, depth = 3) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXAMRESULTS&ARGUMENTS=-N$sessionId,-N$id6," }
            }
            parseLi(menuLocalizer.performance_record) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=STUDENT_RESULT&ARGUMENTS=-N$sessionId,-N$id6,-N0,-N000000000000000,-N000000000000000,-N000000000000000,-N0,-N000000000000000" }
        }

        parseLiWithChildren(
            menuLocalizer.service.text,
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N${menuLocalizer.service.id6()},-A${menuLocalizer.service_html}",
            menuLocalizer.service.id
        ) {
            parseLi(menuLocalizer.personal_data) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=PERSADDRESS&ARGUMENTS=-N$sessionId,-N$id6,-A" }
            parseLi(menuLocalizer.my_documents) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=CREATEDOCUMENT&ARGUMENTS=-N$sessionId,-N$id6," }
            parseLiHref(menuLocalizer.forms.text, menuLocalizer.forms.id)
            parseLi(menuLocalizer.hold_info,) { id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=HOLDINFO&ARGUMENTS=-N$sessionId,-N$id6," }
        }

        parseLiWithChildren(
            menuLocalizer.application.text,
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N${menuLocalizer.application.id6()},-A${menuLocalizer.application_html}",
            menuLocalizer.application.id
        ) {
            parseLi(menuLocalizer.application_welcome) {
                id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$id6,-A${menuLocalizer.application_html}"
            }
            parseLiHref(
                menuLocalizer.my_application.text,
                menuLocalizer.my_application.id
            )
            parseLi(menuLocalizer.application_my_documents) {
                id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=CREATEDOCUMENT&ARGUMENTS=-N$sessionId,-N$id6,"
            }
        }

        parseLi(
            menuLocalizer.help,
            depth = 1
        ) {
            id6 -> "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$id6,-A${menuLocalizer.help_html}"
        }
    }

    fun Body.parseLoggedOutNavigation(menuLocalizer: Localizer, contentLocalizer: Localizer, sessionId: String) {
        li {
            attribute("class", "intern depth_1 linkItem")
            attribute("title", "Startseite")
            attribute("id", "link000344")
            a {
                attribute("class", "depth_1 link000344 navLink")
                attribute(
                    "href",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000344,-Awelcome"
                )
                text("Startseite")
            }
        }

        li {
            attribute("class", "tree depth_1 linkItem branchLinkItem")
            attribute("title", "Vorlesungsverzeichnis (VV)")
            attribute("id", "link000334")
            a {
                attribute(
                    "class",
                    "depth_1 link000334 navLink branchLink"
                )
                // URL regularly changes
                val vvUrl = attributeValue(
                    "href",
                )
                text("Vorlesungsverzeichnis (VV)")
            }

            ul {
                attribute("class", "nav depth_2 linkItemContainer")

                parseVV(menuLocalizer, sessionId, 335, 385, 463)
            }
        }

        li {
            attribute("class", "tree depth_1 linkItem branchLinkItem")
            attribute("title", "TUCaN-Account")
            attribute("id", "link000410")
            a {
                attribute(
                    "class",
                    "depth_1 link000410 navLink branchLink"
                )
                attribute(
                    "href",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000410,-Atucan%5Faccount%2Ehtml"
                )
                text("TUCaN-Account")
            }

            ul {
                attribute("class", "nav depth_2 linkItemContainer")

                parseLi(
                    "Account anlegen",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=CREATEACCOUNT&ARGUMENTS=-N$sessionId,-N000425,",
                    425
                )

                parseLi(
                    "Passwort vergessen (nur für Bewerber/innen!)",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=LOSTPASS&ARGUMENTS=-N$sessionId,-N000426,-A",
                    426
                )
            }
        }

        parseLi(
            "Hilfe",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000340,-Ahilfe%2Ehtml",
            340,
            1
        )
    }

    private fun Body.parseVV(localizer: Localizer, sessionId: String, course_search_id: Int, room_search_id: Int, archive_id: Int) {
        parseLiHref(
            localizer.course_search.text,
            course_search_id
        )

        parseLi(
            localizer.room_search.text,
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SEARCHROOM&ARGUMENTS=-N$sessionId,-N000$room_search_id,",
            room_search_id
        )
        while (peek()?.attr("class")
                ?.trim() == "intern depth_2 linkItem"
        ) {
            li {
                attribute("class", "intern depth_2 linkItem")
                attributeValue("title")
                attributeValue("id")
                a {
                    attributeValue("class")
                    attributeValue("href")
                    extractText()
                }
            }
        }

        li {
            attribute(
                "class",
                "tree depth_2 linkItem branchLinkItem"
            )
            attribute("title", localizer.archive.text)
            attribute("id", "link000$archive_id")
            a {
                attribute(
                    "class",
                    "depth_2 link000$archive_id navLink branchLink"
                )
                attribute(
                    "href",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000$archive_id,-Avvarchivstart%2Ehtml"
                )
                text(localizer.archive.text)
            }

            ul {
                attribute(
                    "class",
                    "nav depth_3 linkItemContainer"
                )

                while (peek() != null) {
                    li {
                        attribute(
                            "class",
                            "intern depth_3 linkItem"
                        )
                        attributeValue("title")
                        attributeValue("id")
                        a {
                            attributeValue(
                                "class",
                            )
                            attributeValue(
                                "href",
                            )
                            extractText()
                        }
                    }
                }
            }
        }
    }

    private fun Body.parseLi(name: String, url: String, id: Int, depth: Int = 2) {
        val link = "link${id.toString().padStart(6, '0')}"
        return li {
            attribute("class", "intern depth_$depth linkItem")
            attribute("title", name)
            attribute("id", link)
            a {
                attribute("class", "depth_$depth $link navLink")
                attribute("href", url)
                text(name)
            }
        }
    }

    private fun Body.parseLi(name: TextAndId, depth: Int = 2, url: (id6: String) -> String) {
        parseLi(name.text, url(name.id6()), name.id, depth)
    }

    private fun Body.parseLiHref(name: String, id: Int, depth: Int = 2): String {
        val link = "link${id.toString().padStart(6, '0')}"
        return li {
            attribute("class", "intern depth_$depth linkItem")
            attribute("title", name)
            attribute("id", link)
            a {
                attribute("class", "depth_$depth $link navLink")
                val href = attributeValue(
                    "href"
                )
                text(name)
                href
            }
        }
    }

    private fun Body.parseLiWithChildrenHref(
        name: String,
        id: Int,
        depth: Int = 1,
        init: Body.() -> Unit
    ): String {
        val link = "link${id.toString().padStart(6, '0')}"
        return li {
            attribute("class", "tree depth_$depth linkItem branchLinkItem")
            attribute("title", name)
            attribute("id", link)
            val href = a {
                attribute("class", "depth_$depth $link navLink branchLink")
                val href = attributeValue(
                    "href",
                )
                text(name)
                href
            }
            ul {
                attribute("class", "nav depth_${depth + 1} linkItemContainer")
                init()
            }
            href
        }
    }


    private fun Body.parseLiWithChildren(
        name: String,
        url: String,
        id: Int,
        depth: Int = 1,
        init: Body.() -> Unit
    ) {
        val link = "link${id.toString().padStart(6, '0')}"
        li {
            attribute("class", "tree depth_$depth linkItem branchLinkItem")
            attribute("title", name)
            attribute("id", link)
            a {
                attribute("class", "depth_$depth $link navLink branchLink")
                attribute(
                    "href",
                    url
                )
                text(name)
            }
            ul {
                attribute("class", "nav depth_${depth + 1} linkItemContainer")
                init()
            }
        }
    }

    fun currentSemester(): Int {
        val dateTime = LocalDateTime.now()
        val year = dateTime.year
        val month = dateTime.month
        val offset = if (Month.APRIL <= month && month <= Month.OCTOBER) {
            // sose
            0
        } else {
            1
        }
        // 15186000
        // wise 2025
        return 15176000 + ((year - 2025)*2 + offset) * 10000
    }
}