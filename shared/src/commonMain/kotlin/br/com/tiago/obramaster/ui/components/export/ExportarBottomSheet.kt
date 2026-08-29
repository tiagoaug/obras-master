package br.com.tiago.obramaster.ui.components.export

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.export.PdfWriter
import br.com.tiago.obramaster.core.export.ReportCanvasRenderer
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.platform.FileExporter
import br.com.tiago.obramaster.platform.paraBytesJpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

private enum class FormatoExportacao(val extensao: String, val mimeType: String, val rotulo: String) {
    JPG("jpg", "image/jpeg", "Compartilhar como JPG"),
    PDF("pdf", "application/pdf", "Compartilhar como PDF"),
}

/** SPEC_OBRA_MASTER.md §5.1 — botão de exportar padrão. Fase 9.2: JPG (via ReportCanvasRenderer).
 * Fase 9.3: PDF (via PdfWriter, escrito do zero em commonMain). XLSX chega na Fase 9.4. O preview
 * visual em tela (ReportPreview, Composable normal) e os bytes exportados (ReportCanvasRenderer/
 * PdfWriter) são desenhos independentes do mesmo ExportableDocument, não uma captura um do outro
 * — ver a nota de decisão em ReportCanvasRenderer.kt sobre por que a captura de Composable não
 * está disponível na versão de Compose deste projeto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportarBottomSheet(doc: ExportableDocument, onDismiss: () -> Unit, fileExporter: FileExporter = koinInject()) {
    var disponivel by remember { mutableStateOf(true) }
    var formatoExportando by remember { mutableStateOf<FormatoExportacao?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { disponivel = fileExporter.isAvailable() }

    fun exportar(formato: FormatoExportacao) {
        formatoExportando = formato
        erro = null
        scope.launch {
            val bytes = withContext(Dispatchers.Default) {
                when (formato) {
                    FormatoExportacao.JPG -> ReportCanvasRenderer.renderizar(doc).paraBytesJpeg()
                    FormatoExportacao.PDF -> PdfWriter.escrever(doc)
                }
            }
            val nomeArquivo = "${doc.titulo.replace(Regex("[^A-Za-z0-9]+"), "_")}.${formato.extensao}"
            val sucesso = fileExporter.compartilhar(nomeArquivo, bytes, formato.mimeType)
            formatoExportando = null
            if (sucesso) onDismiss() else erro = "Não foi possível compartilhar o arquivo."
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Exportar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))

            Column(Modifier.horizontalScroll(rememberScrollState())) {
                ReportPreview(doc = doc, modifier = Modifier.width(480.dp))
            }

            if (!disponivel) {
                Text(
                    "Compartilhar ainda não está disponível nesta plataforma.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            erro?.let { mensagem ->
                Text(mensagem, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { exportar(FormatoExportacao.JPG) },
                    enabled = disponivel && formatoExportando == null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (formatoExportando == FormatoExportacao.JPG) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp)) else Text("Compartilhar como JPG")
                }
                OutlinedButton(
                    onClick = { exportar(FormatoExportacao.PDF) },
                    enabled = disponivel && formatoExportando == null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (formatoExportando == FormatoExportacao.PDF) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp)) else Text("Compartilhar como PDF")
                }
            }
        }
    }
}
