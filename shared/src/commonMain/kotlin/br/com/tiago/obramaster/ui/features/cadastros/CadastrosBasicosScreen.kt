package br.com.tiago.obramaster.ui.features.cadastros

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private sealed interface DestinoCadastro {
    data object Hub : DestinoCadastro
    data object Cores : DestinoCadastro
    data object Materiais : DestinoCadastro
    data object Unidades : DestinoCadastro
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastrosBasicosScreen(onVoltar: () -> Unit) {
    var destino by remember { mutableStateOf<DestinoCadastro>(DestinoCadastro.Hub) }

    when (destino) {
        DestinoCadastro.Hub -> Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cadastros Básicos") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                ItemHub("Cores", Icons.Filled.Palette) { destino = DestinoCadastro.Cores }
                ItemHub("Materiais", Icons.Filled.ViewInAr) { destino = DestinoCadastro.Materiais }
                ItemHub("Unidades de Medida", Icons.Filled.Straighten) { destino = DestinoCadastro.Unidades }
            }
        }

        DestinoCadastro.Cores -> CoresScreen(onVoltar = { destino = DestinoCadastro.Hub })
        DestinoCadastro.Materiais -> MateriaisScreen(onVoltar = { destino = DestinoCadastro.Hub })
        DestinoCadastro.Unidades -> UnidadesMedidaScreen(onVoltar = { destino = DestinoCadastro.Hub })
    }
}

@Composable
private fun ItemHub(titulo: String, icone: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        ListItem(
            headlineContent = { Text(titulo) },
            leadingContent = { Icon(icone, contentDescription = null) },
        )
    }
}
