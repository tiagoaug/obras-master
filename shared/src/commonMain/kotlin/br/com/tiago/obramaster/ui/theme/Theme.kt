package br.com.tiago.obramaster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.em

private val ObraMasterLightColors = lightColorScheme(
    primary = Color(0xFF1B5E20),
    secondary = Color(0xFFF9A825),
)

private val ObraMasterDarkColors = darkColorScheme(
    primary = Color(0xFF66BB6A),
    secondary = Color(0xFFFFCA28),
)

// Alto contraste: preto/branco puros + amarelo de destaque, sem tons intermediários.
private val ObraMasterAltoContrasteColors = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFFFEB3B),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
)

@Composable
fun ObraMasterTheme(
    prefs: PrefsAcessibilidade = PrefsAcessibilidade(),
    content: @Composable () -> Unit,
) {
    val useDark = when (prefs.tema) {
        TemaPreferencia.CLARO -> false
        TemaPreferencia.ESCURO, TemaPreferencia.ALTO_CONTRASTE -> true
        TemaPreferencia.SISTEMA -> isSystemInDarkTheme()
    }
    val colorScheme = when (prefs.tema) {
        TemaPreferencia.ALTO_CONTRASTE -> ObraMasterAltoContrasteColors
        else -> if (useDark) ObraMasterDarkColors else ObraMasterLightColors
    }

    // FontePreferencia.LEGIVEL usa FontFamily.Default + espaçamento maior como aproximação —
    // uma fonte tipo OpenDyslexic de verdade precisa de um arquivo de fonte que não temos ainda.
    val fontFamily = when (prefs.fonte) {
        FontePreferencia.PADRAO -> FontFamily.Default
        FontePreferencia.SERIFADA -> FontFamily.Serif
        FontePreferencia.LEGIVEL -> FontFamily.Default
    }
    val letterSpacingExtra = if (prefs.espacamentoAumentado || prefs.fonte == FontePreferencia.LEGIVEL) 0.05.em else 0.em
    val baseTypography = MaterialTheme.typography
    val typography = Typography(
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = fontFamily, letterSpacing = letterSpacingExtra),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = fontFamily, letterSpacing = letterSpacingExtra),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = fontFamily, letterSpacing = letterSpacingExtra),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = fontFamily, letterSpacing = letterSpacingExtra),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = fontFamily, letterSpacing = letterSpacingExtra),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = fontFamily, letterSpacing = letterSpacingExtra),
    )

    CompositionLocalProvider(
        LocalPrefsAcessibilidade provides prefs,
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = LocalDensity.current.fontScale * prefs.escalaFonte,
        ),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
