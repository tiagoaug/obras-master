package br.com.tiago.obramaster.ui.features.financeiro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.TipoMovimentoConta
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtratoContaScreen(
    contaId: String,
    onVoltar: () -> Unit,
    viewModel: ExtratoContaViewModel = koinInject { parametersOf(contaId) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.conta?.nome ?: "Extrato") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.somenteNaoConciliados,
                    onClick = { viewModel.alternarFiltroConciliados() },
                    label = { Text("Só não conciliados") },
                )
            }

            LazyColumn(Modifier.fillMaxWidth()) {
                items(uiState.linhas) { (movimento, saldoCorrente) ->
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(movimento.descricao, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${DataFormatter.formatar(movimento.data)} · ${movimento.tipo.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Text("Saldo: ${MoneyFormatter.formatar(saldoCorrente)}", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                val entra = movimento.tipo == TipoMovimentoConta.RECEBIMENTO ||
                                    movimento.tipo == TipoMovimentoConta.TRANSFERENCIA_ENTRADA ||
                                    movimento.tipo == TipoMovimentoConta.AJUSTE
                                Text(
                                    "${if (entra) "+" else "-"} ${MoneyFormatter.formatar(movimento.valor)}",
                                    color = if (entra) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("Conciliado", style = MaterialTheme.typography.labelSmall)
                                    Checkbox(
                                        checked = movimento.conciliado,
                                        onCheckedChange = { viewModel.marcarConciliado(movimento.id, it) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
