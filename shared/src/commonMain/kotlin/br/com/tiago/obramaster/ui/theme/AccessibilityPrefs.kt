package br.com.tiago.obramaster.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.Serializable

/** SPEC_OBRA_MASTER.md §6.2. */
@Serializable
enum class TemaPreferencia { CLARO, ESCURO, SISTEMA, ALTO_CONTRASTE }

@Serializable
enum class FontePreferencia { PADRAO, SERIFADA, LEGIVEL }

@Serializable
data class PrefsAcessibilidade(
    val tema: TemaPreferencia = TemaPreferencia.SISTEMA,
    val fonte: FontePreferencia = FontePreferencia.PADRAO,
    val escalaFonte: Float = 1f, // 0.85f..1.4f
    val espacamentoAumentado: Boolean = false,
)

val LocalPrefsAcessibilidade = staticCompositionLocalOf { PrefsAcessibilidade() }
