package br.com.tiago.obramaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.data.repository.NormaABNTRepository
import br.com.tiago.obramaster.domain.NormaABNT
import org.koin.compose.koinInject

/** SPEC_AREA_EXECUTOR.md §6 (Fase 8.8) — carrega o catálogo de normas uma vez, pra qualquer tela
 * de outro módulo filtrar e mostrar um ícone "📘 Base normativa" sem precisar navegar pra Área
 * do Executor. */
@Composable
fun rememberNormasCatalogo(repository: NormaABNTRepository = koinInject()): List<NormaABNT> {
    var normas by remember { mutableStateOf<List<NormaABNT>>(emptyList()) }
    LaunchedEffect(Unit) { normas = repository.listarTodas() }
    return normas
}

/** Ícone que só aparece quando há normas aplicáveis — toca e abre um resumo sem sair da tela atual. */
@Composable
fun BaseNormativaIcon(normas: List<NormaABNT>, titulo: String = "Base normativa") {
    if (normas.isEmpty()) return
    var mostrarDialogo by remember { mutableStateOf(false) }
    SuggestionChip(onClick = { mostrarDialogo = true }, label = { Text("📘 Base normativa") })
    if (mostrarDialogo) {
        NormaResumoDialog(titulo = titulo, normas = normas, onDismiss = { mostrarDialogo = false })
    }
}

@Composable
fun NormaResumoDialog(titulo: String, normas: List<NormaABNT>, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                normas.forEachIndexed { indice, norma ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${norma.numero} — ${norma.titulo}", style = MaterialTheme.typography.titleSmall)
                        Text(norma.escopoResumo, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { uriHandler.openUri(norma.urlCatalogoOficial) }) {
                            Text("Ver no Catálogo Oficial")
                        }
                    }
                    if (indice < normas.lastIndex) HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}
