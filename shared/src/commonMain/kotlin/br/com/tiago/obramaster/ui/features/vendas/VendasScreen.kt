package br.com.tiago.obramaster.ui.features.vendas

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.Venda
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendasScreen(onVoltar: () -> Unit, viewModel: VendasViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarForm by remember { mutableStateOf(false) }
    var vendaAberta by remember { mutableStateOf<Venda?>(null) }

    fun nomeCliente(id: String) = uiState.clientes.firstOrNull { it.id == id }?.nome ?: "—"

    LcrudListScaffold(
        titulo = "Vendas",
        itens = uiState.vendas,
        filtro = { venda, busca -> venda.descricao.contains(busca, ignoreCase = true) || nomeCliente(venda.clientePessoaId).contains(busca, ignoreCase = true) },
        itemHeadline = { it.descricao },
        itemSupporting = { "${it.status.name} · ${nomeCliente(it.clientePessoaId)} · ${MoneyFormatter.formatar(it.valorTotal)}" },
        onItemClicado = { vendaAberta = it },
        onNovoClicado = { mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        podeExcluir = { it.status == StatusVenda.NEGOCIACAO },
    )

    if (mostrarForm) {
        NovaVendaScreen(
            uiState = uiState,
            onSalvar = { clienteId, projetoId, descricao, data, formaPagamento, parcelas ->
                viewModel.salvar(clienteId, projetoId, descricao, data, formaPagamento, parcelas)
                mostrarForm = false
            },
            onVoltar = { mostrarForm = false },
        )
    }

    vendaAberta?.let { venda ->
        DetalheVendaSheet(
            venda = venda,
            nomeCliente = nomeCliente(venda.clientePessoaId),
            nomeProjeto = uiState.projetos.firstOrNull { it.id == venda.projetoId }?.nome,
            contas = uiState.contas,
            carregarParcelas = { viewModel.parcelasDaVenda(venda.id) },
            onFechar = { viewModel.fechar(venda) },
            onCancelar = { viewModel.cancelar(venda); vendaAberta = null },
            onReceberParcela = { parcela, contaId -> viewModel.receberParcela(venda, parcela, contaId) },
            onDismiss = { vendaAberta = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalheVendaSheet(
    venda: Venda,
    nomeCliente: String,
    nomeProjeto: String?,
    contas: List<Conta>,
    carregarParcelas: suspend () -> List<ParcelaVenda>,
    onFechar: () -> Unit,
    onCancelar: () -> Unit,
    onReceberParcela: (ParcelaVenda, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var parcelas by remember { mutableStateOf<List<ParcelaVenda>>(emptyList()) }
    var parcelaParaReceber by remember { mutableStateOf<ParcelaVenda?>(null) }
    LaunchedEffect(venda.id, venda.status) { parcelas = carregarParcelas() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(venda.descricao, style = MaterialTheme.typography.titleMedium)
            Text("Cliente: $nomeCliente", style = MaterialTheme.typography.bodyMedium)
            nomeProjeto?.let { Text("Projeto: $it", style = MaterialTheme.typography.bodyMedium) }
            Text("Data: ${DataFormatter.formatar(venda.data)} · ${venda.formaPagamento}", style = MaterialTheme.typography.bodyMedium)
            Text("Total: ${MoneyFormatter.formatar(venda.valorTotal)}", style = MaterialTheme.typography.titleSmall)

            when (venda.status) {
                StatusVenda.NEGOCIACAO -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onFechar) { Text("Fechar venda") }
                    OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
                }
                StatusVenda.FECHADA -> Text("Status: Fechada", style = MaterialTheme.typography.labelLarge)
                StatusVenda.CANCELADA -> Text("Status: Cancelada", style = MaterialTheme.typography.labelLarge)
            }

            HorizontalDivider()
            Text("Parcelas", style = MaterialTheme.typography.labelLarge)
            parcelas.forEach { parcela ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Parcela ${parcela.numero} · vence ${DataFormatter.formatar(parcela.vencimento)}")
                        Text(if (parcela.pago) "Recebida" else "Pendente", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(MoneyFormatter.formatar(parcela.valor))
                        if (!parcela.pago && parcela.lancamentoFinanceiroId != null) {
                            OutlinedButton(onClick = { parcelaParaReceber = parcela }) { Text("Receber") }
                        }
                    }
                }
            }
        }
    }

    parcelaParaReceber?.let { parcela ->
        ReceberParcelaSheet(
            contas = contas,
            onConfirmar = { contaId -> onReceberParcela(parcela, contaId); parcelaParaReceber = null },
            onDismiss = { parcelaParaReceber = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceberParcelaSheet(
    contas: List<Conta>,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var contaId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Receber em qual conta?", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                contas.forEach { conta ->
                    FilterChip(selected = contaId == conta.id, onClick = { contaId = conta.id }, label = { Text(conta.nome) })
                }
            }
            Button(
                onClick = { contaId?.let(onConfirmar) },
                enabled = contaId != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Confirmar recebimento") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaVendaScreen(
    uiState: VendasUiState,
    onSalvar: (String, String?, String, Long, String, List<ParcelaVenda>) -> Unit,
    onVoltar: () -> Unit,
) {
    var clienteId by remember { mutableStateOf<String?>(null) }
    var projetoId by remember { mutableStateOf<String?>(null) }
    var descricao by remember { mutableStateOf("") }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var formaPagamento by remember { mutableStateOf("") }
    var parcelas by remember { mutableStateOf<List<ParcelaVenda>>(emptyList()) }
    var mostrarNovaParcela by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Venda") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(descricao, { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())

            Text("Cliente", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.clientes.forEach { pessoa ->
                    FilterChip(selected = clienteId == pessoa.id, onClick = { clienteId = pessoa.id }, label = { Text(pessoa.nome) })
                }
            }

            Text("Projeto (opcional)", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = projetoId == null, onClick = { projetoId = null }, label = { Text("Nenhum") })
                uiState.projetos.forEach { projeto ->
                    FilterChip(selected = projetoId == projeto.id, onClick = { projetoId = projeto.id }, label = { Text(projeto.nome) })
                }
            }

            OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(formaPagamento, { formaPagamento = it }, label = { Text("Forma de pagamento") }, modifier = Modifier.fillMaxWidth())

            HorizontalDivider()
            Text(
                "Parcelas (${parcelas.size}) — total ${MoneyFormatter.formatar(parcelas.sumOf { it.valor })}",
                style = MaterialTheme.typography.labelLarge,
            )
            LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(parcelas) { parcela ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Parcela ${parcela.numero} · vence ${DataFormatter.formatar(parcela.vencimento)}")
                            Text(MoneyFormatter.formatar(parcela.valor))
                        }
                    }
                }
            }
            OutlinedButton(onClick = { mostrarNovaParcela = true }) { Text("+ Parcela") }

            Button(
                onClick = {
                    val cliente = clienteId ?: return@Button
                    val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                    onSalvar(cliente, projetoId, descricao, data, formaPagamento, parcelas)
                },
                enabled = clienteId != null && descricao.isNotBlank() && parcelas.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar venda") }
        }
    }

    if (mostrarNovaParcela) {
        NovaParcelaSheet(
            proximoNumero = parcelas.size + 1,
            onAdicionar = { parcela -> parcelas = parcelas + parcela; mostrarNovaParcela = false },
            onDismiss = { mostrarNovaParcela = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun NovaParcelaSheet(
    proximoNumero: Int,
    onAdicionar: (ParcelaVenda) -> Unit,
    onDismiss: () -> Unit,
) {
    var vencimentoTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var valor by remember { mutableStateOf(0L) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parcela $proximoNumero", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(vencimentoTexto, { vencimentoTexto = it }, label = { Text("Vencimento (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            CalculatorTextField(valueCentavos = valor, onValueChange = { valor = it }, label = "Valor", modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val vencimento = DataFormatter.parseOuNulo(vencimentoTexto) ?: Clock.System.now().toEpochMilliseconds()
                    onAdicionar(
                        ParcelaVenda(id = Uuid.random().toString(), vendaId = "", numero = proximoNumero, valor = valor, vencimento = vencimento),
                    )
                },
                enabled = valor > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Adicionar") }
        }
    }
}
