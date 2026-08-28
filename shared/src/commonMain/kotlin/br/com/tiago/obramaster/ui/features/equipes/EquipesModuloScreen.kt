package br.com.tiago.obramaster.ui.features.equipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.WorkHistory
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

private sealed interface DestinoEquipes {
    data object Hub : DestinoEquipes
    data object Funcionarios : DestinoEquipes
    data object Equipes : DestinoEquipes
    data object RegistroTrabalho : DestinoEquipes
    data object GerarPagamento : DestinoEquipes
    data object Relatorio : DestinoEquipes
}

/** SPEC_OBRA_MASTER.md §4.3 — ponto de entrada do módulo Equipes e Pagamentos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipesModuloScreen(onVoltar: () -> Unit) {
    var destino by remember { mutableStateOf<DestinoEquipes>(DestinoEquipes.Hub) }

    when (destino) {
        DestinoEquipes.Hub -> Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Equipes e Pagamentos") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                ItemHub("Funcionários", Icons.Filled.Person) { destino = DestinoEquipes.Funcionarios }
                ItemHub("Equipes", Icons.Filled.Groups) { destino = DestinoEquipes.Equipes }
                ItemHub("Registro de Trabalho", Icons.Filled.WorkHistory) { destino = DestinoEquipes.RegistroTrabalho }
                ItemHub("Gerar Pagamento", Icons.Filled.Payments) { destino = DestinoEquipes.GerarPagamento }
                ItemHub("Relatório", Icons.Filled.Summarize) { destino = DestinoEquipes.Relatorio }
            }
        }

        DestinoEquipes.Funcionarios -> FuncionariosScreen(onVoltar = { destino = DestinoEquipes.Hub })
        DestinoEquipes.Equipes -> EquipesScreen(onVoltar = { destino = DestinoEquipes.Hub })
        DestinoEquipes.RegistroTrabalho -> RegistroTrabalhoScreen(onVoltar = { destino = DestinoEquipes.Hub })
        DestinoEquipes.GerarPagamento -> GerarPagamentoScreen(onVoltar = { destino = DestinoEquipes.Hub })
        DestinoEquipes.Relatorio -> RelatorioEquipesScreen(onVoltar = { destino = DestinoEquipes.Hub })
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
