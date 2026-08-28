package br.com.tiago.obramaster.ui.components

import androidx.compose.ui.graphics.ImageBitmap

/** Bitmap nativo (Android) x Skia (iOS/Web) exigem decodificação diferente — daí o expect/actual. */
expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap?
