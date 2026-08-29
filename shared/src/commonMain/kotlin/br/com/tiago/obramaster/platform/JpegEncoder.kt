package br.com.tiago.obramaster.platform

import androidx.compose.ui.graphics.ImageBitmap

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §1 — captura de bitmap (via GraphicsLayer, ver
 * ReportCanvasRenderer) é unificada entre as 3 plataformas Compose Multiplatform, mas o encode
 * JPEG não: Android usa android.graphics.Bitmap.compress (não é Skia); iOS/Web usam Skia de
 * verdade (org.jetbrains.skia.Image.encodeToData), por isso o expect/actual aqui. */
expect fun ImageBitmap.paraBytesJpeg(qualidade: Int = 85): ByteArray
