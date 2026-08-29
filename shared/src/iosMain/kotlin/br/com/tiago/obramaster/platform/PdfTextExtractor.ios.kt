package br.com.tiago.obramaster.platform

// PDFKit (PDFDocument(data:).string) faria isso, mas é cinterop com framework Apple especializado
// que não tenho como compilar nem validar sem Mac/Xcode — mesma decisão do PdfImageRenderer.ios.kt.
// String vazia = "sem texto extraído": o documento continua listado e abrindo normalmente, só
// não entra na busca por conteúdo nesta plataforma.
actual class PdfTextExtractor {
    actual suspend fun extrairTexto(pdfBytes: ByteArray): String = ""
}
