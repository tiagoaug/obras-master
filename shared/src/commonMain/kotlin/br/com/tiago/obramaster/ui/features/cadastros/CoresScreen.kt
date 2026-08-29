package br.com.tiago.obramaster.ui.features.cadastros

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.Cor
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoresScreen(onVoltar: () -> Unit, viewModel: CoresViewModel = koinInject()) {
    val cores by viewModel.cores.collectAsState()
    var editando by remember { mutableStateOf<Cor?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Cores",
        itens = cores,
        filtro = { cor, busca -> cor.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.nome },
        itemSupporting = { it.hex + (it.codigoFabricante?.let { cod -> " · $cod" } ?: "") },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        exportar = { itens ->
            ExportableDocument(
                titulo = "Cores",
                colunas = listOf("Nome", "Hex", "Código do fabricante"),
                linhas = itens.map { listOf(it.nome, it.hex, it.codigoFabricante.orEmpty()) },
            )
        },
    )

    if (mostrarForm) {
        val atual = editando
        var nome by remember { mutableStateOf(atual?.nome ?: "") }
        var hex by remember { mutableStateOf(atual?.hex ?: "#000000") }
        var codigoFabricante by remember { mutableStateOf(atual?.codigoFabricante ?: "") }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Nova cor" else "Editar cor", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(hex, { hex = it }, label = { Text("Hex (#RRGGBB)") }, modifier = Modifier.fillMaxWidth())
                val corValida = runCatching { Color(("FF" + hex.removePrefix("#")).toLong(16)) }.getOrNull()
                if (corValida != null) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(corValida))
                }
                OutlinedTextField(
                    codigoFabricante,
                    { codigoFabricante = it },
                    label = { Text("Código do fabricante (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        viewModel.salvar(atual, nome, hex, codigoFabricante.ifBlank { null })
                        mostrarForm = false
                    },
                    enabled = nome.isNotBlank() && hex.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
