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
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.TipoContratacao
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuncionariosScreen(onVoltar: () -> Unit, viewModel: FuncionariosViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var editando by remember { mutableStateOf<FuncionarioComPessoa?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Funcionários",
        itens = uiState.funcionarios,
        filtro = { item, busca -> item.pessoa.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.pessoa.nome },
        itemSupporting = { "${it.funcionario.funcao} · ${it.funcionario.tipoContratacao.name} · ${MoneyFormatter.formatar(it.funcionario.valorBase)}" },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.funcionario.pessoaId) },
        onVoltar = onVoltar,
    )

    if (mostrarForm) {
        val atual = editando
        var pessoaId by remember { mutableStateOf(atual?.pessoa?.id) }
        var funcao by remember { mutableStateOf(atual?.funcionario?.funcao ?: "") }
        var tipoContratacao by remember { mutableStateOf(atual?.funcionario?.tipoContratacao ?: TipoContratacao.DIARIA) }
        var valorBase by remember { mutableStateOf(atual?.funcionario?.valorBase ?: 0L) }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Novo funcionário" else "Editar funcionário", style = MaterialTheme.typography.titleMedium)

                if (atual == null) {
                    Text("Pessoa", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.pessoasDisponiveis.forEach { pessoa ->
                            FilterChip(selected = pessoaId == pessoa.id, onClick = { pessoaId = pessoa.id }, label = { Text(pessoa.nome) })
                        }
                    }
                    if (uiState.pessoasDisponiveis.isEmpty()) {
                        Text("Cadastre a pessoa em Pessoas antes de torná-la funcionário.", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Text(atual.pessoa.nome, style = MaterialTheme.typography.bodyLarge)
                }

                OutlinedTextField(funcao, { funcao = it }, label = { Text("Função") }, modifier = Modifier.fillMaxWidth())

                Text("Tipo de contratação", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoContratacao.entries.forEach { opcao ->
                        FilterChip(selected = tipoContratacao == opcao, onClick = { tipoContratacao = opcao }, label = { Text(opcao.name) })
                    }
                }

                CalculatorTextField(
                    valueCentavos = valorBase,
                    onValueChange = { valorBase = it },
                    label = when (tipoContratacao) {
                        TipoContratacao.DIARIA -> "Valor da diária"
                        TipoContratacao.EMPREITADA -> "Valor de referência da empreitada"
                        TipoContratacao.MENSAL -> "Valor mensal"
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        val idFinal = pessoaId ?: return@Button
                        viewModel.salvar(idFinal, funcao, tipoContratacao, valorBase, atual?.funcionario)
                        mostrarForm = false
                    },
                    enabled = pessoaId != null && funcao.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
