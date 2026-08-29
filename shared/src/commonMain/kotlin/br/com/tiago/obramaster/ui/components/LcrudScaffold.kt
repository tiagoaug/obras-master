package br.com.tiago.obramaster.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.ui.components.export.ExportarBottomSheet

/** SPEC_OBRA_MASTER.md §5.3 — padrão de telas (Lista/Formulário/Detalhe) reaproveitado em todo cadastro. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LcrudListScaffold(
    titulo: String,
    itens: List<T>,
    filtro: (T, String) -> Boolean,
    itemHeadline: (T) -> String,
    itemSupporting: (T) -> String?,
    onItemClicado: (T) -> Unit,
    onNovoClicado: () -> Unit,
    onExcluirConfirmado: (T) -> Unit,
    onVoltar: (() -> Unit)? = null,
    acoesTopBar: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    podeExcluir: (T) -> Boolean = { true },
    exportar: ((List<T>) -> ExportableDocument)? = null,
) {
    var busca by remember { mutableStateOf("") }
    var itemParaExcluir by remember { mutableStateOf<T?>(null) }
    var documentoParaExportar by remember { mutableStateOf<ExportableDocument?>(null) }
    val itensFiltrados = if (busca.isBlank()) itens else itens.filter { filtro(it, busca) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (onVoltar != null) {
                        IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    }
                },
                actions = {
                    acoesTopBar()
                    if (exportar != null) {
                        IconButton(onClick = { documentoParaExportar = exportar(itensFiltrados) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Exportar")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNovoClicado) { Icon(Icons.Filled.Add, contentDescription = "Novo") }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
            LazyColumn(Modifier.fillMaxWidth()) {
                items(itensFiltrados) { item ->
                    Card(
                        onClick = { onItemClicado(item) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        ListItem(
                            headlineContent = { Text(itemHeadline(item)) },
                            supportingContent = itemSupporting(item)?.let { texto -> { Text(texto) } },
                            trailingContent = if (podeExcluir(item)) {
                                {
                                    IconButton(onClick = { itemParaExcluir = item }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                                    }
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }

    itemParaExcluir?.let { item ->
        AlertDialog(
            onDismissRequest = { itemParaExcluir = null },
            title = { Text("Excluir?") },
            text = { Text("Tem certeza que deseja excluir \"${itemHeadline(item)}\"?") },
            confirmButton = {
                Button(onClick = { onExcluirConfirmado(item); itemParaExcluir = null }) { Text("Excluir") }
            },
            dismissButton = { OutlinedButton(onClick = { itemParaExcluir = null }) { Text("Cancelar") } },
        )
    }

    documentoParaExportar?.let { doc ->
        ExportarBottomSheet(doc = doc, onDismiss = { documentoParaExportar = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LcrudFormScaffold(
    titulo: String,
    onVoltar: () -> Unit,
    podeSalvar: Boolean,
    onSalvar: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = onSalvar,
                    enabled = podeSalvar,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) { Text("Salvar") }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            content = content,
        )
    }
}
