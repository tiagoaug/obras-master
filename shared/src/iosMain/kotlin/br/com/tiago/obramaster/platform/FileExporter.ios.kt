package br.com.tiago.obramaster.platform

// UIActivityViewController exige apresentar a partir da UIViewController atual — cinterop de
// UIKit que não tenho como compilar nem validar sem Mac/Xcode. Mesma decisão do PdfOpener.ios.kt:
// isAvailable() = false, o botão de exportar/compartilhar some da UI no iOS.
actual class FileExporter {
    actual suspend fun isAvailable(): Boolean = false
    actual suspend fun compartilhar(nomeArquivo: String, bytes: ByteArray, mimeType: String): Boolean = false
}
