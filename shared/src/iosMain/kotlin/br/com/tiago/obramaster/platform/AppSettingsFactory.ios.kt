package br.com.tiago.obramaster.platform

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual class AppSettingsFactory {
    actual fun create(): Settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
