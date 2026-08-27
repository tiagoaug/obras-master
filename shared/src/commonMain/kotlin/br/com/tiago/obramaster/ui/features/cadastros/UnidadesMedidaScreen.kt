package br.com.tiago.obramaster.ui.features.cadastros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.UnidadeMedida
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadesMedidaScreen(onVoltar: () -> Unit, viewModel: UnidadesMedidaViewModel = koinInject()) {
    val unidades by viewModel.unidades.collectAsState()
    var editando by remember { mutableStateOf<UnidadeMedida?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Unidades de Medida",
        itens = unidades,
        filtro = { unidade, busca -> unidade.nome.contains(busca, ignoreCase = true) || unidade.sigla.contains(busca, ignoreCase = true) },
        itemHeadline = { it.sigla },
        itemSupporting = { it.nome },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
    )

    if (mostrarForm) {
        val atual = editando
        var sigla by remember { mutableStateOf(atual?.sigla ?: "") }
        var nome by remember { mutableStateOf(atual?.nome ?: "") }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Nova unidade" else "Editar unidade", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(sigla, { sigla = it }, label = { Text("Sigla (ex.: m², kg, sc)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome (ex.: Metro quadrado)") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = { viewModel.salvar(atual, sigla, nome); mostrarForm = false },
                    enabled = sigla.isNotBlank() && nome.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
