package br.com.tiago.obramaster.ui.features.financeiro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.financeiro.FinanceEngine
import br.com.tiago.obramaster.core.financeiro.MesAno
import br.com.tiago.obramaster.core.financeiro.PeriodoPreset
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.CentroDeCusto
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private val CORES_GRAFICO = listOf(
    Color(0xFF5C6BC0), Color(0xFFEF5350), Color(0xFFFFCA28), Color(0xFF66BB6A),
    Color(0xFF26C6DA), Color(0xFFAB47BC), Color(0xFFFF7043), Color(0xFF8D6E63),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceiroDashboardScreen(
    onVoltar: () -> Unit,
    onAbrirLancamentos: () -> Unit,
    onAbrirCategorias: () -> Unit,
    onAbrirCentrosDeCusto: () -> Unit,
    viewModel: FinanceiroDashboardViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financeiro") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    IconButton(onClick = onAbrirLancamentos) { Icon(Icons.Filled.Receipt, contentDescription = "Lançamentos") }
                    IconButton(onClick = onAbrirCategorias) { Icon(Icons.Filled.Sell, contentDescription = "Categorias") }
                    IconButton(onClick = onAbrirCentrosDeCusto) { Icon(Icons.Filled.AccountTree, contentDescription = "Centros de Custo") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Período", style = MaterialTheme.typography.labelLarge)
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = uiState.filtro.periodoInicio == null,
                    onClick = { viewModel.atualizarFiltro(uiState.filtro.copy(periodoInicio = null, periodoFim = null)) },
                    label = { Text("Tudo") },
                )
                listOf(PeriodoPreset.HOJE to "Hoje", PeriodoPreset.SEMANA to "Semana", PeriodoPreset.MES to "Mês", PeriodoPreset.ANO to "Ano").forEach { (preset, rotulo) ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val (inicio, fim) = FinanceEngine.periodoPreset(preset, Clock.System.now().toEpochMilliseconds())
                            viewModel.atualizarFiltro(uiState.filtro.copy(periodoInicio = inicio, periodoFim = fim))
                        },
                        label = { Text(rotulo) },
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CartaoResumo("Receitas", uiState.totalReceitas, Color(0xFF2E7D32), Modifier.weight(1f))
                CartaoResumo("Despesas", uiState.totalDespesas, Color(0xFFC62828), Modifier.weight(1f))
            }
            CartaoResumo("Lucro", uiState.lucro, if (uiState.lucro >= 0) Color(0xFF2E7D32) else Color(0xFFC62828), Modifier.fillMaxWidth().padding(top = 8.dp))

            Text("Despesas por categoria", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
            PieChartComCategorias(uiState.porCategoria)

            Text("Receita x Despesa por mês", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
            BarChartPorMes(uiState.porMes)

            Text("Evolução do lucro", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
            LineChartEvolucao(uiState.evolucaoLucro)

            Text("Resultado por Centro de Custo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
            uiState.resultadoPorCentro.forEach { (centro, resultado) ->
                Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(centro.nome)
                        Text(MoneyFormatter.formatar(resultado), color = if (resultado >= 0) Color(0xFF2E7D32) else Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoResumo(titulo: String, valor: Long, cor: Color, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            Text(MoneyFormatter.formatar(valor), style = MaterialTheme.typography.titleLarge, color = cor)
        }
    }
}

@Composable
private fun PieChartComCategorias(dados: Map<CategoriaFinanceira, Long>) {
    val entradas = dados.entries.filter { it.value > 0 }.sortedByDescending { it.value }
    if (entradas.isEmpty()) {
        Text("Sem despesas no período.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val total = entradas.sumOf { it.value }.toFloat()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(140.dp)) {
            var anguloInicial = -90f
            entradas.forEachIndexed { indice, entrada ->
                val angulo = 360f * (entrada.value / total)
                drawArc(color = CORES_GRAFICO[indice % CORES_GRAFICO.size], startAngle = anguloInicial, sweepAngle = angulo, useCenter = true)
                anguloInicial += angulo
            }
        }
        Column(Modifier.padding(start = 12.dp)) {
            entradas.forEachIndexed { indice, entrada ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(Modifier.size(10.dp).background(CORES_GRAFICO[indice % CORES_GRAFICO.size]))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${entrada.key.nome} — ${(entrada.value / total * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChartPorMes(dados: Map<MesAno, Pair<Long, Long>>) {
    val entradas = dados.entries.sortedBy { it.key }
    if (entradas.isEmpty()) {
        Text("Sem lançamentos no período.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val maiorValor = entradas.maxOf { maxOf(it.value.first, it.value.second) }.coerceAtLeast(1L).toFloat()
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val larguraGrupo = size.width / entradas.size
        entradas.forEachIndexed { indice, entrada ->
            val xBase = indice * larguraGrupo
            val alturaReceita = (entrada.value.first / maiorValor) * size.height
            val alturaDespesa = (entrada.value.second / maiorValor) * size.height
            drawRect(
                color = Color(0xFF66BB6A),
                topLeft = Offset(xBase + 4, size.height - alturaReceita),
                size = Size((larguraGrupo / 2 - 6).coerceAtLeast(1f), alturaReceita),
            )
            drawRect(
                color = Color(0xFFEF5350),
                topLeft = Offset(xBase + larguraGrupo / 2, size.height - alturaDespesa),
                size = Size((larguraGrupo / 2 - 6).coerceAtLeast(1f), alturaDespesa),
            )
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        entradas.forEach { entrada -> Text(entrada.key.toString(), style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun LineChartEvolucao(dados: List<Pair<MesAno, Long>>) {
    if (dados.size < 2) {
        Text("Dados insuficientes (mínimo 2 meses com lançamentos).", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val maximo = dados.maxOf { it.second }
    val minimo = dados.minOf { it.second }
    val amplitude = (maximo - minimo).coerceAtLeast(1L).toFloat()
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val passoX = size.width / (dados.size - 1)
        val pontos = dados.mapIndexed { indice, par ->
            Offset(indice * passoX, size.height - ((par.second - minimo) / amplitude) * size.height)
        }
        for (indice in 0 until pontos.size - 1) {
            drawLine(Color(0xFF5C6BC0), pontos[indice], pontos[indice + 1], strokeWidth = 4f)
        }
        pontos.forEach { ponto -> drawCircle(Color(0xFF5C6BC0), radius = 5f, center = ponto) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        dados.forEach { (mes, _) -> Text(mes.toString(), style = MaterialTheme.typography.labelSmall) }
    }
}
