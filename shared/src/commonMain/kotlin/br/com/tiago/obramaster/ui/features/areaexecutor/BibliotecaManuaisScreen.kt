package br.com.tiago.obramaster.ui.features.areaexecutor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.domain.CategoriaNorma
import br.com.tiago.obramaster.domain.DocumentoTecnico
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.domain.NormaABNT
import br.com.tiago.obramaster.domain.TipoDocumento
import br.com.tiago.obramaster.domain.labelPtBr
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** SPEC_AREA_EXECUTOR.md §2.2, §4, §6 — lista, anexa, abre e exclui PDFs próprios do usuário
 * (Fase 8.6), busca por conteúdo (Fase 8.7) e vínculo opcional com um material do cadastro
 * (Fase 8.8 — útil pra achar a ficha técnica de um material rápido). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliotecaManuaisContent(
    viewModel: BibliotecaManuaisViewModel,
    normas: List<NormaABNT>,
    buscaAtiva: Boolean = false,
    materialRepository: MaterialRepository = koinInject(),
) {
    val documentos by viewModel.documentos.collectAsState()
    val erro by viewModel.erro.collectAsState()
    var pickerDisponivel by remember { mutableStateOf(true) }
    var abrirDisponivel by remember { mutableStateOf(true) }
    var nomeSugerido by remember { mutableStateOf<String?>(null) }
    var documentoParaExcluir by remember { mutableStateOf<DocumentoTecnico?>(null) }
    var materiais by remember { mutableStateOf<List<Material>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        pickerDisponivel = viewModel.pickerDisponivel()
        abrirDisponivel = viewModel.abrirDisponivel()
        materiais = materialRepository.listarAtivos()
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
            if (pickerDisponivel) {
                Button(
                    onClick = { scope.launch { nomeSugerido = viewModel.escolherPdf() } },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Anexar PDF") }
            } else {
                Text(
                    "Anexar PDF ainda não está disponível nesta plataforma.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            erro?.let { mensagem ->
                Text(mensagem, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }

        when {
            documentos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (buscaAtiva) "Nenhum manual encontrado com esse conteúdo." else "Nenhum manual anexado ainda.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> LazyColumn(Modifier.fillMaxWidth()) {
                items(documentos) { documento ->
                    Card(
                        onClick = { if (abrirDisponivel) viewModel.abrir(documento) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        ListItem(
                            headlineContent = { Text(documento.nome) },
                            supportingContent = { Text("${documento.tipo.labelPtBr} • ${documento.categoria.labelPtBr}") },
                            trailingContent = {
                                IconButton(onClick = { documentoParaExcluir = documento }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    nomeSugerido?.let { nome ->
        AnexarPdfFormSheet(
            nomeInicial = nome,
            normas = normas,
            materiais = materiais,
            onDismiss = { nomeSugerido = null },
            onConfirmar = { nomeFinal, tipo, categoria, tags, normaVinculadaId, materialVinculadoId ->
                viewModel.confirmarAnexo(nomeFinal, tipo, categoria, tags, normaVinculadaId, materialVinculadoId) { nomeSugerido = null }
            },
        )
    }

    documentoParaExcluir?.let { documento ->
        AlertDialog(
            onDismissRequest = { documentoParaExcluir = null },
            title = { Text("Excluir?") },
            text = { Text("Tem certeza que deseja excluir \"${documento.nome}\"? O arquivo será removido.") },
            confirmButton = {
                Button(onClick = { viewModel.excluir(documento); documentoParaExcluir = null }) { Text("Excluir") }
            },
            dismissButton = { OutlinedButton(onClick = { documentoParaExcluir = null }) { Text("Cancelar") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnexarPdfFormSheet(
    nomeInicial: String,
    normas: List<NormaABNT>,
    materiais: List<Material>,
    onDismiss: () -> Unit,
    onConfirmar: (nome: String, tipo: TipoDocumento, categoria: CategoriaNorma, tags: List<String>, normaVinculadaId: String?, materialVinculadoId: String?) -> Unit,
) {
    var nome by remember { mutableStateOf(nomeInicial) }
    var tipo by remember { mutableStateOf(TipoDocumento.OUTRO) }
    var categoria by remember { mutableStateOf(CategoriaNorma.OUTRA) }
    var tagsTexto by remember { mutableStateOf("") }
    var normaVinculada by remember { mutableStateOf<NormaABNT?>(null) }
    var materialVinculado by remember { mutableStateOf<Material?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Anexar PDF", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())

            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoDocumento.entries.forEach { opcao ->
                    FilterChip(selected = tipo == opcao, onClick = { tipo = opcao }, label = { Text(opcao.labelPtBr) })
                }
            }

            Text("Categoria", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoriaNorma.entries.forEach { opcao ->
                    FilterChip(selected = categoria == opcao, onClick = { categoria = opcao }, label = { Text(opcao.labelPtBr) })
                }
            }

            OutlinedTextField(
                tagsTexto, { tagsTexto = it },
                label = { Text("Tags (separadas por vírgula, opcional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (normas.isNotEmpty()) {
                Text("Este PDF é a norma... (opcional)", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = normaVinculada == null, onClick = { normaVinculada = null }, label = { Text("Nenhuma") })
                    normas.forEach { norma ->
                        FilterChip(selected = normaVinculada == norma, onClick = { normaVinculada = norma }, label = { Text(norma.numero) })
                    }
                }
            }

            if (materiais.isNotEmpty()) {
                Text("Este PDF é o manual de qual material? (opcional)", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = materialVinculado == null, onClick = { materialVinculado = null }, label = { Text("Nenhum") })
                    materiais.forEach { material ->
                        FilterChip(selected = materialVinculado == material, onClick = { materialVinculado = material }, label = { Text(material.nome) })
                    }
                }
            }

            Button(
                onClick = {
                    val tags = tagsTexto.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onConfirmar(nome, tipo, categoria, tags, normaVinculada?.id, materialVinculado?.id)
                },
                enabled = nome.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar") }
        }
    }
}
