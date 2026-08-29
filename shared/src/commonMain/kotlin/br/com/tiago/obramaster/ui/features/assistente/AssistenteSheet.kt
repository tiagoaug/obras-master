package br.com.tiago.obramaster.ui.features.assistente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.assistant.ManualSection
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistenteSheet(
    onDismiss: () -> Unit,
    onAbrirManual: (secaoId: String) -> Unit,
    viewModel: AssistenteViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Assistente de Ajuda", style = MaterialTheme.typography.titleMedium)

            val contexto = uiState.contexto
            if (contexto != null) {
                val rotulo = buildString {
                    append("📍 Contexto: ")
                    append(contexto.modulo?.labelPtBr ?: "Geral")
                    contexto.entidadeAberta?.camposChave?.get("nome")?.let { nome -> append(" • $nome") }
                }
                SuggestionChip(onClick = {}, label = { Text(rotulo) })
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.pergunta,
                    onValueChange = viewModel::atualizarPergunta,
                    label = { Text("Pergunte algo sobre o app") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = viewModel::buscar) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar no manual")
                }
            }

            Text(
                "Busca local no manual do programa — funciona sem internet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when {
                !uiState.buscou -> Unit

                uiState.resultados.isEmpty() -> Text(
                    "Não encontramos nada no manual sobre isso. Tente outras palavras.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.resultados) { secao ->
                        ResultadoManualCard(secao = secao, onAbrirManual = { onAbrirManual(secao.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultadoManualCard(secao: ManualSection, onAbrirManual: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(secao.titulo, style = MaterialTheme.typography.titleSmall)
            Text(secao.conteudo, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
            secao.exemploPratico?.let { exemplo ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF5DF))) {
                    Text(
                        "💡 Exemplo prático: $exemplo",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            TextButton(onClick = onAbrirManual) { Text("📖 Ver no Manual") }
        }
    }
}
