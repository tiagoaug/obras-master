package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef

// PDFKit consegue renderizar uma página de PDF (CGPDFDocument + UIGraphicsImageRenderer), mas é
// cinterop com framework Apple que não tenho como compilar nem validar sem Mac/Xcode — mesma
// decisão do ImagePicker.ios.kt/ContactsProvider.ios.kt. isAvailable() = false por enquanto: o
// botão de importar PDF some da UI no iOS; DXF, SVG e desenho manual continuam funcionando.
actual class PdfImageRenderer {
    actual suspend fun isAvailable(): Boolean = false
    actual suspend fun renderizarPrimeiraPagina(pdfBytes: ByteArray): ImageRef? = null
}
