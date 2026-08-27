package br.com.tiago.obramaster.platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual class AppSettingsFactory(private val context: Context) {
    actual fun create(): Settings =
        SharedPreferencesSettings(context.getSharedPreferences("obramaster_prefs", Context.MODE_PRIVATE))
}
