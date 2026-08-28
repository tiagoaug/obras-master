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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.TipoRegistroTrabalho
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroTrabalhoScreen(onVoltar: () -> Unit, viewModel: RegistroTrabalhoViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarForm by remember { mutableStateOf(false) }

    fun nomePessoa(id: String) = uiState.pessoas.firstOrNull { it.id == id }?.nome ?: "—"
    fun nomeProjeto(id: String) = uiState.projetos.firstOrNull { it.id == id }?.nome ?: "—"

    LcrudListScaffold(
        titulo = "Registro de Trabalho",
        itens = uiState.registros,
        filtro = { registro, busca -> nomePessoa(registro.pessoaId).contains(busca, ignoreCase = true) },
        itemHeadline = { "${nomePessoa(it.pessoaId)} — ${MoneyFormatter.formatar(it.valor)}" },
        itemSupporting = { "${DataFormatter.formatar(it.data)} · ${nomeProjeto(it.projetoId)} · ${it.tipo.name}${if (it.pago) " · Pago" else ""}" },
        onItemClicado = {},
        onNovoClicado = { mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        podeExcluir = { !it.pago },
    )

    if (mostrarForm) {
        NovoRegistroSheet(
            uiState = uiState,
            onCarregarEtapas = { projetoId -> viewModel.etapasDoProjeto(projetoId) },
            onSalvar = { pessoaId, projetoId, etapaId, data, tipo, valor, observacao ->
                viewModel.registrar(pessoaId, projetoId, etapaId, data, tipo, valor, observacao)
                mostrarForm = false
            },
            onDismiss = { mostrarForm = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoRegistroSheet(
    uiState: RegistroTrabalhoUiState,
    onCarregarEtapas: suspend (String) -> List<Etapa>,
    onSalvar: (String, String, String?, Long, TipoRegistroTrabalho, Long, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var pessoaId by remember { mutableStateOf<String?>(null) }
    var projetoId by remember { mutableStateOf<String?>(null) }
    var etapaId by remember { mutableStateOf<String?>(null) }
    var etapasDoProjeto by remember { mutableStateOf<List<Etapa>>(emptyList()) }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var tipo by remember { mutableStateOf(TipoRegistroTrabalho.DIARIA) }
    var valor by remember { mutableStateOf(0L) }
    var observacao by remember { mutableStateOf("") }

    LaunchedEffect(projetoId) {
        etapaId = null
        etapasDoProjeto = projetoId?.let { onCarregarEtapas(it) } ?: emptyList()
    }

    LaunchedEffect(pessoaId, tipo) {
        val funcionario = uiState.funcionarios.firstOrNull { it.pessoaId == pessoaId }
        if (funcionario != null && tipo == TipoRegistroTrabalho.DIARIA) valor = funcionario.valorBase
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Novo registro de trabalho", style = MaterialTheme.typography.titleMedium)

            Text("Funcionário", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.funcionarios.forEach { funcionario ->
                    val nome = uiState.pessoas.firstOrNull { it.id == funcionario.pessoaId }?.nome ?: funcionario.pessoaId
                    FilterChip(selected = pessoaId == funcionario.pessoaId, onClick = { pessoaId = funcionario.pessoaId }, label = { Text(nome) })
                }
            }
            if (uiState.funcionarios.isEmpty()) {
                Text("Cadastre um funcionário antes de registrar trabalho.", style = MaterialTheme.typography.labelSmall)
            }

            Text("Projeto", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.projetos.forEach { projeto ->
                    FilterChip(selected = projetoId == projeto.id, onClick = { projetoId = projeto.id }, label = { Text(projeto.nome) })
                }
            }

            if (etapasDoProjeto.isNotEmpty()) {
                Text("Etapa (opcional)", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = etapaId == null, onClick = { etapaId = null }, label = { Text("Nenhuma") })
                    etapasDoProjeto.forEach { etapa ->
                        FilterChip(selected = etapaId == etapa.id, onClick = { etapaId = etapa.id }, label = { Text(etapa.nome) })
                    }
                }
            }

            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoRegistroTrabalho.entries.forEach { opcao ->
                    FilterChip(selected = tipo == opcao, onClick = { tipo = opcao }, label = { Text(opcao.name) })
                }
            }

            OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            CalculatorTextField(valueCentavos = valor, onValueChange = { valor = it }, label = "Valor", modifier = Modifier.fillMaxWidth())
            OutlinedTextField(observacao, { observacao = it }, label = { Text("Observação (opcional)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val pessoa = pessoaId ?: return@Button
                    val projeto = projetoId ?: return@Button
                    val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                    onSalvar(pessoa, projeto, etapaId, data, tipo, valor, observacao.ifBlank { null })
                },
                enabled = pessoaId != null && projetoId != null && valor > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Registrar") }
        }
    }
}
