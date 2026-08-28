package br.com.tiago.obramaster.ui.features.financeiro

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.TipoConta
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContasScreen(onVoltar: () -> Unit, onAbrirExtrato: (String) -> Unit, viewModel: ContasViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarNovaConta by remember { mutableStateOf(false) }
    var mostrarTransferencia by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Contas",
        itens = uiState.contas,
        filtro = { conta, busca -> conta.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.nome },
        itemSupporting = { conta -> "${conta.tipo.name} · ${MoneyFormatter.formatar(uiState.saldos[conta.id] ?: 0L)}" },
        onItemClicado = { onAbrirExtrato(it.id) },
        onNovoClicado = { mostrarNovaConta = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        acoesTopBar = {
            IconButton(onClick = { mostrarTransferencia = true }) { Icon(Icons.Filled.SwapHoriz, contentDescription = "Transferir entre contas") }
        },
    )

    if (mostrarNovaConta) {
        NovaContaSheet(onSalvar = { nome, tipo, banco, agencia, numeroConta, saldoInicial, dataSaldoInicial, cor ->
            viewModel.salvarNovaConta(nome, tipo, banco, agencia, numeroConta, saldoInicial, dataSaldoInicial, cor)
            mostrarNovaConta = false
        }, onDismiss = { mostrarNovaConta = false })
    }

    if (mostrarTransferencia) {
        TransferenciaSheet(
            contas = uiState.contas,
            onConfirmar = { origemId, destinoId, valor, data, motivo ->
                viewModel.transferir(origemId, destinoId, valor, data, motivo)
                mostrarTransferencia = false
            },
            onDismiss = { mostrarTransferencia = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaContaSheet(
    onSalvar: (String, TipoConta, String?, String?, String?, Long, Long, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var nome by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoConta.CAIXA) }
    var banco by remember { mutableStateOf("") }
    var agencia by remember { mutableStateOf("") }
    var numeroConta by remember { mutableStateOf("") }
    var saldoInicial by remember { mutableStateOf(0L) }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var cor by remember { mutableStateOf("#5C6BC0") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nova conta", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())

            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoConta.entries.forEach { opcao ->
                    FilterChip(selected = tipo == opcao, onClick = { tipo = opcao }, label = { Text(opcao.name) })
                }
            }

            OutlinedTextField(banco, { banco = it }, label = { Text("Banco (opcional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(agencia, { agencia = it }, label = { Text("Agência (opcional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(numeroConta, { numeroConta = it }, label = { Text("Número da conta (opcional)") }, modifier = Modifier.fillMaxWidth())
            CalculatorTextField(valueCentavos = saldoInicial, onValueChange = { saldoInicial = it }, label = "Saldo inicial", modifier = Modifier.fillMaxWidth())
            OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data do saldo inicial (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(cor, { cor = it }, label = { Text("Cor (#RRGGBB, opcional)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                    onSalvar(nome, tipo, banco.ifBlank { null }, agencia.ifBlank { null }, numeroConta.ifBlank { null }, saldoInicial, data, cor.ifBlank { null })
                },
                enabled = nome.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferenciaSheet(
    contas: List<br.com.tiago.obramaster.domain.Conta>,
    onConfirmar: (String, String, Long, Long, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var origemId by remember { mutableStateOf<String?>(null) }
    var destinoId by remember { mutableStateOf<String?>(null) }
    var valor by remember { mutableStateOf(0L) }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var motivo by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Transferir entre contas", style = MaterialTheme.typography.titleMedium)

            Text("De", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                contas.forEach { conta ->
                    FilterChip(selected = origemId == conta.id, onClick = { origemId = conta.id }, label = { Text(conta.nome) })
                }
            }

            Text("Para", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                contas.filter { it.id != origemId }.forEach { conta ->
                    FilterChip(selected = destinoId == conta.id, onClick = { destinoId = conta.id }, label = { Text(conta.nome) })
                }
            }

            CalculatorTextField(valueCentavos = valor, onValueChange = { valor = it }, label = "Valor", modifier = Modifier.fillMaxWidth())
            OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(motivo, { motivo = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val origem = origemId ?: return@Button
                    val destino = destinoId ?: return@Button
                    val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                    onConfirmar(origem, destino, valor, data, motivo)
                },
                enabled = origemId != null && destinoId != null && valor > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Transferir") }
        }
    }
}
