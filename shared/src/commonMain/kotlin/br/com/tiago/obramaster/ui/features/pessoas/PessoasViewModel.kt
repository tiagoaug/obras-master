package br.com.tiago.obramaster.ui.features.pessoas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.util.CsvVCardParser
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.domain.ContatoImportado
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.platform.ContactsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PessoasViewModel(
    private val repository: PessoaRepository,
    private val contactsProvider: ContactsProvider,
) : ViewModel() {

    val pessoas: StateFlow<List<Pessoa>> = repository.observarAtivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importandoDaAgenda = MutableStateFlow(false)
    val importandoDaAgenda: StateFlow<Boolean> = _importandoDaAgenda

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        existente: Pessoa?,
        nome: String,
        tags: Set<TagPessoa>,
        telefone: String?,
        email: String?,
        endereco: String?,
        documento: String?,
        observacoes: String?,
    ) {
        viewModelScope.launch {
            if (existente != null) {
                repository.atualizar(
                    existente.copy(
                        nome = nome, tags = tags, telefone = telefone, email = email,
                        endereco = endereco, documento = documento, observacoes = observacoes,
                    ),
                )
            } else {
                repository.salvar(
                    Pessoa(
                        id = Uuid.random().toString(), nome = nome, tags = tags, telefone = telefone,
                        email = email, endereco = endereco, documento = documento, observacoes = observacoes,
                    ),
                )
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }

    suspend fun agendaDisponivel(): Boolean = contactsProvider.isAvailable()

    fun buscarContatosDaAgenda(onResultado: (List<ContatoImportado>) -> Unit) {
        viewModelScope.launch {
            _importandoDaAgenda.value = true
            val contatos = contactsProvider.pickContacts()
            _importandoDaAgenda.value = false
            onResultado(contatos)
        }
    }

    fun parsearArquivoImportacao(conteudo: String): List<ContatoImportado> = CsvVCardParser.parsear(conteudo)

    @OptIn(ExperimentalUuidApi::class)
    fun importarSelecionados(selecionados: List<ContatoImportado>, tagPadrao: TagPessoa) {
        viewModelScope.launch {
            selecionados.forEach { contato ->
                repository.salvar(
                    Pessoa(
                        id = Uuid.random().toString(),
                        nome = contato.nome,
                        tags = setOf(tagPadrao),
                        telefone = contato.telefone,
                        email = contato.email,
                        fotoUri = contato.fotoUri,
                    ),
                )
            }
        }
    }
}
