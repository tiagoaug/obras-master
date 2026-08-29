package br.com.tiago.obramaster.ui.features.financeiro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

/** Tipos que o usuário cria manualmente aqui — PROJETO nasce sozinho junto com um Projeto (§3). */
private val TIPOS_MANUAIS = listOf(TipoCentroDeCusto.ADMINISTRATIVO, TipoCentroDeCusto.COMERCIAL, TipoCentroDeCusto.OUTRO)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentrosDeCustoScreen(onVoltar: () -> Unit, viewModel: CentrosDeCustoViewModel = koinInject()) {
    val centros by viewModel.centros.collectAsState()
    var editando by remember { mutableStateOf<CentroDeCusto?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Centros de Custo",
        itens = centros,
        filtro = { centro, busca -> centro.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.nome },
        itemSupporting = { it.tipo.name },
        onItemClicado = { centro -> if (centro.tipo != TipoCentroDeCusto.PROJETO) { editando = centro; mostrarForm = true } },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        podeExcluir = { it.tipo != TipoCentroDeCusto.PROJETO },
        exportar = { itens ->
            ExportableDocument(
                titulo = "Centros de Custo",
                colunas = listOf("Nome", "Tipo"),
                linhas = itens.map { listOf(it.nome, it.tipo.name) },
            )
        },
    )

    if (mostrarForm) {
        val atual = editando
        var nome by remember { mutableStateOf(atual?.nome ?: "") }
        var tipo by remember { mutableStateOf(atual?.tipo ?: TipoCentroDeCusto.ADMINISTRATIVO) }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Novo centro de custo" else "Editar centro de custo", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())

                Text("Tipo", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TIPOS_MANUAIS.forEach { opcao ->
                        FilterChip(selected = tipo == opcao, onClick = { tipo = opcao }, label = { Text(opcao.name) })
                    }
                }

                Button(
                    onClick = { viewModel.salvar(atual, nome, tipo); mostrarForm = false },
                    enabled = nome.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
