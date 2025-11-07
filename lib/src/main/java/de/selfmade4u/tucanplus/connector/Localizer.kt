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
    override val schedule_export: TextAndId get() = TODO("Not yet implemented")

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
    override val schedule_export: TextAndId get() = TODO("Not yet implemented")


}