package br.com.tiago.obramaster.ui.features.equipes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import br.com.tiago.obramaster.domain.Equipe
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipesScreen(onVoltar: () -> Unit, viewModel: EquipesViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var editando by remember { mutableStateOf<Equipe?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    fun nomePessoa(id: String?) = uiState.pessoas.firstOrNull { it.id == id }?.nome

    LcrudListScaffold(
        titulo = "Equipes",
        itens = uiState.equipes,
        filtro = { equipe, busca -> equipe.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.nome },
        itemSupporting = { "${it.membrosIds.size} membro(s)" + (nomePessoa(it.liderPessoaId)?.let { lider -> " · Líder: $lider" } ?: "") },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        exportar = { itens ->
            ExportableDocument(
                titulo = "Equipes",
                colunas = listOf("Nome", "Membros", "Líder"),
                linhas = itens.map { listOf(it.nome, it.membrosIds.size.toString(), nomePessoa(it.liderPessoaId).orEmpty()) },
            )
        },
    )

    if (mostrarForm) {
        val atual = editando
        var nome by remember { mutableStateOf(atual?.nome ?: "") }
        var liderPessoaId by remember { mutableStateOf(atual?.liderPessoaId) }
        var membrosIds by remember { mutableStateOf(atual?.membrosIds ?: emptySet()) }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Nova equipe" else "Editar equipe", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome da equipe") }, modifier = Modifier.fillMaxWidth())

                Text("Membros", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.pessoas.forEach { pessoa ->
                        FilterChip(
                            selected = pessoa.id in membrosIds,
                            onClick = { membrosIds = if (pessoa.id in membrosIds) membrosIds - pessoa.id else membrosIds + pessoa.id },
                            label = { Text(pessoa.nome) },
                        )
                    }
                }

                Text("Líder (opcional)", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = liderPessoaId == null, onClick = { liderPessoaId = null }, label = { Text("Nenhum") })
                    uiState.pessoas.filter { it.id in membrosIds }.forEach { pessoa ->
                        FilterChip(selected = liderPessoaId == pessoa.id, onClick = { liderPessoaId = pessoa.id }, label = { Text(pessoa.nome) })
                    }
                }

                Button(
                    onClick = { viewModel.salvar(atual, nome, liderPessoaId, membrosIds); mostrarForm = false },
                    enabled = nome.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
