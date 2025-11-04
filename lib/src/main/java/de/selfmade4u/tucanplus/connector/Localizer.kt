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

}