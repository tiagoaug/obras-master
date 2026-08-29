package br.com.tiago.obramaster.core.onboarding

import br.com.tiago.obramaster.platform.AppSettingsFactory

private const val KEY_ONBOARDING_CONCLUIDO = "onboarding_concluido"

/** Fase 10 (pivô Firebase) — antes, "existe algum Colaborador local?" decidia Onboarding vs.
 * Login; agora os dados são remotos (Firestore, multi-dispositivo), então essa pergunta não faz
 * mais sentido (o segundo dispositivo do mesmo Gestor veria "colaborador existe" e pularia o
 * onboarding sem nunca ter passado por ele). Isso é puramente "este aparelho já concluiu o
 * onboarding pelo menos uma vez" — uma flag local, não um dado de negócio. */
class OnboardingConcluidoStore(settingsFactory: AppSettingsFactory) {
    private val settings = settingsFactory.create()

    fun concluido(): Boolean = settings.getBoolean(KEY_ONBOARDING_CONCLUIDO, false)

    fun marcarConcluido() {
        settings.putBoolean(KEY_ONBOARDING_CONCLUIDO, true)
    }
}
