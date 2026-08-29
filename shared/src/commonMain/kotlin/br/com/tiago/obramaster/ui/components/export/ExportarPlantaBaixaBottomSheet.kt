package br.com.tiago.obramaster.ui.components.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import br.com.tiago.obramaster.core.export.PlantaBaixaExportModel
import br.com.tiago.obramaster.core.export.ReportCanvasRenderer
import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.platform.FileExporter
import br.com.tiago.obramaster.platform.paraBytesJpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

private enum class FormatoExportacaoPlanta(val extensao: String, val mimeType: String) { JPG("jpg", "image/jpeg"), PDF("pdf", "application/pdf") }

private const val LARGURA_JPG = 1240.0
private const val ALTURA_JPG = LARGURA_JPG * 0.75
private const val LARGURA_PDF = 595.0 // A4 retrato em pt
private const val ALTURA_PDF = 842.0
private const val MARGEM = 32.0

/** SPEC_PLANTA_BAIXA.md §6 (Fase 9.5) — exporta a planta baixa em JPG/PDF, reaproveitando
 * PlantaBaixaExportModel (transformação de coordenadas) + ReportCanvasRenderer/PdfWriter (mesmos
 * renderers já usados pelo ExportarBottomSheet padrão). Inverte Y só na saída PDF, porque o
 * sistema de coordenadas do PDF é bottom-up e o do editor/Canvas é top-down. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportarPlantaBaixaBottomSheet(
    titulo: String,
    comodos: List<Comodo>,
    paredes: List<Parede>,
    escalaPxPorMetro: Double,
    onDismiss: () -> Unit,
    fileExporter: FileExporter = koinInject(),
) {
    var disponivel by remember { mutableStateOf(true) }
    var formatoExportando by remember { mutableStateOf<FormatoExportacaoPlanta?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { disponivel = fileExporter.isAvailable() }

    fun exportar(formato: FormatoExportacaoPlanta) {
        formatoExportando = formato
        erro = null
        scope.launch {
            val bytes = withContext(Dispatchers.Default) {
                when (formato) {
                    FormatoExportacaoPlanta.JPG -> {
                        val desenho = PlantaBaixaExportModel.montar(comodos, paredes, escalaPxPorMetro, LARGURA_JPG, ALTURA_JPG, MARGEM, inverterY = false)
                        ReportCanvasRenderer.renderizarPlantaBaixa(titulo, desenho, LARGURA_JPG.toFloat(), ALTURA_JPG.toFloat()).paraBytesJpeg()
                    }
                    FormatoExportacaoPlanta.PDF -> {
                        val desenho = PlantaBaixaExportModel.montar(comodos, paredes, escalaPxPorMetro, LARGURA_PDF, ALTURA_PDF, MARGEM, inverterY = true)
                        PdfWriter.escreverPlantaBaixa(titulo, desenho)
                    }
                }
            }
            val nomeArquivo = "${titulo.replace(Regex("[^A-Za-z0-9]+"), "_")}.${formato.extensao}"
            val sucesso = fileExporter.compartilhar(nomeArquivo, bytes, formato.mimeType)
            formatoExportando = null
            if (sucesso) onDismiss() else erro = "Não foi possível compartilhar o arquivo."
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Exportar planta baixa", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))

            if (!disponivel) {
                Text(
                    "Compartilhar ainda não está disponível nesta plataforma.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            erro?.let { mensagem ->
                Text(mensagem, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { exportar(FormatoExportacaoPlanta.JPG) },
                    enabled = disponivel && formatoExportando == null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (formatoExportando == FormatoExportacaoPlanta.JPG) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp)) else Text("Compartilhar como JPG")
                }
                OutlinedButton(
                    onClick = { exportar(FormatoExportacaoPlanta.PDF) },
                    enabled = disponivel && formatoExportando == null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (formatoExportando == FormatoExportacaoPlanta.PDF) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp)) else Text("Compartilhar como PDF")
                }
            }
        }
    }
}
