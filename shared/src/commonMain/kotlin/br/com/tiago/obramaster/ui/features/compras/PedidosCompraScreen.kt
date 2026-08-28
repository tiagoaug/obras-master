package br.com.tiago.obramaster.ui.features.compras

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
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosCompraScreen(onVoltar: () -> Unit, viewModel: PedidosCompraViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarForm by remember { mutableStateOf(false) }
    var pedidoAberto by remember { mutableStateOf<PedidoCompra?>(null) }

    fun nomeProjeto(id: String) = uiState.projetos.firstOrNull { it.id == id }?.nome ?: "—"
    fun nomeFornecedor(id: String?) = uiState.fornecedores.firstOrNull { it.id == id }?.nome

    LcrudListScaffold(
        titulo = "Compras",
        itens = uiState.pedidos,
        filtro = { pedido, busca -> nomeProjeto(pedido.projetoId).contains(busca, ignoreCase = true) },
        itemHeadline = { "${nomeProjeto(it.projetoId)}${nomeFornecedor(it.fornecedorId)?.let { f -> " — $f" } ?: ""}" },
        itemSupporting = { "${it.status.name} · ${MoneyFormatter.formatar(it.valorTotal)} · ${DataFormatter.formatar(it.data)}" },
        onItemClicado = { pedidoAberto = it },
        onNovoClicado = { mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
    )

    if (mostrarForm) {
        NovoPedidoScreen(
            uiState = uiState,
            onCarregarEtapas = { projetoId -> viewModel.etapasDoProjeto(projetoId) },
            onSalvar = { projetoId, etapaId, fornecedorId, data, itens ->
                viewModel.salvarPedido(projetoId, etapaId, fornecedorId, data, itens)
                mostrarForm = false
            },
            onVoltar = { mostrarForm = false },
        )
    }

    pedidoAberto?.let { pedido ->
        DetalhePedidoSheet(
            pedido = pedido,
            nomeProjeto = nomeProjeto(pedido.projetoId),
            nomeFornecedor = nomeFornecedor(pedido.fornecedorId),
            carregarItens = { viewModel.itensDoPedido(pedido.id) },
            onAtualizarStatus = { novoStatus -> viewModel.atualizarStatus(pedido, novoStatus) },
            onDismiss = { pedidoAberto = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalhePedidoSheet(
    pedido: PedidoCompra,
    nomeProjeto: String,
    nomeFornecedor: String?,
    carregarItens: suspend () -> List<ItemCompra>,
    onAtualizarStatus: (StatusPedidoCompra) -> Unit,
    onDismiss: () -> Unit,
) {
    var itens by remember { mutableStateOf<List<ItemCompra>>(emptyList()) }
    LaunchedEffect(pedido.id) { itens = carregarItens() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(nomeProjeto, style = MaterialTheme.typography.titleMedium)
            nomeFornecedor?.let { Text("Fornecedor: $it", style = MaterialTheme.typography.bodyMedium) }
            Text("Data: ${DataFormatter.formatar(pedido.data)}", style = MaterialTheme.typography.bodyMedium)

            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPedidoCompra.entries.forEach { opcao ->
                    FilterChip(selected = pedido.status == opcao, onClick = { onAtualizarStatus(opcao) }, label = { Text(opcao.name) })
                }
            }

            Text("Itens", style = MaterialTheme.typography.labelLarge)
            itens.forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantidade} ${item.unidade}")
                    Text(MoneyFormatter.formatar(item.valorTotal))
                }
            }
            Text("Total: ${MoneyFormatter.formatar(pedido.valorTotal)}", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoPedidoScreen(
    uiState: PedidosCompraUiState,
    onCarregarEtapas: suspend (String) -> List<Etapa>,
    onSalvar: (String, String?, String?, Long, List<ItemCompra>) -> Unit,
    onVoltar: () -> Unit,
) {
    var projetoId by remember { mutableStateOf<String?>(null) }
    var etapaId by remember { mutableStateOf<String?>(null) }
    var etapasDoProjeto by remember { mutableStateOf<List<Etapa>>(emptyList()) }
    var fornecedorId by remember { mutableStateOf<String?>(null) }
    var dataTexto by remember { mutableStateOf(DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var itens by remember { mutableStateOf<List<ItemCompra>>(emptyList()) }
    var mostrarNovoItem by remember { mutableStateOf(false) }

    LaunchedEffect(projetoId) {
        etapaId = null
        etapasDoProjeto = projetoId?.let { onCarregarEtapas(it) } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Pedido de Compra") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

            Text("Fornecedor (opcional)", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = fornecedorId == null, onClick = { fornecedorId = null }, label = { Text("Nenhum") })
                uiState.fornecedores.forEach { pessoa ->
                    FilterChip(selected = fornecedorId == pessoa.id, onClick = { fornecedorId = pessoa.id }, label = { Text(pessoa.nome) })
                }
            }

            OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())

            Text("Itens (${itens.size}) — total ${MoneyFormatter.formatar(itens.sumOf { it.valorTotal })}", style = MaterialTheme.typography.labelLarge)
            LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(itens) { item ->
                    val material = uiState.materiais.firstOrNull { it.id == item.materialId }
                    Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${material?.nome ?: "—"} · ${item.quantidade} ${item.unidade}")
                            Text(MoneyFormatter.formatar(item.valorTotal))
                        }
                    }
                }
            }
            OutlinedButton(onClick = { mostrarNovoItem = true }) { Text("+ Item") }

            Button(
                onClick = {
                    val projeto = projetoId ?: return@Button
                    val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                    onSalvar(projeto, etapaId, fornecedorId, data, itens)
                },
                enabled = projetoId != null && itens.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar pedido") }
        }
    }

    if (mostrarNovoItem) {
        NovoItemSheet(
            materiais = uiState.materiais,
            onAdicionar = { item -> itens = itens + item; mostrarNovoItem = false },
            onDismiss = { mostrarNovoItem = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun NovoItemSheet(
    materiais: List<br.com.tiago.obramaster.domain.Material>,
    onAdicionar: (ItemCompra) -> Unit,
    onDismiss: () -> Unit,
) {
    var materialId by remember { mutableStateOf<String?>(null) }
    var quantidadeTexto by remember { mutableStateOf("1") }
    var unidade by remember { mutableStateOf("") }
    var valorUnitario by remember { mutableStateOf(0L) }

    LaunchedEffect(materialId) {
        unidade = materiais.firstOrNull { it.id == materialId }?.unidadePadrao ?: unidade
    }

    val quantidade = quantidadeTexto.replace(',', '.').toDoubleOrNull() ?: 0.0
    val valorTotal = (valorUnitario * quantidade).toLong()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Novo item", style = MaterialTheme.typography.titleMedium)

            Text("Material", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                materiais.forEach { material ->
                    FilterChip(selected = materialId == material.id, onClick = { materialId = material.id }, label = { Text(material.nome) })
                }
            }

            OutlinedTextField(quantidadeTexto, { quantidadeTexto = it }, label = { Text("Quantidade") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(unidade, { unidade = it }, label = { Text("Unidade") }, modifier = Modifier.fillMaxWidth())
            CalculatorTextField(valueCentavos = valorUnitario, onValueChange = { valorUnitario = it }, label = "Valor unitário", modifier = Modifier.fillMaxWidth())
            Text("Total: ${MoneyFormatter.formatar(valorTotal)}", style = MaterialTheme.typography.titleSmall)

            Button(
                onClick = {
                    val material = materialId ?: return@Button
                    onAdicionar(
                        ItemCompra(
                            id = Uuid.random().toString(),
                            pedidoId = "",
                            materialId = material,
                            quantidade = quantidade,
                            unidade = unidade,
                            valorUnitario = valorUnitario,
                            valorTotal = valorTotal,
                        ),
                    )
                },
                enabled = materialId != null && quantidade > 0 && valorUnitario > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Adicionar") }
        }
    }
}
