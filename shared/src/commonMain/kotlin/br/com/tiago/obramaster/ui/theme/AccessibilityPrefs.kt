package br.com.tiago.obramaster.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/** SPEC_OBRA_MASTER.md §6.2. */
enum class TemaPreferencia { CLARO, ESCURO, SISTEMA, ALTO_CONTRASTE }
enum class FontePreferencia { PADRAO, SERIFADA, LEGIVEL }

data class PrefsAcessibilidade(
    val tema: TemaPreferencia = TemaPreferencia.SISTEMA,
    val fonte: FontePreferencia = FontePreferencia.PADRAO,
    val escalaFonte: Float = 1f, // 0.85f..1.4f
    val espacamentoAumentado: Boolean = false,
)

val LocalPrefsAcessibilidade = staticCompositionLocalOf { PrefsAcessibilidade() }
