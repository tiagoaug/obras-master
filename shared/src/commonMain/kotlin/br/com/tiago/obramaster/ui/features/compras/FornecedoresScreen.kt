package br.com.tiago.obramaster.ui.features.compras

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
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FornecedoresScreen(onVoltar: () -> Unit, viewModel: FornecedoresViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var editando by remember { mutableStateOf<FornecedorComPessoa?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Fornecedores",
        itens = uiState.fornecedores,
        filtro = { item, busca -> item.pessoa.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.pessoa.nome },
        itemSupporting = { it.fornecedor.cnpjCpf ?: "—" },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.fornecedor.pessoaId) },
        onVoltar = onVoltar,
        exportar = { itens ->
            ExportableDocument(
                titulo = "Fornecedores",
                colunas = listOf("Nome", "CNPJ/CPF", "Observações"),
                linhas = itens.map { listOf(it.pessoa.nome, it.fornecedor.cnpjCpf.orEmpty(), it.fornecedor.observacoes.orEmpty()) },
            )
        },
    )

    if (mostrarForm) {
        val atual = editando
        var pessoaId by remember { mutableStateOf(atual?.pessoa?.id) }
        var cnpjCpf by remember { mutableStateOf(atual?.fornecedor?.cnpjCpf ?: "") }
        var observacoes by remember { mutableStateOf(atual?.fornecedor?.observacoes ?: "") }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Novo fornecedor" else "Editar fornecedor", style = MaterialTheme.typography.titleMedium)

                if (atual == null) {
                    Text("Pessoa", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.pessoasDisponiveis.forEach { pessoa ->
                            FilterChip(selected = pessoaId == pessoa.id, onClick = { pessoaId = pessoa.id }, label = { Text(pessoa.nome) })
                        }
                    }
                    if (uiState.pessoasDisponiveis.isEmpty()) {
                        Text("Cadastre a pessoa em Pessoas antes de torná-la fornecedor.", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Text(atual.pessoa.nome, style = MaterialTheme.typography.bodyLarge)
                }

                OutlinedTextField(cnpjCpf, { cnpjCpf = it }, label = { Text("CNPJ/CPF (opcional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(observacoes, { observacoes = it }, label = { Text("Observações (opcional)") }, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = {
                        val idFinal = pessoaId ?: return@Button
                        viewModel.salvar(idFinal, cnpjCpf.ifBlank { null }, observacoes.ifBlank { null }, atual?.fornecedor)
                        mostrarForm = false
                    },
                    enabled = pessoaId != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
