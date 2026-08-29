package br.com.tiago.obramaster.ui.features.areaexecutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.NormaABNT
import br.com.tiago.obramaster.domain.labelPtBr
import org.koin.compose.koinInject

private enum class AbaAreaExecutor(val rotulo: String) { NORMAS("Normas"), MANUAIS("Meus Manuais") }

private sealed interface DestinoAreaExecutor {
    data object Hub : DestinoAreaExecutor
    data class DetalheNorma(val norma: NormaABNT) : DestinoAreaExecutor
}

/** SPEC_AREA_EXECUTOR.md — hub da Área do Executor. Fase 8.5: catálogo de normas ABNT.
 * Fase 8.6: biblioteca de manuais em PDF (anexar/abrir/excluir). Fase 8.7: uma única barra de
 * busca (§3) cobre as duas abas — número/título/categoria nas Normas, conteúdo extraído (FTS)
 * nos Manuais. Vínculos calculadora/etapa/material (§6, Fase 8.8) chegam na próxima sub-fase. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaExecutorHomeScreen(
    onVoltar: () -> Unit,
    normasViewModel: AreaExecutorViewModel = koinInject(),
    bibliotecaViewModel: BibliotecaManuaisViewModel = koinInject(),
) {
    var destino by remember { mutableStateOf<DestinoAreaExecutor>(DestinoAreaExecutor.Hub) }
    var aba by remember { mutableStateOf(AbaAreaExecutor.NORMAS) }
    val uiState by normasViewModel.uiState.collectAsState()

    fun buscar(query: String) {
        normasViewModel.buscar(query)
        bibliotecaViewModel.buscar(query)
    }

    when (val atual = destino) {
        DestinoAreaExecutor.Hub -> Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Área do Executor") },
                    navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = uiState.busca,
                    onValueChange = ::buscar,
                    placeholder = { Text(if (aba == AbaAreaExecutor.NORMAS) "Buscar por número, título ou categoria" else "Buscar por conteúdo dos PDFs anexados") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AbaAreaExecutor.entries.forEach { opcao ->
                        FilterChip(selected = aba == opcao, onClick = { aba = opcao }, label = { Text(opcao.rotulo) })
                    }
                }
                when (aba) {
                    AbaAreaExecutor.NORMAS -> AbaNormas(
                        uiState = uiState,
                        onAbrirNorma = { destino = DestinoAreaExecutor.DetalheNorma(it) },
                    )
                    AbaAreaExecutor.MANUAIS -> BibliotecaManuaisContent(
                        viewModel = bibliotecaViewModel,
                        normas = uiState.todasAsNormas,
                        buscaAtiva = uiState.busca.isNotBlank(),
                    )
                }
            }
        }

        is DestinoAreaExecutor.DetalheNorma -> NormaDetalheScreen(
            norma = atual.norma,
            todasAsNormas = uiState.todasAsNormas,
            onVoltar = { destino = DestinoAreaExecutor.Hub },
            onAbrirNorma = { destino = DestinoAreaExecutor.DetalheNorma(it) },
        )
    }
}

@Composable
private fun AbaNormas(
    uiState: AreaExecutorUiState,
    onAbrirNorma: (NormaABNT) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        when {
            uiState.carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.resultados.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma norma encontrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(Modifier.fillMaxWidth()) {
                items(uiState.resultados) { norma ->
                    Card(
                        onClick = { onAbrirNorma(norma) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        ListItem(
                            headlineContent = { Text("${norma.numero} — ${norma.titulo}") },
                            supportingContent = { Text(norma.categoria.labelPtBr) },
                        )
                    }
                }
            }
        }
    }
}
