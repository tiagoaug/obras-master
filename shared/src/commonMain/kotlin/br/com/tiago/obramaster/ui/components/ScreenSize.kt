package br.com.tiago.obramaster.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** SPEC_OBRA_MASTER_KMP.md §1.2 — COMPACT <600dp, MEDIUM 600-840dp, EXPANDED >840dp. */
enum class ScreenSize {
    COMPACT, MEDIUM, EXPANDED;

    companion object {
        fun from(width: Dp): ScreenSize = when {
            width < 600.dp -> COMPACT
            width < 840.dp -> MEDIUM
            else -> EXPANDED
        }
    }
}

/** Mede a largura disponível e expõe o [ScreenSize] correspondente ao conteúdo. */
@Composable
fun WithScreenSize(content: @Composable (ScreenSize) -> Unit) {
    BoxWithConstraints {
        content(ScreenSize.from(maxWidth))
    }
}
