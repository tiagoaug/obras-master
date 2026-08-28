package br.com.tiago.obramaster.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import br.com.tiago.obramaster.domain.ImageRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** android.graphics.pdf.PdfRenderer é nativo do SDK (API 21+) — sem dependência nova. */
actual class PdfImageRenderer(private val context: Context) {

    actual suspend fun isAvailable(): Boolean = true

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun renderizarPrimeiraPagina(pdfBytes: ByteArray): ImageRef? = withContext(Dispatchers.IO) {
        val arquivoTemporario = File(context.cacheDir, "importacao_${Uuid.random()}.pdf")
        try {
            arquivoTemporario.writeBytes(pdfBytes)
            ParcelFileDescriptor.open(arquivoTemporario, ParcelFileDescriptor.MODE_READ_ONLY).use { descritor ->
                PdfRenderer(descritor).use { renderizador ->
                    if (renderizador.pageCount == 0) return@withContext null
                    renderizador.openPage(0).use { pagina ->
                        val bitmap = Bitmap.createBitmap(pagina.width * 2, pagina.height * 2, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        ImageRef(bitmap.paraJpegBytes())
                    }
                }
            }
        } finally {
            arquivoTemporario.delete()
        }
    }

    private fun Bitmap.paraJpegBytes(qualidade: Int = 85): ByteArray {
        val saida = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, qualidade, saida)
        return saida.toByteArray()
    }
}
