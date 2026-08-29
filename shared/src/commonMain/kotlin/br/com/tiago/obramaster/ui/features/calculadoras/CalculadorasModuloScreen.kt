package br.com.tiago.obramaster.ui.features.calculadoras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CropSquare
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private sealed interface DestinoCalculadoras {
    data object Hub : DestinoCalculadoras
    data object Cientifica : DestinoCalculadoras
    data object Trigonometria : DestinoCalculadoras
    data object AreasPerimetros : DestinoCalculadoras
    data object Volumes : DestinoCalculadoras
    data object Engenharia : DestinoCalculadoras
}

/** SPEC_OBRA_MASTER.md §4.12 — ponto de entrada do módulo Calculadoras. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadorasModuloScreen(onVoltar: () -> Unit) {
    var destino by remember { mutableStateOf<DestinoCalculadoras>(DestinoCalculadoras.Hub) }

    when (destino) {
        DestinoCalculadoras.Hub -> Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Calculadoras") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                ItemHub("Científica", Icons.Filled.Calculate) { destino = DestinoCalculadoras.Cientifica }
                ItemHub("Trigonométrica", Icons.Filled.Straighten) { destino = DestinoCalculadoras.Trigonometria }
                ItemHub("Áreas e Perímetros", Icons.Filled.CropSquare) { destino = DestinoCalculadoras.AreasPerimetros }
                ItemHub("Volumes", Icons.Filled.ViewInAr) { destino = DestinoCalculadoras.Volumes }
                ItemHub("Engenharia", Icons.Filled.Build) { destino = DestinoCalculadoras.Engenharia }
            }
        }

        DestinoCalculadoras.Cientifica -> CientificaScreen(onVoltar = { destino = DestinoCalculadoras.Hub })
        DestinoCalculadoras.Trigonometria -> TrigonometriaScreen(onVoltar = { destino = DestinoCalculadoras.Hub })
        DestinoCalculadoras.AreasPerimetros -> AreasPerimetrosScreen(onVoltar = { destino = DestinoCalculadoras.Hub })
        DestinoCalculadoras.Volumes -> VolumesScreen(onVoltar = { destino = DestinoCalculadoras.Hub })
        DestinoCalculadoras.Engenharia -> EngenhariaScreen(onVoltar = { destino = DestinoCalculadoras.Hub })
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
