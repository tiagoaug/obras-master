package br.com.tiago.obramaster.ui.features.compras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import org.koin.compose.koinInject

/** SPEC_OBRA_MASTER.md §4.4 — "Comparativo simples de cotações (mesmo item, fornecedores diferentes)." */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparativoCotacoesScreen(onVoltar: () -> Unit, viewModel: PedidosCompraViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var todosItens by remember { mutableStateOf<List<ItemCompra>>(emptyList()) }

    LaunchedEffect(Unit) { todosItens = viewModel.itensDeTodos() }

    val pedidosEmCotacao = uiState.pedidos.filter { it.status == StatusPedidoCompra.COTACAO }
    val itensEmCotacao = todosItens.filter { item -> pedidosEmCotacao.any { it.id == item.pedidoId } }
    val porMaterial = itensEmCotacao.groupBy { it.materialId }.filter { (_, itens) -> itens.size > 1 }

    fun nomeMaterial(id: String) = uiState.materiais.firstOrNull { it.id == id }?.nome ?: "—"
    fun nomeFornecedorDoItem(item: ItemCompra): String {
        val pedido = pedidosEmCotacao.firstOrNull { it.id == item.pedidoId }
        return uiState.fornecedores.firstOrNull { it.id == pedido?.fornecedorId }?.nome ?: "—"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparativo de Cotações") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (porMaterial.isEmpty()) {
                Text("Nenhum material com mais de uma cotação em aberto ainda.", style = MaterialTheme.typography.bodyMedium)
            }
            porMaterial.forEach { (materialId, itens) ->
                Text(nomeMaterial(materialId), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                itens.sortedBy { it.valorUnitario }.forEach { item ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(nomeFornecedorDoItem(item))
                            Text("${MoneyFormatter.formatar(item.valorUnitario)} / ${item.unidade}")
                        }
                    }
                }
            }
        }
    }
}
