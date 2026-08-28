package br.com.tiago.obramaster.ui.features.projetos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.budget.BudgetEngine
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.StatusEtapa
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjetoDetalheScreen(
    projetoId: String,
    onVoltar: () -> Unit,
    onAbrirPlanta: (String) -> Unit,
    viewModel: ProjetoDetalheViewModel = koinInject { parametersOf(projetoId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val projeto = uiState.projeto
    var mostrarFormEtapa by remember { mutableStateOf(false) }
    var etapaEditando by remember { mutableStateOf<Etapa?>(null) }
    var mostrarFormPlanta by remember { mutableStateOf(false) }

    if (projeto == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // Nenhum módulo gera gasto ainda (Compras/Equipes/Financeiro chegam nas Fases 4-6) —
    // BudgetEngine já funciona de verdade, só falta alimentar com dados reais depois.
    val gastos = emptyList<Long>()
    val gastoTotal = gastos.sum()
    val saldo = BudgetEngine.saldo(projeto.orcamentoTotal, gastos)
    val percentual = BudgetEngine.percentualConsumido(projeto.orcamentoTotal, gastos)
    val faixa = BudgetEngine.faixaOrcamento(projeto.orcamentoTotal, gastos)
    val custoM2Construcao = BudgetEngine.custoPorM2(gastoTotal, projeto.areaConstruidaM2)
    val custoM2Terreno = BudgetEngine.custoPorM2(gastoTotal, projeto.areaTerrenoM2)

    val corFaixa = when (faixa) {
        BudgetEngine.FaixaOrcamento.TRANQUILO -> Color(0xFF2E7D32)
        BudgetEngine.FaixaOrcamento.ATENCAO -> Color(0xFFF9A825)
        BudgetEngine.FaixaOrcamento.ESTOURADO -> Color(0xFFC62828)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(projeto.nome) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { etapaEditando = null; mostrarFormEtapa = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nova etapa")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Orçamento: ${MoneyFormatter.formatar(projeto.orcamentoTotal)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Gasto até agora: ${MoneyFormatter.formatar(gastoTotal)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Saldo: ${MoneyFormatter.formatar(saldo)}", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { (percentual / 100.0).toFloat().coerceIn(0f, 1f) },
                        color = corFaixa,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    Text("${percentual.toInt()}% do orçamento consumido", style = MaterialTheme.typography.labelMedium)

                    if (custoM2Construcao != null) {
                        Text(
                            "Custo/m² (área construída): ${MoneyFormatter.formatar(custoM2Construcao)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (custoM2Terreno != null) {
                        Text("Custo/m² (terreno): ${MoneyFormatter.formatar(custoM2Terreno)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Text("Etapas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))

            if (uiState.etapas.isEmpty()) {
                Button(onClick = { viewModel.aplicarTemplatePadrao() }) { Text("Aplicar template padrão de etapas") }
            }

            uiState.etapas.forEachIndexed { indice, etapa ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(etapa.nome, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${MoneyFormatter.formatar(etapa.orcamentoEtapa)} · ${etapa.progressoPercent}% · ${etapa.status.name}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        IconButton(onClick = { viewModel.moverEtapa(etapa, -1) }, enabled = indice > 0) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Mover para cima")
                        }
                        IconButton(onClick = { viewModel.moverEtapa(etapa, 1) }, enabled = indice < uiState.etapas.lastIndex) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Mover para baixo")
                        }
                        IconButton(onClick = { etapaEditando = etapa; mostrarFormEtapa = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { viewModel.excluirEtapa(etapa.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            }

            Text("Plantas Baixas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            Text(
                "Desenhe a planta pra calcular a área construída automaticamente, em vez de digitar.",
                style = MaterialTheme.typography.bodySmall,
            )

            uiState.plantas.forEach { planta ->
                Card(
                    onClick = { onAbrirPlanta(planta.id) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Text(planta.nome, Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall)
                }
            }

            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { mostrarFormPlanta = true }) { Text("Nova planta") }
                if (uiState.plantas.isNotEmpty()) {
                    Button(onClick = { viewModel.calcularEAplicarAreaDaPlanta() }) { Text("Calcular área a partir da planta") }
                }
            }
        }
    }

    if (mostrarFormEtapa) {
        EtapaFormSheet(
            existente = etapaEditando,
            onDismiss = { mostrarFormEtapa = false },
            onSalvar = { nome, orcamento, progresso, status ->
                viewModel.salvarEtapa(etapaEditando, nome, orcamento, progresso, status)
                mostrarFormEtapa = false
            },
        )
    }

    if (mostrarFormPlanta) {
        NovaPlantaDialog(
            onCriar = { nome ->
                viewModel.criarNovaPlanta(nome) { idCriado -> onAbrirPlanta(idCriado) }
                mostrarFormPlanta = false
            },
            onDispensar = { mostrarFormPlanta = false },
        )
    }
}

@Composable
private fun NovaPlantaDialog(onCriar: (String) -> Unit, onDispensar: () -> Unit) {
    var nome by remember { mutableStateOf("Pavimento Térreo") }
    Card(Modifier.padding(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nova planta baixa", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { if (nome.isNotBlank()) onCriar(nome) }) { Text("Criar e abrir") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EtapaFormSheet(
    existente: Etapa?,
    onDismiss: () -> Unit,
    onSalvar: (nome: String, orcamento: Long, progresso: Int, status: StatusEtapa) -> Unit,
) {
    var nome by remember { mutableStateOf(existente?.nome ?: "") }
    var orcamento by remember { mutableStateOf(existente?.orcamentoEtapa ?: 0L) }
    var progresso by remember { mutableStateOf((existente?.progressoPercent ?: 0).toFloat()) }
    var status by remember { mutableStateOf(existente?.status ?: StatusEtapa.NAO_INICIADA) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (existente == null) "Nova etapa" else "Editar etapa", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            CalculatorTextField(
                valueCentavos = orcamento,
                onValueChange = { orcamento = it },
                label = "Orçamento da etapa",
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Progresso: ${progresso.toInt()}%", style = MaterialTheme.typography.labelLarge)
            Slider(value = progresso, onValueChange = { progresso = it }, valueRange = 0f..100f)

            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row {
                StatusEtapa.entries.forEach { opcao ->
                    androidx.compose.material3.FilterChip(
                        selected = status == opcao,
                        onClick = { status = opcao },
                        label = { Text(opcao.name) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }

            Button(
                onClick = { onSalvar(nome, orcamento, progresso.toInt(), status) },
                enabled = nome.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar") }
        }
    }
}
