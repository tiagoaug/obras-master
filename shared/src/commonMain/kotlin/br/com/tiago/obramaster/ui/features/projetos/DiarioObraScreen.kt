package br.com.tiago.obramaster.ui.features.projetos

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.domain.DiarioObra
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import br.com.tiago.obramaster.ui.components.decodeImageBitmap
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiarioObraScreen(
    projetoId: String,
    onVoltar: () -> Unit,
    viewModel: DiarioObraViewModel = koinInject { parametersOf(projetoId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarForm by remember { mutableStateOf(false) }
    var registroEditando by remember { mutableStateOf<DiarioObra?>(null) }

    fun nomeEtapa(id: String?) = uiState.etapas.firstOrNull { it.id == id }?.nome

    LcrudListScaffold(
        titulo = "Diário de Obra",
        itens = uiState.registros,
        filtro = { registro, busca -> registro.texto.contains(busca, ignoreCase = true) },
        itemHeadline = { "${DataFormatter.formatar(it.data)}${nomeEtapa(it.etapaId)?.let { nome -> " · $nome" } ?: ""}" },
        itemSupporting = { it.texto.take(80) + (it.clima?.let { c -> " · $c" } ?: "") },
        onItemClicado = { registroEditando = it; mostrarForm = true },
        onNovoClicado = { registroEditando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        exportar = { itens ->
            ExportableDocument(
                titulo = "Diário de Obra",
                colunas = listOf("Data", "Etapa", "Registro", "Clima"),
                linhas = itens.map { listOf(DataFormatter.formatar(it.data), nomeEtapa(it.etapaId).orEmpty(), it.texto, it.clima.orEmpty()) },
            )
        },
    )

    if (mostrarForm) {
        NovoRegistroDiarioScreen(
            existente = registroEditando,
            etapas = uiState.etapas,
            onImagemDisponivel = { viewModel.imagemDisponivel() },
            onTirarFoto = { viewModel.tirarFoto() },
            onEscolherDaGaleria = { viewModel.escolherDaGaleria() },
            onCarregarFoto = { chave -> viewModel.carregarFoto(chave) },
            onSalvar = { etapaId, data, texto, clima, fotos ->
                viewModel.salvar(registroEditando, etapaId, data, texto, clima, fotos)
                mostrarForm = false
            },
            onVoltar = { mostrarForm = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoRegistroDiarioScreen(
    existente: DiarioObra?,
    etapas: List<Etapa>,
    onImagemDisponivel: suspend () -> Boolean,
    onTirarFoto: suspend () -> String?,
    onEscolherDaGaleria: suspend () -> String?,
    onCarregarFoto: suspend (String) -> ByteArray?,
    onSalvar: (String?, Long, String, String?, List<String>) -> Unit,
    onVoltar: () -> Unit,
) {
    var etapaId by remember { mutableStateOf(existente?.etapaId) }
    var dataTexto by remember { mutableStateOf(existente?.data?.let(DataFormatter::formatar) ?: DataFormatter.formatar(Clock.System.now().toEpochMilliseconds())) }
    var texto by remember { mutableStateOf(existente?.texto ?: "") }
    var clima by remember { mutableStateOf(existente?.clima ?: "") }
    var fotos by remember { mutableStateOf(existente?.fotosUris ?: emptyList()) }
    var camaraDisponivel by remember { mutableStateOf(false) }
    var acaoFotoPendente by remember { mutableStateOf<AcaoFoto?>(null) }

    LaunchedEffect(Unit) { camaraDisponivel = onImagemDisponivel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existente == null) "Novo registro" else "Editar registro") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Etapa (opcional)", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = etapaId == null, onClick = { etapaId = null }, label = { Text("Nenhuma") })
                etapas.forEach { etapa ->
                    FilterChip(selected = etapaId == etapa.id, onClick = { etapaId = etapa.id }, label = { Text(etapa.nome) })
                }
            }

            OutlinedTextField(dataTexto, { dataTexto = it }, label = { Text("Data (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(clima, { clima = it }, label = { Text("Clima (opcional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(texto, { texto = it }, label = { Text("Observações do dia") }, minLines = 4, modifier = Modifier.fillMaxWidth())

            Text("Fotos", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fotos) { chave ->
                    FotoThumbnail(chave = chave, carregar = onCarregarFoto, onRemover = { fotos = fotos - chave })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (camaraDisponivel) {
                    OutlinedButton(enabled = acaoFotoPendente == null, onClick = { acaoFotoPendente = AcaoFoto.CAMERA }) { Text("Tirar foto") }
                }
                OutlinedButton(enabled = acaoFotoPendente == null, onClick = { acaoFotoPendente = AcaoFoto.GALERIA }) { Text("Da galeria") }
            }
            if (acaoFotoPendente != null) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            Button(
                onClick = {
                    val data = DataFormatter.parseOuNulo(dataTexto) ?: Clock.System.now().toEpochMilliseconds()
                    onSalvar(etapaId, data, texto, clima.ifBlank { null }, fotos)
                },
                enabled = texto.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar registro") }
        }
    }

    // Efeito de captura/seleção de foto disparado fora da árvore de clique pra manter o
    // callback suspend fora do onClick (Compose não permite suspend direto em onClick).
    val acao = acaoFotoPendente
    if (acao != null) {
        CapturaFotoEffect(
            acao = acao,
            tirarFoto = onTirarFoto,
            escolherDaGaleria = onEscolherDaGaleria,
            onResultado = { chave -> chave?.let { fotos = fotos + it }; acaoFotoPendente = null },
        )
    }
}

private enum class AcaoFoto { CAMERA, GALERIA }

@Composable
private fun CapturaFotoEffect(
    acao: AcaoFoto,
    tirarFoto: suspend () -> String?,
    escolherDaGaleria: suspend () -> String?,
    onResultado: (String?) -> Unit,
) {
    LaunchedEffect(acao) {
        val chave = if (acao == AcaoFoto.CAMERA) tirarFoto() else escolherDaGaleria()
        onResultado(chave)
    }
}

@Composable
private fun FotoThumbnail(chave: String, carregar: suspend (String) -> ByteArray?, onRemover: () -> Unit) {
    var bytes by remember(chave) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(chave) { bytes = carregar(chave) }

    Box(Modifier.size(72.dp)) {
        bytes?.let { dados ->
            val bitmap = remember(dados) { decodeImageBitmap(dados) }
            bitmap?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize()) }
        }
        IconButton(onClick = onRemover, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Remover foto")
        }
    }
}
