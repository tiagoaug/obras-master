package br.com.tiago.obramaster.platform

import com.russhwolf.settings.Settings

/** Preferências não sensíveis (acessibilidade, "manter conectado" etc.) — não confundir com [SecureStorage]. */
expect class AppSettingsFactory {
    fun create(): Settings
}
