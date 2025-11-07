package de.selfmade4u.tucanplus.connector

data class TextAndId(val text: String, val id: Int) {
    fun id6(): String = id.toString().padStart(6, '0')
}

interface Localizer {
    val language: String
    val javascript_message: String
    val imprint: String
    val contact: String
    val print: String
    val move_to_bottom: String
    val my_tucan: TextAndId
    val messages: TextAndId
    val vorlesungsverzeichnis: TextAndId
    val course_search: TextAndId
    val room_search: TextAndId
    val archive: TextAndId
    val schedule: TextAndId
    val schedule_day: TextAndId
    val schedule_week: TextAndId
    val schedule_month: TextAndId
    val schedule_export: TextAndId
    val courses: TextAndId
    val my_modules: TextAndId
    val my_courses: TextAndId
    val courses_html: String
    val my_elective_subjects: TextAndId
    val registration: TextAndId
    val my_current_registrations: TextAndId
    val examinations: TextAndId
    val examinations_html: String
    val my_examinations: TextAndId
    val my_examination_schedule: TextAndId
    val my_examination_schedule_important_notes: TextAndId
    val my_examination_schedule_important_notes_html: String
    val semester_results: TextAndId
    val semester_results_html: String
}

object GermanLocalizer : Localizer {
    override val language: String get() = "de"
    override val javascript_message: String get() = "Für maximale Nutzerfreundlichkeit empfehlen wir, die Ausführung von JavaScript und Cookies zu erlauben.Mithilfe der folgenden Accesskeys können Sie im Portal navigieren:"
    override val imprint: String get() = "Impressum"
    override val contact: String get() = "Kontakt"
    override val print: String get() = "Drucken"
    override val move_to_bottom: String get() = "Zum Ende der Seite"
    override val my_tucan: TextAndId get() = TextAndId("Aktuelles", 19)
    override val messages: TextAndId get() = TextAndId("Nachrichten", 299)
    override val vorlesungsverzeichnis: TextAndId get() = TextAndId("VV", 326)
    override val course_search: TextAndId get() = TextAndId("Lehrveranstaltungssuche", 327)
    override val room_search: TextAndId get() = TextAndId("Raumsuche", 387)
    override val archive: TextAndId get() = TextAndId("Archiv", 464)
    override val schedule: TextAndId get() = TextAndId("Stundenplan", 268)
    override val schedule_day: TextAndId get() = TextAndId("Tagesansicht", 269)
    override val schedule_week: TextAndId get() = TextAndId("Wochenansicht", 270)
    override val schedule_month: TextAndId get() = TextAndId("Monatsansicht", 271)
    override val schedule_export: TextAndId get() = TextAndId("Export", 272)
    override val courses: TextAndId get() = TextAndId("Veranstaltungen", 273)
    override val my_modules: TextAndId get() = TextAndId("Meine Module",275)
    override val my_courses: TextAndId get() = TextAndId("Meine Veranstaltungen",274)
    override val courses_html: String get() = "studveranst%2Ehtml"
    override val my_elective_subjects: TextAndId get() = TextAndId("Meine Wahlbereiche",307)
    override val registration: TextAndId get() = TextAndId("Anmeldung",311)
    override val my_current_registrations: TextAndId get() = TextAndId("Mein aktueller Anmeldestatus",308)
    override val examinations: TextAndId get() = TextAndId("Prüfungen",280)
    override val examinations_html: String get() = "studpruefungen%2Ehtml"
    override val my_examinations: TextAndId get() = TextAndId("Meine Prüfungen",318)
    override val my_examination_schedule: TextAndId get() = TextAndId("Mein Prüfungsplan",389)
    override val my_examination_schedule_important_notes: TextAndId get() = TextAndId("Wichtige Hinweise",391)
    override val my_examination_schedule_important_notes_html: String get() = "studplan%2Ehtml"
    override val semester_results: TextAndId
        get() = TextAndId("Semesterergebnisse",323)
    override val semester_results_html: String
        get() = "studergebnis%2Ehtml"

}

object EnglishLocalizer : Localizer {
    override val language: String get() = "en"
    override val javascript_message: String get() = "We recommend to enable JavaScript and cookies for maximum usability of these pages. With the following access keys you can navigate through the portal:"
    override val imprint: String get() = "Imprint"
    override val contact: String get() = "Contact"
    override val print: String get() = "Print"
    override val move_to_bottom: String get() = "Move to Bottom"
    override val my_tucan: TextAndId get() = TextAndId("My TUCaN",350)
    override val messages: TextAndId get() = TextAndId("Messages",351)
    override val vorlesungsverzeichnis: TextAndId get() = TextAndId("Course Catalogue",352)
    override val course_search: TextAndId get() = TextAndId("Course Search", 353)
    override val room_search: TextAndId get() = TextAndId("Room Search",388)
    override val archive: TextAndId get() = TextAndId("Archive",484)
    override val schedule: TextAndId get() = TextAndId("Schedule",54)
    override val schedule_day: TextAndId get() = TextAndId("Days",55)
    override val schedule_week: TextAndId get() = TextAndId("Week", 56)
    override val schedule_month: TextAndId get() = TextAndId("Month", 57)
    override val schedule_export: TextAndId get() = TextAndId("Export", 354)
    override val courses: TextAndId get() = TextAndId("Courses", 176)
    override val my_modules: TextAndId get() = TextAndId("My Modules", 177)
    override val my_courses: TextAndId get() = TextAndId("My Courses", 356)
    override val courses_html: String get() = "estcourses%2Ehtml"
    override val my_elective_subjects: TextAndId get() = TextAndId("My Elective Subjects",357)
    override val registration: TextAndId get() = TextAndId("Registration",358)
    override val my_current_registrations: TextAndId get() = TextAndId("My Current Registrations",359)
    override val examinations: TextAndId get() = TextAndId("Examinations",360)
    override val examinations_html: String get() = "estexams%2Ehtml"
    override val my_examinations: TextAndId get() = TextAndId("My Examinations",361)
    override val my_examination_schedule: TextAndId get() = TextAndId("My Examination Schedule",390)
    override val my_examination_schedule_important_notes: TextAndId get() = TextAndId("Important notes",392)
    override val my_examination_schedule_important_notes_html: String get() = "estplan%2Ehtml"
    override val semester_results: TextAndId
        get() = TextAndId("Semester Results",362)
    override val semester_results_html: String
        get() = "estresult%2Ehtml"

}