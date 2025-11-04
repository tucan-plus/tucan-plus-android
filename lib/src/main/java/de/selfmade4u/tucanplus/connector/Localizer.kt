package de.selfmade4u.tucanplus.connector

interface Localizer {
    val language: String
}

object GermanLocalizer : Localizer {
    override val language: String get() = "de"

}

object EnglishLocalizer : Localizer {
    override val language: String get() = "en"

}