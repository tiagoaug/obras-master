package br.com.tiago.obramaster.ui.features.cadastros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaisScreen(onVoltar: () -> Unit, viewModel: MateriaisViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var editando by remember { mutableStateOf<Material?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Materiais",
        itens = uiState.materiais,
        filtro = { material, busca -> material.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.nome },
        itemSupporting = { material ->
            buildString {
                append(material.unidadePadrao)
                material.precoReferencia?.let { append(" · ${MoneyFormatter.formatar(it)}") }
            }
        },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
    )

    if (mostrarForm) {
        val atual = editando
        var nome by remember { mutableStateOf(atual?.nome ?: "") }
        var unidade by remember { mutableStateOf(atual?.unidadePadrao ?: "") }
        var categoria by remember { mutableStateOf(atual?.categoria ?: "") }
        var precoCentavos by remember { mutableStateOf(atual?.precoReferencia ?: 0L) }
        var corSelecionadaId by remember { mutableStateOf(atual?.corId) }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Novo material" else "Editar material", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(unidade, { unidade = it }, label = { Text("Unidade padrão (ex.: m², kg, sc)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(categoria, { categoria = it }, label = { Text("Categoria (opcional)") }, modifier = Modifier.fillMaxWidth())
                CalculatorTextField(
                    valueCentavos = precoCentavos,
                    onValueChange = { precoCentavos = it },
                    label = "Preço de referência (opcional)",
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.cores.isNotEmpty()) {
                    Text("Cor (opcional)", style = MaterialTheme.typography.labelLarge)
                    Row {
                        FilterChip(
                            selected = corSelecionadaId == null,
                            onClick = { corSelecionadaId = null },
                            label = { Text("Nenhuma") },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        uiState.cores.forEach { cor ->
                            FilterChip(
                                selected = corSelecionadaId == cor.id,
                                onClick = { corSelecionadaId = cor.id },
                                label = { Text(cor.nome) },
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.salvar(
                            existente = atual,
                            nome = nome,
                            unidadePadrao = unidade,
                            precoReferencia = precoCentavos.takeIf { it > 0 },
                            categoria = categoria.ifBlank { null },
                            corId = corSelecionadaId,
                        )
                        mostrarForm = false
                    },
                    enabled = nome.isNotBlank() && unidade.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
