package br.com.tiago.obramaster.core.prefs

import br.com.tiago.obramaster.platform.AppSettingsFactory
import br.com.tiago.obramaster.ui.theme.FontePreferencia
import br.com.tiago.obramaster.ui.theme.PrefsAcessibilidade
import br.com.tiago.obramaster.ui.theme.TemaPreferencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_TEMA = "prefs_tema"
private const val KEY_FONTE = "prefs_fonte"
private const val KEY_ESCALA = "prefs_escala_fonte"
private const val KEY_ESPACAMENTO = "prefs_espacamento_aumentado"

class AccessibilityPrefsStore(settingsFactory: AppSettingsFactory) {
    private val settings = settingsFactory.create()

    private val _prefs = MutableStateFlow(carregar())
    val prefs: StateFlow<PrefsAcessibilidade> = _prefs.asStateFlow()

    fun atualizar(novo: PrefsAcessibilidade) {
        val validado = novo.copy(escalaFonte = novo.escalaFonte.coerceIn(0.85f, 1.4f))
        settings.putString(KEY_TEMA, validado.tema.name)
        settings.putString(KEY_FONTE, validado.fonte.name)
        settings.putFloat(KEY_ESCALA, validado.escalaFonte)
        settings.putBoolean(KEY_ESPACAMENTO, validado.espacamentoAumentado)
        _prefs.value = validado
    }

    private fun carregar(): PrefsAcessibilidade = PrefsAcessibilidade(
        tema = settings.getStringOrNull(KEY_TEMA)?.let { nome ->
            runCatching { TemaPreferencia.valueOf(nome) }.getOrNull()
        } ?: TemaPreferencia.SISTEMA,
        fonte = settings.getStringOrNull(KEY_FONTE)?.let { nome ->
            runCatching { FontePreferencia.valueOf(nome) }.getOrNull()
        } ?: FontePreferencia.PADRAO,
        escalaFonte = settings.getFloat(KEY_ESCALA, 1f),
        espacamentoAumentado = settings.getBoolean(KEY_ESPACAMENTO, false),
    )
}
