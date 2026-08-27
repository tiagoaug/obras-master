package br.com.tiago.obramaster.platform

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings

// KeychainSettings é a implementação já publicada e testada da lib multiplatform-settings
// para o Keychain do iOS — evita reescrever cinterop de Security.framework à mão.
@OptIn(ExperimentalSettingsImplementation::class)
actual object SecureStorage {
    private val settings = KeychainSettings(service = "br.com.tiago.obramaster.secure")

    actual fun put(key: String, value: String) {
        settings.putString(key, value)
    }

    actual fun get(key: String): String? = settings.getStringOrNull(key)

    actual fun remove(key: String) {
        settings.remove(key)
    }
}
