package br.com.tiago.obramaster.ui.features.configuracoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.ui.theme.FontePreferencia
import br.com.tiago.obramaster.ui.theme.TemaPreferencia
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcessibilidadeScreen(
    onVoltar: () -> Unit,
    store: AccessibilityPrefsStore = koinInject(),
) {
    val prefs by store.prefs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acessibilidade") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column {
                Text("Tema", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TemaPreferencia.entries.forEach { tema ->
                        FilterChip(
                            selected = prefs.tema == tema,
                            onClick = { store.atualizar(prefs.copy(tema = tema)) },
                            label = { Text(tema.name) },
                        )
                    }
                }
            }

            Column {
                Text("Fonte", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FontePreferencia.entries.forEach { fonte ->
                        FilterChip(
                            selected = prefs.fonte == fonte,
                            onClick = { store.atualizar(prefs.copy(fonte = fonte)) },
                            label = { Text(fonte.name) },
                        )
                    }
                }
            }

            Column {
                Text("Tamanho da fonte: ${(prefs.escalaFonte * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = prefs.escalaFonte,
                    onValueChange = { store.atualizar(prefs.copy(escalaFonte = it)) },
                    valueRange = 0.85f..1.4f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row {
                Checkbox(
                    checked = prefs.espacamentoAumentado,
                    onCheckedChange = { store.atualizar(prefs.copy(espacamentoAumentado = it)) },
                )
                Text("Espaçamento aumentado", modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
