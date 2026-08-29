package br.com.tiago.obramaster.ui.features.areaexecutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.DocumentoTecnicoRepository
import br.com.tiago.obramaster.domain.CategoriaNorma
import br.com.tiago.obramaster.domain.DocumentoTecnico
import br.com.tiago.obramaster.domain.TipoDocumento
import br.com.tiago.obramaster.platform.DocumentStore
import br.com.tiago.obramaster.platform.FilePicker
import br.com.tiago.obramaster.platform.PdfOpener
import br.com.tiago.obramaster.platform.PdfTextExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** SPEC_AREA_EXECUTOR.md §2.2, §3, §4 — anexar/listar/abrir/excluir PDFs (Fase 8.6) + extração
 * de texto em segundo plano e busca full-text via índice FTS (Fase 8.7). */
class BibliotecaManuaisViewModel(
    private val repository: DocumentoTecnicoRepository,
    private val documentStore: DocumentStore,
    private val filePicker: FilePicker,
    private val pdfOpener: PdfOpener,
    private val pdfTextExtractor: PdfTextExtractor,
) : ViewModel() {

    private val todos: StateFlow<List<DocumentoTecnico>> =
        repository.observarTodos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val busca = MutableStateFlow("")
    private val resultadosBusca = MutableStateFlow<List<DocumentoTecnico>?>(null)

    /** Documentos exibidos na lista: todos, ou o resultado da busca full-text quando há query ativa. */
    val documentos: StateFlow<List<DocumentoTecnico>> =
        combine(todos, busca, resultadosBusca) { lista, query, resultados ->
            if (query.isBlank()) lista else resultados ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro.asStateFlow()

    private var arquivoEscolhidoBytes: ByteArray? = null
    private var arquivoEscolhidoNome: String = ""

    suspend fun pickerDisponivel(): Boolean = filePicker.isAvailable()
    suspend fun abrirDisponivel(): Boolean = pdfOpener.isAvailable()

    fun limparErro() {
        _erro.value = null
    }

    /** Busca por conteúdo dos PDFs (Fase 8.7) — mesma query da aba Normas, disparada do hub. */
    fun buscar(query: String) {
        busca.value = query
        if (query.isBlank()) return
        viewModelScope.launch { resultadosBusca.value = repository.buscarPorTexto(query) }
    }

    /** Abre o seletor de arquivo e valida a extensão .pdf — devolve o nome sugerido pro
     * formulário de anexar, ou null se o usuário cancelou ou escolheu algo que não é PDF. */
    suspend fun escolherPdf(): String? {
        val arquivo = filePicker.escolherArquivo(extensoesAceitas = listOf("pdf")) ?: return null
        if (!arquivo.nomeArquivo.lowercase().endsWith(".pdf")) {
            _erro.value = "Escolha um arquivo .pdf"
            return null
        }
        arquivoEscolhidoBytes = arquivo.bytes
        arquivoEscolhidoNome = arquivo.nomeArquivo
        return arquivo.nomeArquivo
    }

    @OptIn(ExperimentalUuidApi::class)
    fun confirmarAnexo(
        nome: String,
        tipo: TipoDocumento,
        categoria: CategoriaNorma,
        tags: List<String>,
        normaVinculadaId: String?,
        vinculadaMaterialId: String?,
        onConcluido: () -> Unit,
    ) {
        val bytes = arquivoEscolhidoBytes ?: return
        val nomeOriginal = arquivoEscolhidoNome
        viewModelScope.launch {
            val chave = documentStore.salvar(bytes, nomeOriginal)
            val id = Uuid.random().toString()
            repository.salvar(
                DocumentoTecnico(
                    id = id,
                    nome = nome.ifBlank { nomeOriginal },
                    tipo = tipo,
                    categoria = categoria,
                    arquivoKey = chave,
                    tamanhoBytes = bytes.size.toLong(),
                    normaVinculadaId = normaVinculadaId,
                    vinculadaMaterialId = vinculadaMaterialId,
                    tags = tags,
                    adicionadoEm = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            arquivoEscolhidoBytes = null
            arquivoEscolhidoNome = ""
            onConcluido()

            // Extração de texto (Fase 8.7) roda em segundo plano — não bloqueia o "Salvar" do
            // formulário nem falha o anexo se der errado, só não fica pesquisável por conteúdo.
            val texto = pdfTextExtractor.extrairTexto(bytes)
            if (texto.isNotBlank()) repository.atualizarTextoExtraido(id, texto)
        }
    }

    fun abrir(documento: DocumentoTecnico) {
        viewModelScope.launch {
            val bytes = documentStore.abrir(documento.arquivoKey)
            val sucesso = bytes != null && pdfOpener.abrir(bytes, documento.nome)
            if (!sucesso) _erro.value = "Não foi possível abrir \"${documento.nome}\"."
        }
    }

    fun excluir(documento: DocumentoTecnico) {
        viewModelScope.launch {
            repository.excluir(documento.id)
            documentStore.excluir(documento.arquivoKey)
        }
    }
}
