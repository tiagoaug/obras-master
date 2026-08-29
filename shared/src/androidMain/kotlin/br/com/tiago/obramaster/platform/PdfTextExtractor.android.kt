package br.com.tiago.obramaster.platform

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private var pdfBoxInicializado = false

/** Mesma lib pdfbox-android já usada no PdfVectorExtractor — PDFTextStripper é o utilitário
 * padrão do PdfBox pra extrair todo o texto de um documento, página por página. */
actual class PdfTextExtractor(private val context: Context) {

    actual suspend fun extrairTexto(pdfBytes: ByteArray): String = withContext(Dispatchers.Default) {
        if (!pdfBoxInicializado) {
            PDFBoxResourceLoader.init(context.applicationContext)
            pdfBoxInicializado = true
        }
        runCatching {
            PDDocument.load(pdfBytes).use { documento -> PDFTextStripper().getText(documento) }
        }.getOrDefault("")
    }
}
