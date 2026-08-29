package br.com.tiago.obramaster.ui.features.areaexecutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.NormaABNT
import br.com.tiago.obramaster.domain.labelPtBr

/** SPEC_AREA_EXECUTOR.md §9 — nunca mostra texto integral da norma, só metadado + link oficial.
 * O ícone "📘 Base normativa" na tela da calculadora (vínculo calculadora → norma) é da Fase 8.8. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormaDetalheScreen(
    norma: NormaABNT,
    todasAsNormas: List<NormaABNT>,
    onVoltar: () -> Unit,
    onAbrirNorma: (NormaABNT) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val relacionadas = remember(norma, todasAsNormas) {
        norma.normasRelacionadas.mapNotNull { numero -> todasAsNormas.find { it.numero == numero } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(norma.numero) },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(norma.titulo, style = MaterialTheme.typography.headlineSmall)
            SuggestionChip(onClick = {}, label = { Text(norma.categoria.labelPtBr) })

            HorizontalDivider()

            Text("Escopo", style = MaterialTheme.typography.labelLarge)
            Text(norma.escopoResumo, style = MaterialTheme.typography.bodyLarge)

            if (relacionadas.isNotEmpty()) {
                HorizontalDivider()
                Text("Normas relacionadas", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    relacionadas.forEach { relacionada ->
                        AssistChip(
                            onClick = { onAbrirNorma(relacionada) },
                            label = { Text("${relacionada.numero} — ${relacionada.titulo}") },
                        )
                    }
                }
            }

            HorizontalDivider()

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "O ObraMaster não reproduz o texto integral de normas ABNT. Consulte ou adquira a norma completa na fonte oficial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = { uriHandler.openUri(norma.urlCatalogoOficial) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Adquirir no Catálogo Oficial ABNT")
                    }
                }
            }
        }
    }
}
