package br.com.tiago.obramaster.ui.features.assistente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.assistant.ManualSection
import org.koin.compose.koinInject

/** SPEC_ASSISTENTE_IA.md §5.1 — o manual renderizado dentro do app ("Ajuda"), acessível de
 * Configurações e como destino do "📖 Ver no Manual" do Assistente (abre já na seção certa). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjudaScreen(
    secaoInicialId: String? = null,
    onVoltar: () -> Unit,
    viewModel: AjudaViewModel = koinInject(),
) {
    val secoes by viewModel.secoes.collectAsState()
    var busca by remember { mutableStateOf("") }
    var expandidaId by remember { mutableStateOf(secaoInicialId) }
    val listState: LazyListState = rememberLazyListState()

    LaunchedEffect(secoes, secaoInicialId) {
        if (secaoInicialId != null) {
            val indice = secoes.indexOfFirst { it.id == secaoInicialId }
            if (indice >= 0) listState.animateScrollToItem(indice)
        }
    }

    val filtradas = if (busca.isBlank()) {
        secoes
    } else {
        val termo = busca.lowercase()
        secoes.filter { it.titulo.lowercase().contains(termo) || it.conteudo.lowercase().contains(termo) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajuda — Manual do Programa") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar no manual") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            ) {
                items(filtradas, key = { it.id }) { secao ->
                    SecaoManualItem(
                        secao = secao,
                        expandida = expandidaId == secao.id,
                        onToggle = { expandidaId = if (expandidaId == secao.id) null else secao.id },
                    )
                }
            }
        }
    }
}

@Composable
private fun SecaoManualItem(secao: ManualSection, expandida: Boolean, onToggle: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onToggle)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(secao.titulo, style = MaterialTheme.typography.titleSmall)
            if (expandida) {
                Text(secao.conteudo, style = MaterialTheme.typography.bodyMedium)
                secao.exemploPratico?.let { exemplo ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF5DF))) {
                        Text(
                            "💡 Exemplo prático: $exemplo",
                            modifier = Modifier.padding(8.dp).background(Color.Transparent),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
