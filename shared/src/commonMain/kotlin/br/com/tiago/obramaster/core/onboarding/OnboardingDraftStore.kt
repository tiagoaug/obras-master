package br.com.tiago.obramaster.core.onboarding

import br.com.tiago.obramaster.platform.AppSettingsFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private const val KEY_DRAFT = "onboarding_draft_json"
private const val KEY_PREFERENCIAS_FUTURAS = "onboarding_preferencias_futuras_json"

/** Rascunho incremental do onboarding — SPEC_ONBOARDING.md §5 ("persistido em Settings local"). */
class OnboardingDraftStore(settingsFactory: AppSettingsFactory) {
    private val settings = settingsFactory.create()
    private val json = Json { ignoreUnknownKeys = true }

    fun carregarRascunho(): OnboardingState? {
        val texto = settings.getStringOrNull(KEY_DRAFT) ?: return null
        return runCatching { json.decodeFromString(OnboardingState.serializer(), texto) }.getOrNull()
    }

    fun salvarRascunho(state: OnboardingState) {
        settings.putString(KEY_DRAFT, json.encodeToString(OnboardingState.serializer(), state))
    }

    fun limparRascunho() {
        settings.remove(KEY_DRAFT)
    }

    fun salvarPreferenciasFuturas(preferencias: PreferenciasPosOnboarding) {
        settings.putString(KEY_PREFERENCIAS_FUTURAS, json.encodeToString(preferencias))
    }

    fun carregarPreferenciasFuturas(): PreferenciasPosOnboarding? {
        val texto = settings.getStringOrNull(KEY_PREFERENCIAS_FUTURAS) ?: return null
        return runCatching { json.decodeFromString<PreferenciasPosOnboarding>(texto) }.getOrNull()
    }
}
