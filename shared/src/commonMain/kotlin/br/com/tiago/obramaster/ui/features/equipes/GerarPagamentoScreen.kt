package br.com.tiago.obramaster.ui.features.equipes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.financeiro.FinanceEngine
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.TipoRetencao
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerarPagamentoScreen(onVoltar: () -> Unit, viewModel: GerarPagamentoViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()

    var pessoaId by remember { mutableStateOf<String?>(null) }
    var registrosPendentes by remember { mutableStateOf<List<RegistroTrabalho>>(emptyList()) }
    var selecionados by remember { mutableStateOf<Set<String>>(emptySet()) }
    var periodo by remember { mutableStateOf("") }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var contaId by remember { mutableStateOf<String?>(null) }
    var centroManualId by remember { mutableStateOf<String?>(null) }
    var retencaoAtiva by remember { mutableStateOf(false) }
    var retencoes by remember { mutableStateOf(listOf<Pair<TipoRetencao, String>>()) }

    LaunchedEffect(pessoaId) {
        selecionados = emptySet()
        registrosPendentes = pessoaId?.let { viewModel.registrosPendentes(it) } ?: emptyList()
    }

    val registrosSelecionados = registrosPendentes.filter { it.id in selecionados }
    val valorBruto = registrosSelecionados.sumOf { it.valor }
    val projetosDosSelecionados = registrosSelecionados.map { it.projetoId }.toSet()
    val projetoUnico = projetosDosSelecionados.singleOrNull()
    val centroDoProjeto = uiState.centros.firstOrNull { it.projetoId == projetoUnico }
    val centroDeCustoId = centroDoProjeto?.id ?: centroManualId
    val centrosManuais = uiState.centros.filter { it.projetoId == null }
    val retencoesValidas = retencoes.mapNotNull { (tipo, texto) -> texto.replace(',', '.').toDoubleOrNull()?.let { tipo to it } }
    val valorLiquido = valorBruto - retencoesValidas.sumOf { (_, percentual) -> FinanceEngine.calcularValorRetencao(valorBruto, percentual) }

    fun nomeProjeto(id: String) = uiState.projetos.firstOrNull { it.id == id }?.nome ?: "—"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerar Pagamento") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Funcionário", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.funcionarios.forEach { item ->
                    FilterChip(selected = pessoaId == item.pessoa.id, onClick = { pessoaId = item.pessoa.id }, label = { Text(item.pessoa.nome) })
                }
            }

            if (pessoaId != null) {
                Text("Registros pendentes (${registrosPendentes.size})", style = MaterialTheme.typography.labelLarge)
                if (registrosPendentes.isEmpty()) {
                    Text("Nenhum registro de trabalho pendente para essa pessoa.", style = MaterialTheme.typography.bodyMedium)
                }
                LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                    items(registrosPendentes) { registro ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("${DataFormatter.formatar(registro.data)} · ${nomeProjeto(registro.projetoId)}")
                                    Text("${registro.tipo.name} · ${MoneyFormatter.formatar(registro.valor)}", style = MaterialTheme.typography.labelSmall)
                                }
                                Checkbox(
                                    checked = registro.id in selecionados,
                                    onCheckedChange = { marcado ->
                                        selecionados = if (marcado) selecionados + registro.id else selecionados - registro.id
                                    },
                                )
                            }
                        }
                    }
                }

                if (registrosSelecionados.isNotEmpty()) {
                    Text("Total bruto: ${MoneyFormatter.formatar(valorBruto)}", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(periodo, { periodo = it }, label = { Text("Período (ex.: Agosto/2026)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data do pagamento (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())

                    if (centroDoProjeto == null) {
                        Text("Centro de custo", style = MaterialTheme.typography.labelLarge)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            centrosManuais.forEach { centro ->
                                FilterChip(selected = centroManualId == centro.id, onClick = { centroManualId = centro.id }, label = { Text(centro.nome) })
                            }
                        }
                    } else {
                        Text("Centro de custo: ${centroDoProjeto.nome} (do projeto)", style = MaterialTheme.typography.bodyMedium)
                    }

                    Text("Conta (obrigatório)", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.contas.forEach { conta ->
                            FilterChip(selected = contaId == conta.id, onClick = { contaId = conta.id }, label = { Text(conta.nome) })
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Retenções fiscais", style = MaterialTheme.typography.labelLarge)
                        Checkbox(checked = retencaoAtiva, onCheckedChange = { retencaoAtiva = it; if (it && retencoes.isEmpty()) retencoes = listOf(TipoRetencao.INSS to "11") })
                    }
                    if (retencaoAtiva) {
                        retencoes.forEachIndexed { indice, (tipo, texto) ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TipoRetencao.entries.forEach { opcao ->
                                        FilterChip(
                                            selected = tipo == opcao,
                                            onClick = { retencoes = retencoes.toMutableList().also { it[indice] = opcao to texto } },
                                            label = { Text(opcao.name) },
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = texto,
                                    onValueChange = { novo -> retencoes = retencoes.toMutableList().also { it[indice] = tipo to novo } },
                                    label = { Text("%") },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        OutlinedButton(onClick = { retencoes = retencoes + (TipoRetencao.INSS to "") }) { Text("+ Retenção") }
                        Text("Valor líquido a pagar: ${MoneyFormatter.formatar(valorLiquido)}", style = MaterialTheme.typography.titleSmall)
                    }

                    Button(
                        onClick = {
                            val pessoa = pessoaId ?: return@Button
                            val centro = centroDeCustoId ?: return@Button
                            val conta = contaId ?: return@Button
                            val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                            val nomePessoa = uiState.funcionarios.firstOrNull { it.pessoa.id == pessoa }?.pessoa?.nome ?: ""
                            viewModel.gerarPagamento(
                                pessoa, nomePessoa, projetoUnico, centro, periodo.ifBlank { DataFormatter.formatar(data) },
                                registrosSelecionados, data, conta, if (retencaoAtiva) retencoesValidas else emptyList(),
                            )
                            onVoltar()
                        },
                        enabled = periodo.isNotBlank() && centroDeCustoId != null && contaId != null,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Gerar pagamento") }
                }
            }
        }
    }
}
