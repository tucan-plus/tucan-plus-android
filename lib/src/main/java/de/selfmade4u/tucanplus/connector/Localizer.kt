package de.selfmade4u.tucanplus.connector

interface Localizer {
    val language: String
    val javascript_message: String
}

object GermanLocalizer : Localizer {
    override val language: String get() = "de"
    override val javascript_message: String
        get() = "Für maximale Nutzerfreundlichkeit empfehlen wir, die Ausführung von JavaScript und Cookies zu erlauben.Mithilfe der folgenden Accesskeys können Sie im Portal navigieren:"

}

object EnglishLocalizer : Localizer {
    override val language: String get() = "en"
    override val javascript_message: String
        get() = "We recommend to enable JavaScript and cookies for maximum usability of these pages. With the following access keys you can navigate through the portal:"

}