package de.selfmade4u.tucanplus.connector

import de.selfmade4u.tucanplus.Body
import de.selfmade4u.tucanplus.Head
import de.selfmade4u.tucanplus.Root
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


object Common {
    fun <T> Root.parseBase(
        sessionId: String,
        menuId: String,
        headInit: Head.() -> Unit,
        inner: Body.(pageType: String) -> T
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
            val language = attributeValue("xml:lang") // de or en
            val localizer = when (language) {
                "de" -> GermanLocalizer
                "en" -> EnglishLocalizer
                else -> throw IllegalStateException()
            }
            println("chose $localizer")
            // well this is a problem. maybe use a completely different library or the compose multiplatform one? but compose is not nice because it doesn't allow java.* access
            attribute("lang", localizer.language)
            head {
                title {
                    text("Technische Universität Darmstadt")
                }
                // https://github.com/tucan-plus/tucan-plus/blob/640bb9cbb9e3f8d22e8b9d6ddaabb5256b2eb0e6/crates/tucan-connector/src/head.rs#L53
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
                    attribute("src", "/js/jquery-3.6.0.min.js")
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
                    dataHash("ef6753960a6d7e3cd692df61de9af62c6bac7a1f1b00cccb3c602fd3b11f33b7")
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
                                "Impressum"
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
                                "Kontakt"
                            )
                            }
                            a {
                                attribute("href", "#"); attribute(
                                "onclick",
                                "window.print();"
                            ); attribute(
                                "class",
                                "img img_arrowPrint pageElementLeft"
                            ); text("Drucken")
                            }
                            a {
                                attribute("href", "#bottom"); attribute(
                                "class",
                                "img img_arrowDown pageElementRight"
                            ); text("Zum Ende der Seite")
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
                                    parseLoggedOutNavigation(localizer, sessionId)
                                } else {
                                    parseLoggedInNavigation(sessionId)
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
                                        "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=CHANGELANGUAGE&ARGUMENTS=-N${if (sessionId == "000000000000001") "000000000000002" else sessionId},-N002"
                                    ); attribute(
                                    "class",
                                    "img img_LangEnglish pageElementLeft"
                                ); attribute("title", "English"); text("English")
                                }

                                if (sessionId != "000000000000001") {
                                    a {
                                        attribute(
                                            "href",
                                            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=LOGOUT&ARGUMENTS=-N$sessionId,-N001"
                                        )
                                        attribute("id", "logoutButton")
                                        attribute("class", "img img_arrowLogout logout")
                                        attribute("title", "Abmelden")
                                        text("Abmelden")
                                    }
                                }
                            }

                            if (sessionId == "000000000000001") {
                                form {
                                    attribute("name", "cn_loginForm")
                                    attribute("id", "cn_loginForm")
                                    attribute("action", "/scripts/mgrqispi.dll")
                                    attribute("method", "post")
                                    attribute("class", "pageElementRight")

                                    div {
                                        fieldset {
                                            attribute("id", "fieldSet_login")
                                            legend { text("Anmeldung") }
                                            div {
                                                attribute("class", "formRow nb")
                                                div {
                                                    attribute(
                                                        "class",
                                                        "inputFieldLabel"
                                                    ); label {
                                                    attribute(
                                                        "for",
                                                        "field_user"
                                                    ); text("TU-ID:")
                                                }; input {
                                                    attribute(
                                                        "type",
                                                        "text"
                                                    ); attribute("id", "field_user"); attribute(
                                                    "name",
                                                    "usrname"
                                                ); attribute("size", "15"); attribute(
                                                    "class",
                                                    "login"
                                                ); attribute(
                                                    "maxlength",
                                                    "255"
                                                ); attribute(
                                                    "accesskey",
                                                    "n"
                                                ); attribute("autofocus", null)
                                                }
                                                }
                                                div {
                                                    attribute(
                                                        "class",
                                                        "inputFieldLabel"
                                                    ); label {
                                                    attribute(
                                                        "for",
                                                        "field_pass"
                                                    ); text("Passwort:")
                                                }; input {
                                                    attribute("type", "password"); attribute(
                                                    "id",
                                                    "field_pass"
                                                ); attribute("name", "pass"); attribute(
                                                    "value",
                                                    ""
                                                ); attribute("size", "15"); attribute(
                                                    "class",
                                                    "login"
                                                ); attribute(
                                                    "maxlength",
                                                    "255"
                                                ); attribute("accesskey", "p")
                                                }
                                                }
                                            }
                                        }
                                        input {
                                            attribute(
                                                "class",
                                                "img img_arrowSubmit login_btn"
                                            ); attribute("type", "submit"); attribute(
                                            "id",
                                            "logIn_btn"
                                        ); attribute("value", "Anmelden"); attribute(
                                            "onclick",
                                            "return checkform('cn_loginForm','usrname:TU-ID,pass:Passwort','000000000000001');"
                                        )
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
                                        ); attribute("value", "LOGINCHECK")
                                        }
                                        input {
                                            attribute("name", "ARGUMENTS"); attribute(
                                            "type",
                                            "hidden"
                                        ); attribute(
                                            "value",
                                            "clino,usrname,pass,menuno,menu_type,browser,platform"
                                        )
                                        }
                                        input {
                                            attribute("name", "clino"); attribute(
                                            "type",
                                            "hidden"
                                        ); attribute("value", "000000000000001")
                                        }
                                        input {
                                            attribute("name", "menuno"); attribute(
                                            "type",
                                            "hidden"
                                        ); attribute("value", "000000")
                                        }
                                        input {
                                            attribute("name", "menu_type"); attribute(
                                            "type",
                                            "hidden"
                                        ); attribute("value", "classic")
                                        }
                                        input {
                                            attribute("name", "browser"); attribute(
                                            "type",
                                            "hidden"
                                        ); attribute("value", "")
                                        }
                                        input {
                                            attribute("name", "platform"); attribute(
                                            "type",
                                            "hidden"
                                        ); attribute("value", "")
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
                                                text("Sie sind angemeldet als")
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
                                                text("am")
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
                                                text("um")
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
                                inner(pageType)
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
                                ); attribute("id", "pageFootControl_imp"); text("Impressum")
                                }
                                a {
                                    attribute(
                                        "href",
                                        "?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N$menuId,-Acontact"
                                    ); attribute(
                                    "class",
                                    "img img_arrowContact pageElementLeft"
                                ); attribute("id", "pageFootControl_con"); text("Kontakt")
                                }
                                a {
                                    attribute("href", "#"); attribute(
                                    "onclick",
                                    "window.print();"
                                ); attribute(
                                    "class",
                                    "img img_arrowPrint pageElementLeft"
                                ); attribute("id", "pageFootControl_pri"); text("Drucken")
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

    fun Body.parseLoggedInNavigation(sessionId: String) {
        parseLiWithChildren(
            "Aktuelles",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MLSSTART&ARGUMENTS=-N$sessionId,-N000019,",
            19
        ) {
            parseLiHref(
                "Nachrichten",
                299
            )
        }

        parseLiWithChildrenHref(
            "VV",
            326
        ) {
            parseVV(sessionId, 327, 387, 464)
        }

        parseLiWithChildren(
            "Stundenplan",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER&ARGUMENTS=-N$sessionId,-N000268,-A,-A,-N1",
            268
        ) {
            parseLi(
                "Tagesansicht",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER&ARGUMENTS=-N$sessionId,-N000269,-A,-A,-N0",
                269
            )
            parseLi(
                "Wochenansicht",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER&ARGUMENTS=-N$sessionId,-N000270,-A,-A,-N1",
                270
            )
            parseLi(
                "Monatsansicht",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MONTH&ARGUMENTS=-N$sessionId,-N000271,-A",
                271
            )
            parseLi(
                "Export",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCHEDULER_EXPORT&ARGUMENTS=-N$sessionId,-N000272,",
                272
            )
        }

        parseLiWithChildren(
            "Veranstaltungen",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000273,-Astudveranst%2Ehtml",
            273
        ) {
            parseLi(
                "Meine Module",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MYMODULES&ARGUMENTS=-N$sessionId,-N000275,",
                275
            )
            parseLi(
                "Meine Veranstaltungen",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=PROFCOURSES&ARGUMENTS=-N$sessionId,-N000274,",
                274
            )
            parseLi(
                "Meine Wahlbereiche",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=STUDENTCHOICECOURSES&ARGUMENTS=-N$sessionId,-N000307,",
                307
            )
            parseLi(
                "Anmeldung",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=REGISTRATION&ARGUMENTS=-N$sessionId,-N000311,-A",
                311
            )
            parseLi(
                "Mein aktueller Anmeldestatus",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MYREGISTRATIONS&ARGUMENTS=-N$sessionId,-N000308,-N000000000000000",
                308
            )
        }

        parseLiWithChildren(
            "Prüfungen",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000280,-Astudpruefungen%2Ehtml",
            280
        ) {
            parseLi(
                "Meine Prüfungen",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=MYEXAMS&ARGUMENTS=-N$sessionId,-N000318,",
                318
            )
            parseLiWithChildren(
                "Mein Prüfungsplan",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SCPCHOICE&ARGUMENTS=-N$sessionId,-N000389,",
                389,
                depth = 2
            ) {
                parseLi(
                    "Wichtige Hinweise",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000391,-Astudplan%2Ehtml",
                    391,
                    depth = 3
                )
            }
            parseLiWithChildren(
                "Semesterergebnisse",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000323,-Astudergebnis%2Ehtml",
                323,
                depth = 2,
            ) {
                parseLi(
                    "Modulergebnisse",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=COURSERESULTS&ARGUMENTS=-N$sessionId,-N000324,",
                    324,
                    depth = 3
                )
                parseLi(
                    "Prüfungsergebnisse",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXAMRESULTS&ARGUMENTS=-N$sessionId,-N000325,",
                    325,
                    depth = 3
                )
            }
            parseLi(
                "Leistungsspiegel",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=STUDENT_RESULT&ARGUMENTS=-N$sessionId,-N000316,-N0,-N000000000000000,-N000000000000000,-N000000000000000,-N0,-N000000000000000",
                316
            )
        }

        parseLiWithChildren(
            "Service",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000337,-Aservice%2Ehtml",
            337
        ) {
            parseLi(
                "Persönliche Daten",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=PERSADDRESS&ARGUMENTS=-N$sessionId,-N000339,-A",
                339
            )
            parseLi(
                "Meine Dokumente",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=CREATEDOCUMENT&ARGUMENTS=-N$sessionId,-N000557,",
                557
            )
            parseLiHref(
                "Anträge",
                600
            )
            parseLi(
                "Sperren",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=HOLDINFO&ARGUMENTS=-N$sessionId,-N000652,",
                652
            )
        }

        parseLiWithChildren(
            "Bewerbung",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000441,-Abewerbung",
            441
        ) {
            parseLi(
                "Herzlich Willkommen",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000442,-Abewerbung",
                442
            )
            parseLiHref(
                "Meine Bewerbung",
                443
            )
            parseLi(
                "Meine Dokumente",
                "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=CREATEDOCUMENT&ARGUMENTS=-N$sessionId,-N000444,",
                444
            )
        }

        parseLi(
            "Hilfe",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000340,-Ahilfe%2Ehtml",
            340,
            depth = 1
        )
    }

    fun Body.parseLoggedOutNavigation(localizer: Localizer, sessionId: String) {
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

                parseVV(sessionId, 335, 385, 463)
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

    private fun Body.parseVV(sessionId: String, id1: Int, id2: Int, id3: Int) {
        parseLiHref(
            "Lehrveranstaltungssuche",
            id1
        )

        parseLi(
            "Raumsuche",
            "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=SEARCHROOM&ARGUMENTS=-N$sessionId,-N000$id2,",
            id2
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
            attribute("title", "Archiv")
            attribute("id", "link000$id3")
            a {
                attribute(
                    "class",
                    "depth_2 link000$id3 navLink branchLink"
                )
                attribute(
                    "href",
                    "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N$sessionId,-N000$id3,-Avvarchivstart%2Ehtml"
                )
                text("Archiv")
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
}