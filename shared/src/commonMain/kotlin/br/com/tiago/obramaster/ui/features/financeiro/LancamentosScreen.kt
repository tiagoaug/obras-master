package br.com.tiago.obramaster.ui.features.financeiro

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import br.com.tiago.obramaster.core.financeiro.FiltroFinanceiro
import br.com.tiago.obramaster.core.financeiro.FinanceEngine
import br.com.tiago.obramaster.core.financeiro.PeriodoPreset
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.RateioLancamento
import br.com.tiago.obramaster.domain.RetencaoLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.domain.TipoRetencao
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudFormScaffold
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LancamentosScreen(onVoltar: () -> Unit, viewModel: LancamentosViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var editando by remember { mutableStateOf<LancamentoFinanceiro?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var mostrarFiltro by remember { mutableStateOf(false) }

    fun nomeCategoria(id: String) = uiState.categorias.firstOrNull { it.id == id }?.nome ?: "—"

    if (mostrarForm) {
        LancamentoFormScreen(
            existente = editando,
            uiState = uiState,
            onVoltar = { mostrarForm = false },
            onSalvar = { tipo, categoriaId, centroId, natureza, projetoId, descricao, valor, data, formaPagamento, pago, contaId, rateios, retencoes ->
                viewModel.salvar(editando, tipo, categoriaId, centroId, natureza, projetoId, null, descricao, valor, data, formaPagamento, pago, contaId, rateios, retencoes)
                mostrarForm = false
            },
            carregarRateios = { id -> viewModel.rateiosDoLancamento(id) },
            carregarRetencoes = { id -> viewModel.retencoesDoLancamento(id) },
        )
        return
    }

    LcrudListScaffold(
        titulo = "Lançamentos",
        itens = uiState.lancamentosFiltrados,
        filtro = { lancamento, busca -> lancamento.descricao.contains(busca, ignoreCase = true) },
        itemHeadline = { "${it.descricao} — ${MoneyFormatter.formatar(if (it.tipo == TipoLancamento.RECEITA) it.valor else -it.valor)}" },
        itemSupporting = { "${DataFormatter.formatar(it.data)} · ${nomeCategoria(it.categoriaId)} · ${if (it.pago) "Pago" else "Pendente"}" },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        acoesTopBar = {
            IconButton(onClick = { mostrarFiltro = true }) { Icon(Icons.Filled.FilterList, contentDescription = "Filtros") }
        },
        exportar = { itens ->
            ExportableDocument(
                titulo = "Lançamentos",
                colunas = listOf("Descrição", "Categoria", "Data", "Valor", "Pago"),
                linhas = itens.map { lancamento ->
                    listOf(
                        lancamento.descricao,
                        nomeCategoria(lancamento.categoriaId),
                        DataFormatter.formatar(lancamento.data),
                        MoneyFormatter.formatar(if (lancamento.tipo == TipoLancamento.RECEITA) lancamento.valor else -lancamento.valor),
                        if (lancamento.pago) "Sim" else "Não",
                    )
                },
            )
        },
    )

    if (mostrarFiltro) {
        FiltroLancamentosSheet(
            uiState = uiState,
            onAplicar = { viewModel.atualizarFiltro(it); mostrarFiltro = false },
            onDismiss = { mostrarFiltro = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroLancamentosSheet(
    uiState: LancamentosUiState,
    onAplicar: (FiltroFinanceiro) -> Unit,
    onDismiss: () -> Unit,
) {
    var filtro by remember { mutableStateOf(uiState.filtro) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium)

            Text("Período", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filtro.periodoInicio == null, onClick = { filtro = filtro.copy(periodoInicio = null, periodoFim = null) }, label = { Text("Tudo") })
                listOf(PeriodoPreset.HOJE to "Hoje", PeriodoPreset.SEMANA to "Semana", PeriodoPreset.MES to "Mês", PeriodoPreset.ANO to "Ano").forEach { (preset, rotulo) ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val (inicio, fim) = FinanceEngine.periodoPreset(preset, Clock.System.now().toEpochMilliseconds())
                            filtro = filtro.copy(periodoInicio = inicio, periodoFim = fim)
                        },
                        label = { Text(rotulo) },
                    )
                }
            }

            Text("Projeto", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filtro.projetoId == null, onClick = { filtro = filtro.copy(projetoId = null) }, label = { Text("Todos") })
                uiState.projetos.forEach { projeto ->
                    FilterChip(selected = filtro.projetoId == projeto.id, onClick = { filtro = filtro.copy(projetoId = projeto.id) }, label = { Text(projeto.nome) })
                }
            }

            Text("Natureza", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filtro.natureza == null, onClick = { filtro = filtro.copy(natureza = null) }, label = { Text("Ambos") })
                NaturezaLancamento.entries.forEach { opcao ->
                    FilterChip(selected = filtro.natureza == opcao, onClick = { filtro = filtro.copy(natureza = opcao) }, label = { Text(opcao.name) })
                }
            }

            Text("Centro de custo", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filtro.centroDeCustoId == null, onClick = { filtro = filtro.copy(centroDeCustoId = null) }, label = { Text("Todos") })
                uiState.centros.forEach { centro ->
                    FilterChip(selected = filtro.centroDeCustoId == centro.id, onClick = { filtro = filtro.copy(centroDeCustoId = centro.id) }, label = { Text(centro.nome) })
                }
            }

            Button(onClick = { onAplicar(filtro) }, modifier = Modifier.fillMaxWidth()) { Text("Aplicar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LancamentoFormScreen(
    existente: LancamentoFinanceiro?,
    uiState: LancamentosUiState,
    onVoltar: () -> Unit,
    onSalvar: (TipoLancamento, String, String, NaturezaLancamento, String?, String, Long, Long, String, Boolean, String?, List<Pair<String, Double>>, List<Pair<TipoRetencao, Double>>) -> Unit,
    carregarRateios: suspend (String) -> List<RateioLancamento>,
    carregarRetencoes: suspend (String) -> List<RetencaoLancamento>,
) {
    var tipo by remember { mutableStateOf(existente?.tipo ?: TipoLancamento.DESPESA) }
    var categoriaId by remember { mutableStateOf(existente?.categoriaId) }
    var projetoId by remember { mutableStateOf(existente?.projetoId) }
    var descricao by remember { mutableStateOf(existente?.descricao ?: "") }
    var valor by remember { mutableStateOf(existente?.valor ?: 0L) }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(existente?.data ?: Clock.System.now().toEpochMilliseconds())) }
    var formaPagamento by remember { mutableStateOf(existente?.formaPagamento ?: "") }
    var pago by remember { mutableStateOf(existente?.pago ?: false) }
    var contaId by remember { mutableStateOf(existente?.contaId) }
    var natureza by remember { mutableStateOf(existente?.natureza ?: NaturezaLancamento.CONTABIL) }
    var centroManualId by remember { mutableStateOf(existente?.centroDeCustoId) }
    var rateioAtivo by remember { mutableStateOf(false) }
    var rateios by remember { mutableStateOf(listOf<Pair<String, String>>()) } // centroId to texto do percentual
    var retencaoAtiva by remember { mutableStateOf(false) }
    var retencoes by remember { mutableStateOf(listOf<Pair<TipoRetencao, String>>()) } // tipo to texto do percentual

    LaunchedEffect(existente?.id) {
        if (existente != null) {
            val rateiosExistentes = carregarRateios(existente.id)
            if (rateiosExistentes.isNotEmpty()) {
                rateioAtivo = true
                rateios = rateiosExistentes.map { it.centroDeCustoId to it.percentual.toString() }
            }
            val retencoesExistentes = carregarRetencoes(existente.id)
            if (retencoesExistentes.isNotEmpty()) {
                retencaoAtiva = true
                retencoes = retencoesExistentes.map { it.tipo to it.percentual.toString() }
            }
        }
    }

    val centroDoProjeto = uiState.centros.firstOrNull { it.projetoId == projetoId }
    val centroDeCustoId = centroDoProjeto?.id ?: centroManualId
    val categoriasDoTipo = uiState.categorias.filter { it.tipo == tipo }
    val centrosManuais = uiState.centros.filter { it.projetoId == null }
    val somaRateio = rateios.sumOf { it.second.replace(',', '.').toDoubleOrNull() ?: 0.0 }
    val rateioValido = !rateioAtivo || kotlin.math.abs(somaRateio - 100.0) <= 0.01
    val retencoesValidas = retencoes.mapNotNull { (tipoRetencao, texto) -> texto.replace(',', '.').toDoubleOrNull()?.let { tipoRetencao to it } }
    val valorLiquido = valor - retencoesValidas.sumOf { (_, percentual) -> FinanceEngine.calcularValorRetencao(valor, percentual) }

    LcrudFormScaffold(
        titulo = if (existente == null) "Novo lançamento" else "Editar lançamento",
        onVoltar = onVoltar,
        podeSalvar = descricao.isNotBlank() && categoriaId != null && centroDeCustoId != null && valor > 0 && rateioValido && (!pago || contaId != null),
        onSalvar = {
            val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
            val rateiosFinal = if (rateioAtivo) {
                rateios.mapNotNull { (centroId, texto) -> texto.replace(',', '.').toDoubleOrNull()?.let { centroId to it } }
            } else {
                emptyList()
            }
            val retencoesFinal = if (retencaoAtiva) retencoesValidas else emptyList()
            onSalvar(tipo, categoriaId!!, centroDeCustoId!!, natureza, projetoId, descricao, valor, data, formaPagamento, pago, contaId, rateiosFinal, retencoesFinal)
        },
    ) {
        Text("Tipo", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoLancamento.entries.forEach { opcao ->
                FilterChip(selected = tipo == opcao, onClick = { tipo = opcao; categoriaId = null }, label = { Text(opcao.name) })
            }
        }

        Text("Categoria", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categoriasDoTipo.forEach { categoria ->
                FilterChip(
                    selected = categoriaId == categoria.id,
                    onClick = { categoriaId = categoria.id; natureza = categoria.naturezaPadrao },
                    label = { Text(categoria.nome) },
                )
            }
        }

        OutlinedTextField(descricao, { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        CalculatorTextField(valueCentavos = valor, onValueChange = { valor = it }, label = "Valor (bruto)", modifier = Modifier.fillMaxWidth().padding(top = 12.dp))

        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Retenções fiscais (INSS, ISS, IRRF...)", style = MaterialTheme.typography.labelLarge)
            Switch(checked = retencaoAtiva, onCheckedChange = { retencaoAtiva = it; if (it && retencoes.isEmpty()) retencoes = listOf(TipoRetencao.INSS to "11") })
        }
        if (retencaoAtiva) {
            retencoes.forEachIndexed { indice, (tipoRetencao, percentualTexto) ->
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TipoRetencao.entries.forEach { opcao ->
                            FilterChip(
                                selected = tipoRetencao == opcao,
                                onClick = { retencoes = retencoes.toMutableList().also { it[indice] = opcao to percentualTexto } },
                                label = { Text(opcao.name) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = percentualTexto,
                        onValueChange = { novo -> retencoes = retencoes.toMutableList().also { it[indice] = tipoRetencao to novo } },
                        label = { Text("%") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { retencoes = retencoes + (TipoRetencao.INSS to "") }) { Text("+ Retenção") }
                Text("Valor líquido: ${MoneyFormatter.formatar(valorLiquido)}", modifier = Modifier.padding(top = 12.dp))
            }
        }

        OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        OutlinedTextField(formaPagamento, { formaPagamento = it }, label = { Text("Forma de pagamento") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))

        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (tipo == TipoLancamento.RECEITA) "Recebido" else "Pago")
            Switch(checked = pago, onCheckedChange = { pago = it })
        }
        if (pago) {
            Text("Conta (obrigatório)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.contas.forEach { conta ->
                    FilterChip(selected = contaId == conta.id, onClick = { contaId = conta.id }, label = { Text(conta.nome) })
                }
            }
            if (uiState.contas.isEmpty()) {
                Text("Cadastre uma conta em Financeiro → Contas antes de marcar como pago.", style = MaterialTheme.typography.labelSmall)
            }
        }

        Text("Natureza", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NaturezaLancamento.entries.forEach { opcao ->
                FilterChip(selected = natureza == opcao, onClick = { natureza = opcao }, label = { Text(opcao.name) })
            }
        }

        Text("Projeto (opcional)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = projetoId == null, onClick = { projetoId = null }, label = { Text("Nenhum") })
            uiState.projetos.forEach { projeto ->
                FilterChip(selected = projetoId == projeto.id, onClick = { projetoId = projeto.id }, label = { Text(projeto.nome) })
            }
        }

        if (centroDoProjeto == null) {
            Text("Centro de custo", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                centrosManuais.forEach { centro ->
                    FilterChip(selected = centroManualId == centro.id, onClick = { centroManualId = centro.id }, label = { Text(centro.nome) })
                }
            }
        } else {
            Text(
                "Centro de custo: ${centroDoProjeto.nome} (do projeto)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Ratear entre centros de custo", style = MaterialTheme.typography.labelLarge)
            Switch(checked = rateioAtivo, onCheckedChange = { rateioAtivo = it; if (it && rateios.isEmpty()) rateios = listOf("" to "") })
        }
        if (rateioAtivo) {
            rateios.forEachIndexed { indice, (centroId, percentualTexto) ->
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.centros.forEach { centro ->
                            FilterChip(
                                selected = centroId == centro.id,
                                onClick = { rateios = rateios.toMutableList().also { it[indice] = centro.id to percentualTexto } },
                                label = { Text(centro.nome) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = percentualTexto,
                        onValueChange = { novo -> rateios = rateios.toMutableList().also { it[indice] = centroId to novo } },
                        label = { Text("%") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { rateios = rateios + ("" to "") }) { Text("+ Centro") }
                Text("Soma: $somaRateio%", modifier = Modifier.padding(top = 12.dp))
            }
            if (!rateioValido) {
                Text("A soma do rateio precisa ser 100%.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
