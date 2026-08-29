package br.com.tiago.obramaster.platform

// Mesma decisão do FilePicker.ios.kt: abrir o "Open In"/Quick Look nativo exige apresentar um
// UIDocumentInteractionController/QLPreviewController a partir da UIViewController atual —
// cinterop de UIKit que não tenho como compilar nem validar sem Mac/Xcode. isAvailable() = false
// por enquanto — o botão "Abrir" some da UI no iOS; anexar e excluir continuam funcionando.
actual class PdfOpener {
    actual suspend fun isAvailable(): Boolean = false
    actual suspend fun abrir(pdfBytes: ByteArray, nomeArquivo: String): Boolean = false
}
