package br.com.tiago.obramaster.ui.features.orcamentos

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.orcamentos.BdiEngine
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.StatusOrcamento
import br.com.tiago.obramaster.domain.TipoItemOrcamento
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrcamentosScreen(onVoltar: () -> Unit, viewModel: OrcamentosViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarForm by remember { mutableStateOf(false) }
    var orcamentoAberto by remember { mutableStateOf<Orcamento?>(null) }

    fun nomeCliente(id: String?) = uiState.clientes.firstOrNull { it.id == id }?.nome

    LcrudListScaffold(
        titulo = "Orçamentos",
        itens = uiState.orcamentos,
        filtro = { orcamento, busca -> orcamento.titulo.contains(busca, ignoreCase = true) },
        itemHeadline = { it.titulo },
        itemSupporting = { "${it.status.name} · ${nomeCliente(it.clientePessoaId) ?: "sem cliente"} · ${MoneyFormatter.formatar(it.precoVendaTotal)}" },
        onItemClicado = { orcamentoAberto = it },
        onNovoClicado = { mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
    )

    if (mostrarForm) {
        NovoOrcamentoScreen(
            uiState = uiState,
            onSalvar = { projetoId, clientePessoaId, titulo, data, validadeDias, descontoPercent, observacoes, configBdiId, overridePercent, itens ->
                viewModel.salvar(null, projetoId, clientePessoaId, titulo, data, validadeDias, descontoPercent, observacoes, configBdiId, overridePercent, itens)
                mostrarForm = false
            },
            onVoltar = { mostrarForm = false },
        )
    }

    orcamentoAberto?.let { orcamento ->
        DetalheOrcamentoSheet(
            orcamento = orcamento,
            nomeCliente = nomeCliente(orcamento.clientePessoaId),
            nomeProjeto = uiState.projetos.firstOrNull { it.id == orcamento.projetoId }?.nome,
            materiais = uiState.materiais,
            carregarItens = { viewModel.itensDoOrcamento(orcamento.id) },
            onAtualizarStatus = { novoStatus -> viewModel.atualizarStatus(orcamento, novoStatus) },
            onConverterEmProjeto = { viewModel.converterEmProjeto(orcamento) { orcamentoAberto = null } },
            onDismiss = { orcamentoAberto = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalheOrcamentoSheet(
    orcamento: Orcamento,
    nomeCliente: String?,
    nomeProjeto: String?,
    materiais: List<Material>,
    carregarItens: suspend () -> List<ItemOrcamento>,
    onAtualizarStatus: (StatusOrcamento) -> Unit,
    onConverterEmProjeto: () -> Unit,
    onDismiss: () -> Unit,
) {
    var itens by remember { mutableStateOf<List<ItemOrcamento>>(emptyList()) }
    LaunchedEffect(orcamento.id) { itens = carregarItens() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(orcamento.titulo, style = MaterialTheme.typography.titleMedium)
            nomeCliente?.let { Text("Cliente: $it", style = MaterialTheme.typography.bodyMedium) }
            nomeProjeto?.let { Text("Projeto: $it", style = MaterialTheme.typography.bodyMedium) }
            Text("Data: ${DataFormatter.formatar(orcamento.data)} · Validade: ${orcamento.validadeDias} dias", style = MaterialTheme.typography.bodyMedium)

            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusOrcamento.entries.forEach { opcao ->
                    FilterChip(selected = orcamento.status == opcao, onClick = { onAtualizarStatus(opcao) }, label = { Text(opcao.name) })
                }
            }

            HorizontalDivider()
            Text("Itens", style = MaterialTheme.typography.labelLarge)
            itens.forEach { item ->
                val descricao = item.materialId?.let { id -> materiais.firstOrNull { it.id == id }?.nome } ?: item.descricao
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.tipo.name.replace('_', ' ')} · $descricao · ${item.quantidade} ${item.unidade}")
                    Text(MoneyFormatter.formatar(item.valorTotal))
                }
            }

            HorizontalDivider()
            Text("Custo direto: ${MoneyFormatter.formatar(orcamento.custoDiretoTotal)}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "BDI: ${percentTexto(orcamento.bdiPercentualCalculado * 100)}%${if (orcamento.bdiCustomizado) " (customizado)" else ""}",
                style = MaterialTheme.typography.bodyMedium,
            )
            orcamento.descontoPercent?.let { Text("Desconto: ${percentTexto(it)}%", style = MaterialTheme.typography.bodyMedium) }
            Text("Preço de venda total: ${MoneyFormatter.formatar(orcamento.precoVendaTotal)}", style = MaterialTheme.typography.titleSmall)

            if (orcamento.status == StatusOrcamento.APROVADO && orcamento.projetoId == null) {
                Button(onClick = onConverterEmProjeto, modifier = Modifier.fillMaxWidth()) { Text("Converter em Projeto") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoOrcamentoScreen(
    uiState: OrcamentosUiState,
    onSalvar: (String?, String?, String, Long, Int, Double?, String?, String?, Double?, List<ItemOrcamento>) -> Unit,
    onVoltar: () -> Unit,
) {
    var projetoId by remember { mutableStateOf<String?>(null) }
    var clienteId by remember { mutableStateOf<String?>(null) }
    var titulo by remember { mutableStateOf("") }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var validadeDiasTexto by remember { mutableStateOf("15") }
    var descontoTexto by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var configBdiId by remember { mutableStateOf(uiState.configsBdi.firstOrNull { it.padrao }?.id) }
    var usarBdiManual by remember { mutableStateOf(false) }
    var bdiManualTexto by remember { mutableStateOf("") }
    var itens by remember { mutableStateOf<List<ItemOrcamento>>(emptyList()) }
    var mostrarNovoItem by remember { mutableStateOf(false) }

    val custoDireto = itens.sumOf { it.valorTotal }
    val configSelecionada = uiState.configsBdi.firstOrNull { it.id == configBdiId }
    val bdiPercent = if (usarBdiManual) {
        bdiManualTexto.replace(',', '.').toDoubleOrNull() ?: 0.0
    } else {
        configSelecionada?.let { BdiEngine.calcularBdi(it) * 100 } ?: 0.0
    }
    val descontoPercent = descontoTexto.replace(',', '.').toDoubleOrNull()
    val precoComBdi = BdiEngine.precoVendaComBdiPercentual(custoDireto, bdiPercent / 100)
    val precoFinal = if (descontoPercent != null) (precoComBdi * (1 - descontoPercent / 100)).toLong() else precoComBdi
    val markup = if (custoDireto > 0) precoFinal.toDouble() / custoDireto.toDouble() else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Orçamento") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(titulo, { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())

            Text("Cliente (opcional)", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = clienteId == null, onClick = { clienteId = null }, label = { Text("Nenhum") })
                uiState.clientes.forEach { pessoa ->
                    FilterChip(selected = clienteId == pessoa.id, onClick = { clienteId = pessoa.id }, label = { Text(pessoa.nome) })
                }
            }

            Text("Projeto existente (opcional)", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = projetoId == null, onClick = { projetoId = null }, label = { Text("Nenhum") })
                uiState.projetos.forEach { projeto ->
                    FilterChip(selected = projetoId == projeto.id, onClick = { projetoId = projeto.id }, label = { Text(projeto.nome) })
                }
            }

            OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(validadeDiasTexto, { validadeDiasTexto = it }, label = { Text("Validade (dias)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(observacoes, { observacoes = it }, label = { Text("Observações (opcional)") }, modifier = Modifier.fillMaxWidth())

            HorizontalDivider()
            Text(
                "Itens (${itens.size}) — custo direto ${MoneyFormatter.formatar(custoDireto)}",
                style = MaterialTheme.typography.labelLarge,
            )
            LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(itens) { item ->
                    val descricao = item.materialId?.let { id -> uiState.materiais.firstOrNull { it.id == id }?.nome } ?: item.descricao
                    Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.tipo.name.replace('_', ' ')} · $descricao · ${item.quantidade} ${item.unidade}")
                            Text(MoneyFormatter.formatar(item.valorTotal))
                        }
                    }
                }
            }
            OutlinedButton(onClick = { mostrarNovoItem = true }) { Text("+ Item") }

            HorizontalDivider()
            Text("Aplicar BDI", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.configsBdi.forEach { config ->
                    FilterChip(
                        selected = !usarBdiManual && configBdiId == config.id,
                        onClick = { usarBdiManual = false; configBdiId = config.id },
                        label = { Text(config.nome) },
                    )
                }
            }
            Row {
                Checkbox(checked = usarBdiManual, onCheckedChange = { usarBdiManual = it })
                Text("BDI manual pontual (%)", modifier = Modifier.padding(top = 12.dp))
            }
            if (usarBdiManual) {
                OutlinedTextField(bdiManualTexto, { bdiManualTexto = it }, label = { Text("BDI (%)") }, modifier = Modifier.fillMaxWidth())
            }
            OutlinedTextField(descontoTexto, { descontoTexto = it }, label = { Text("Desconto sobre o preço final (%, opcional)") }, modifier = Modifier.fillMaxWidth())

            Text("BDI aplicado: ${percentTexto(bdiPercent)}%", style = MaterialTheme.typography.bodyMedium)
            Text("Preço de venda: ${MoneyFormatter.formatar(precoFinal)}", style = MaterialTheme.typography.titleSmall)
            Text("Markup: ${percentTexto(markup * 100)}%", style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = {
                    val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                    val validade = validadeDiasTexto.toIntOrNull() ?: 15
                    onSalvar(
                        projetoId, clienteId, titulo, data, validade,
                        descontoPercent, observacoes.ifBlank { null }, if (usarBdiManual) null else configBdiId,
                        if (usarBdiManual) bdiPercent else null,
                        itens,
                    )
                },
                enabled = titulo.isNotBlank() && itens.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar orçamento") }
        }
    }

    if (mostrarNovoItem) {
        NovoItemOrcamentoSheet(
            materiais = uiState.materiais,
            onAdicionar = { item -> itens = itens + item; mostrarNovoItem = false },
            onDismiss = { mostrarNovoItem = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun NovoItemOrcamentoSheet(
    materiais: List<Material>,
    onAdicionar: (ItemOrcamento) -> Unit,
    onDismiss: () -> Unit,
) {
    var tipo by remember { mutableStateOf(TipoItemOrcamento.MATERIAL) }
    var materialId by remember { mutableStateOf<String?>(null) }
    var descricao by remember { mutableStateOf("") }
    var quantidadeTexto by remember { mutableStateOf("1") }
    var unidade by remember { mutableStateOf("") }
    var valorUnitario by remember { mutableStateOf(0L) }

    LaunchedEffect(materialId) {
        if (tipo == TipoItemOrcamento.MATERIAL) {
            val material = materiais.firstOrNull { it.id == materialId }
            unidade = material?.unidadePadrao ?: unidade
            descricao = material?.nome ?: descricao
        }
    }

    val quantidade = quantidadeTexto.replace(',', '.').toDoubleOrNull() ?: 0.0
    val valorTotal = (valorUnitario * quantidade).toLong()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Novo item", style = MaterialTheme.typography.titleMedium)

            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoItemOrcamento.entries.forEach { opcao ->
                    FilterChip(
                        selected = tipo == opcao,
                        onClick = { tipo = opcao; materialId = null; descricao = "" },
                        label = { Text(opcao.name.replace('_', ' ')) },
                    )
                }
            }

            if (tipo == TipoItemOrcamento.MATERIAL) {
                Text("Material", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    materiais.forEach { material ->
                        FilterChip(selected = materialId == material.id, onClick = { materialId = material.id }, label = { Text(material.nome) })
                    }
                }
            } else {
                OutlinedTextField(descricao, { descricao = it }, label = { Text("Descrição (ex.: Pedreiro, Servente)") }, modifier = Modifier.fillMaxWidth())
            }

            OutlinedTextField(quantidadeTexto, { quantidadeTexto = it }, label = { Text("Quantidade") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(unidade, { unidade = it }, label = { Text("Unidade") }, modifier = Modifier.fillMaxWidth())
            CalculatorTextField(valueCentavos = valorUnitario, onValueChange = { valorUnitario = it }, label = "Valor unitário", modifier = Modifier.fillMaxWidth())
            Text("Total: ${MoneyFormatter.formatar(valorTotal)}", style = MaterialTheme.typography.titleSmall)

            Button(
                onClick = {
                    onAdicionar(
                        ItemOrcamento(
                            id = Uuid.random().toString(),
                            orcamentoId = "",
                            tipo = tipo,
                            descricao = descricao,
                            materialId = materialId,
                            quantidade = quantidade,
                            unidade = unidade,
                            valorUnitario = valorUnitario,
                            valorTotal = valorTotal,
                        ),
                    )
                },
                enabled = descricao.isNotBlank() && quantidade > 0 && valorUnitario > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Adicionar") }
        }
    }
}

private fun percentTexto(valor: Double): String {
    val arredondado = kotlin.math.round(valor * 100) / 100
    return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
}
