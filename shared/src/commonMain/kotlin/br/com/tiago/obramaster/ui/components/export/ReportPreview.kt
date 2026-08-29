package br.com.tiago.obramaster.ui.components.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.ExportableDocument

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §1 — mesmo Composable serve de preview em tela cheia
 * antes de exportar E de fonte pro ExportEngine capturar como JPG (ver JpegExportCapture.kt) —
 * sem lógica duplicada, como a spec pede. Fundo branco fixo (Color.White, não MaterialTheme)
 * porque o resultado é um "documento" pra compartilhar, não deve variar com o tema escuro. */
@Composable
fun ReportPreview(doc: ExportableDocument, modifier: Modifier = Modifier) {
    Column(modifier.background(Color.White).padding(16.dp)) {
        doc.empresa?.let { empresa ->
            Text(empresa.nome, color = Color.Black, style = MaterialTheme.typography.titleMedium)
        }
        Text(doc.titulo, color = Color.Black, style = MaterialTheme.typography.headlineSmall)
        doc.subtitulo?.let { Text(it, color = Color(0xFF444444), style = MaterialTheme.typography.bodyMedium) }

        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFFCCCCCC))

        Row(Modifier.fillMaxWidth()) {
            doc.colunas.forEach { coluna ->
                Text(coluna, color = Color.Black, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            }
        }
        HorizontalDivider(color = Color(0xFFCCCCCC))

        doc.linhas.forEach { linha ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                linha.forEach { valor ->
                    Text(valor, color = Color.Black, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }

        if (doc.resumo.isNotEmpty()) {
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFFCCCCCC))
            doc.resumo.forEach { (rotulo, valor) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(rotulo, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                    Text(valor, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        doc.rodape?.let {
            Text(it, color = Color(0xFF666666), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
