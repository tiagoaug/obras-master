package br.com.tiago.obramaster.platform

import android.content.Context
import android.provider.OpenableColumns
import br.com.tiago.obramaster.domain.ArquivoSelecionado

// ACTION_OPEN_DOCUMENT não filtra bem por extensão de arquivo (DXF/SVG não têm MIME padronizado
// e confiável em todo Android) — aceita qualquer arquivo (mime genérico) e deixa a checagem de
// extensão pro chamador (PlantaBaixaViewModel.importarArquivo), que já sabe o que faz sentido.
actual class FilePicker(private val context: Context) {

    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun escolherArquivo(extensoesAceitas: List<String>): ArquivoSelecionado? {
        val uri = FilePickerBridge.escolherArquivo(arrayOf("*/*")) ?: return null
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val nome = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: uri.lastPathSegment ?: "arquivo"
        return ArquivoSelecionado(nomeArquivo = nome, bytes = bytes)
    }
}
