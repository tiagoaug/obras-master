package br.com.tiago.obramaster.ui.features.compras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private sealed interface DestinoCompras {
    data object Hub : DestinoCompras
    data object Fornecedores : DestinoCompras
    data object Pedidos : DestinoCompras
    data object Comparativo : DestinoCompras
}

/** SPEC_OBRA_MASTER.md §4.4 — ponto de entrada do módulo Compras. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasModuloScreen(onVoltar: () -> Unit) {
    var destino by remember { mutableStateOf<DestinoCompras>(DestinoCompras.Hub) }

    when (destino) {
        DestinoCompras.Hub -> Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Compras") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                ItemHub("Fornecedores", Icons.Filled.LocalShipping) { destino = DestinoCompras.Fornecedores }
                ItemHub("Pedidos de Compra", Icons.Filled.ShoppingCart) { destino = DestinoCompras.Pedidos }
                ItemHub("Comparativo de Cotações", Icons.Filled.Compare) { destino = DestinoCompras.Comparativo }
            }
        }

        DestinoCompras.Fornecedores -> FornecedoresScreen(onVoltar = { destino = DestinoCompras.Hub })
        DestinoCompras.Pedidos -> PedidosCompraScreen(onVoltar = { destino = DestinoCompras.Hub })
        DestinoCompras.Comparativo -> ComparativoCotacoesScreen(onVoltar = { destino = DestinoCompras.Hub })
    }
}

@Composable
private fun ItemHub(titulo: String, icone: ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        ListItem(
            headlineContent = { Text(titulo) },
            leadingContent = { Icon(icone, contentDescription = null) },
        )
    }
}
