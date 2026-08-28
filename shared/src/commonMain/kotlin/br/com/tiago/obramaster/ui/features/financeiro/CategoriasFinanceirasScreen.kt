package br.com.tiago.obramaster.ui.features.financeiro

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasFinanceirasScreen(onVoltar: () -> Unit, viewModel: CategoriasFinanceirasViewModel = koinInject()) {
    val categorias by viewModel.categorias.collectAsState()
    val emArvore = viewModel.ordenadasEmArvore(categorias)
    var editando by remember { mutableStateOf<CategoriaFinanceira?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Categorias Financeiras",
        itens = emArvore,
        filtro = { categoria, busca -> categoria.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { if (it.categoriaPaiId != null) "— ${it.nome}" else it.nome },
        itemSupporting = { it.tipo.name + (if (it.padraoDoSistema) " · padrão do sistema" else "") },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        podeExcluir = { !it.padraoDoSistema },
    )

    if (mostrarForm) {
        val atual = editando
        var nome by remember { mutableStateOf(atual?.nome ?: "") }
        var tipo by remember { mutableStateOf(atual?.tipo ?: TipoLancamento.DESPESA) }
        var natureza by remember { mutableStateOf(atual?.naturezaPadrao ?: NaturezaLancamento.CONTABIL) }
        var categoriaPaiId by remember { mutableStateOf(atual?.categoriaPaiId) }
        var cor by remember { mutableStateOf(atual?.cor ?: "#90A4AE") }
        val categoriasRaiz = categorias.filter { it.categoriaPaiId == null && it.id != atual?.id }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Nova categoria" else "Editar categoria", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())

                Text("Tipo", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoLancamento.entries.forEach { opcao ->
                        FilterChip(selected = tipo == opcao, onClick = { tipo = opcao }, label = { Text(opcao.name) })
                    }
                }

                Text("Natureza padrão", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NaturezaLancamento.entries.forEach { opcao ->
                        FilterChip(selected = natureza == opcao, onClick = { natureza = opcao }, label = { Text(opcao.name) })
                    }
                }

                Text("Categoria pai (opcional)", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = categoriaPaiId == null, onClick = { categoriaPaiId = null }, label = { Text("Nenhuma") })
                    categoriasRaiz.forEach { pai ->
                        FilterChip(selected = categoriaPaiId == pai.id, onClick = { categoriaPaiId = pai.id }, label = { Text(pai.nome) })
                    }
                }

                OutlinedTextField(cor, { cor = it }, label = { Text("Cor (#RRGGBB)") }, modifier = Modifier.fillMaxWidth())
                val corValida = runCatching { Color(("FF" + cor.removePrefix("#")).toLong(16)) }.getOrNull()
                if (corValida != null) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(corValida))
                }

                Button(
                    onClick = {
                        viewModel.salvar(atual, nome, tipo, natureza, categoriaPaiId, cor)
                        mostrarForm = false
                    },
                    enabled = nome.isNotBlank() && cor.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
