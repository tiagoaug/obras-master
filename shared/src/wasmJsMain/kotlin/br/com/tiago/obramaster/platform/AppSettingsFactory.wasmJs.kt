package br.com.tiago.obramaster.platform

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual class AppSettingsFactory {
    actual fun create(): Settings = StorageSettings()
}
