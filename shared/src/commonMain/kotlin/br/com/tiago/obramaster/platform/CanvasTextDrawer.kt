package br.com.tiago.obramaster.platform

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §1 — desenha texto num DrawScope "cru" (sem
 * composição, ligado direto a um ImageBitmap via CanvasDrawScope — ver ReportCanvasRenderer).
 * Precisa de expect/actual porque a captura de Composable (GraphicsLayer/TextMeasurer) não está
 * disponível na versão de Compose deste projeto (1.7.3) — desenhar com a API nativa de cada
 * plataforma (Paint/Canvas do Android, Skia do resto) é a via estável. [x, y] é a posição da
 * baseline do texto, igual às APIs nativas de desenho de texto. */
expect fun DrawScope.desenharTexto(texto: String, x: Float, y: Float, tamanhoPx: Float, cor: Color)
