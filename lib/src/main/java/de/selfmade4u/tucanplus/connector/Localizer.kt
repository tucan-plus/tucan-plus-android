package de.selfmade4u.tucanplus.connector

interface Localizer {
    val language: String
    val javascript_message: String
    val imprint: String
    val contact: String
    val print: String
    val move_to_bottom: String
    val my_tucan: String
    val my_tucan_id: Int
    val messages: String
    val messages_id: Int
    val vorlesungsverzeichnis: String
    val vorlesungsverzeichnis_id: Int
    val course_search: String
    val course_search_id: Int
    val room_search: String
    val room_search_id: Int
    val archive: String
    val archive_id: Int
    val schedule: String
    val schedule_id: Int
}

object GermanLocalizer : Localizer {
    override val language: String get() = "de"
    override val javascript_message: String
        get() = "Für maximale Nutzerfreundlichkeit empfehlen wir, die Ausführung von JavaScript und Cookies zu erlauben.Mithilfe der folgenden Accesskeys können Sie im Portal navigieren:"
    override val imprint: String
        get() = "Impressum"
    override val contact: String
        get() = "Kontakt"
    override val print: String
        get() = "Drucken"
    override val move_to_bottom: String
        get() = "Zum Ende der Seite"
    override val my_tucan: String
        get() = "Aktuelles"
    override val my_tucan_id: Int
        get() = 19
    override val messages: String
        get() = "Nachrichten"
    override val messages_id: Int
        get() = 299
    override val vorlesungsverzeichnis: String
        get() = "VV"
    override val vorlesungsverzeichnis_id: Int
        get() = 326
    override val course_search: String
        get() = "Lehrveranstaltungssuche"
    override val course_search_id: Int
        get() = 327
    override val room_search: String
        get() = "Raumsuche"
    override val room_search_id: Int
        get() = 387
    override val archive: String
        get() = "Archiv"
    override val archive_id: Int
        get() = 464
    override val schedule: String
        get() = "Stundenplan"
    override val schedule_id: Int
        get() = 268
}

object EnglishLocalizer : Localizer {
    override val language: String get() = "en"
    override val javascript_message: String
        get() = "We recommend to enable JavaScript and cookies for maximum usability of these pages. With the following access keys you can navigate through the portal:"
    override val imprint: String
        get() = "Imprint"
    override val contact: String
        get() = "Contact"
    override val print: String
        get() = "Print"
    override val move_to_bottom: String
        get() = "Move to Bottom"
    override val my_tucan: String
        get() = "My TUCaN"
    override val my_tucan_id: Int
        get() = 350
    override val messages: String
        get() = "Messages"
    override val messages_id: Int
        get() = 351
    override val vorlesungsverzeichnis: String
        get() = "Course Catalogue"
    override val vorlesungsverzeichnis_id: Int
        get() = 352
    override val course_search: String
        get() = "Course Search"
    override val course_search_id: Int
        get() = 353
    override val room_search: String
        get() = "Room Search"
    override val room_search_id: Int
        get() = 388
    override val archive: String
        get() = "Archive"
    override val archive_id: Int
        get() = 484
    override val schedule: String
        get() = "Schedule"
    override val schedule_id: Int
        get() = 54

}