package de.selfmade4u.tucanplus

import android.content.Context
import java.util.Locale

fun resourcesForLanguage(context: Context, locale: Locale) {
    val ctx = context.createConfigurationContext(context.resources.configuration.let {
        it.setLocale(Locale.GERMAN)
        it
    }).resources
    ctx.getString(R.string.app_name)
}