package de.selfmade4u.tucanplus.connector

interface Localizer {
    val language: String
    val vorlesungsverzeichnis: String
}

object GermanLocalizer : Localizer {
    override val language: String get() = "de"
    override val vorlesungsverzeichnis: String get() = "/scripts/mgrqispi.dll?APPNAME=CampusNet&PRGNAME=ACTION&ARGUMENTS=-AR~11V0hv-BytBNU72hvo-g0Oo7Gxt48gGhIQg3tkSzSPyMYBsMwYzZvAR42~cq5pY91Gpt1Ii868MGJMX0wXM0QSwYibVfW-tlMSERpmHlkMGi0X7QGTv1gFEXOnpdnP07-Ah750gFYfr6umq0mTCKCkX~JNatm6cgmftrpaxn9ivmjyF4fYupfK8A__"

}

object EnglishLocalizer : Localizer {
    override val language: String get() = "en"
    override val vorlesungsverzeichnis: String get() = ""

}