package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Colaborador
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

/** Coleção de nível raiz `colaboradores/{uid}` (não aninhada em `empresas/{empresaId}/...` como
 * os recursos de negócio) porque o primeiro passo depois do login é justamente descobrir a quais
 * empresas aquele uid pertence — um documento raiz por uid resolve isso com uma leitura só, sem
 * query. `empresaIds` é lista porque um Gestor pode administrar mais de uma empresa (ver "Minhas
 * Empresas" em Configurações); colaborador comum sempre tem lista de 1 item.
 * `criar`/`buscarComEmpresaIds`/`adicionarEmpresa` (chamados pelo SessionManager antes do
 * EmpresaContexto existir, ou fora do escopo de uma única empresa) não fazem parte da interface
 * comum ColaboradorRepository. */
@Serializable
private data class ColaboradorDoc(
    val empresaIds: List<String> = emptyList(),
    val nome: String = "",
    val email: String = "",
    val ativo: Boolean = true,
    val ehGestor: Boolean = false,
)

class FirestoreColaboradorRepository(
    private val empresaContexto: EmpresaContexto,
) : ColaboradorRepository {

    private fun colecao() = Firebase.firestore.collection("colaboradores")

    private fun ColaboradorDoc.toDomain(id: String) = Colaborador(id = id, nome = nome, email = email, ativo = ativo, ehGestor = ehGestor)
    private fun Colaborador.toDoc(empresaIds: List<String>) = ColaboradorDoc(empresaIds = empresaIds, nome = nome, email = email, ativo = ativo, ehGestor = ehGestor)

    suspend fun buscarComEmpresaIds(uid: String): Pair<Colaborador, List<String>>? {
        val doc = colecao().document(uid).get()
        if (!doc.exists) return null
        val dados = doc.data(ColaboradorDoc.serializer())
        return dados.toDomain(uid) to dados.empresaIds
    }

    suspend fun criar(uid: String, empresaIds: List<String>, colaborador: Colaborador) {
        colecao().document(uid).set(ColaboradorDoc.serializer(), colaborador.toDoc(empresaIds))
    }

    /** "Minhas Empresas" (Configurações) — Gestor cria mais uma empresa além das que já administra. */
    suspend fun adicionarEmpresa(uid: String, empresaId: String) {
        colecao().document(uid).update("empresaIds" to FieldValue.arrayUnion(empresaId))
    }

    override suspend fun listarAtivos(): List<Colaborador> =
        colecao().where { "empresaIds" contains empresaContexto.exigir() }.get().documents
            .map { it.data(ColaboradorDoc.serializer()).toDomain(it.id) }
            .filter { it.ativo }

    override suspend fun buscarPorId(id: String): Colaborador? {
        val doc = colecao().document(id).get()
        return if (doc.exists) doc.data(ColaboradorDoc.serializer()).toDomain(id) else null
    }

    override suspend fun buscarPorLogin(login: String): Colaborador? {
        val encontrados = colecao().where { "email" equalTo login }.get().documents
        return encontrados.firstOrNull()?.let { it.data(ColaboradorDoc.serializer()).toDomain(it.id) }
    }

    override suspend fun atualizar(colaborador: Colaborador) {
        colecao().document(colaborador.id).update("nome" to colaborador.nome, "ehGestor" to colaborador.ehGestor)
    }

    override suspend fun desativar(id: String) {
        colecao().document(id).update("ativo" to false)
    }

    // Ver o comentário em FirestoreCollection.observarTodos(): listeners aqui podem sobreviver a
    // um logout (nada cancela viewModelScope ao navegar pra longe da tela); sem o catch, o
    // PERMISSION_DENIED que o Firestore devolve nesse caso derruba o app inteiro.
    override fun observarAtivos(): Flow<List<Colaborador>> =
        colecao().where { "empresaIds" contains empresaContexto.exigir() }.snapshots
            .map { snapshot -> snapshot.documents.map { it.data(ColaboradorDoc.serializer()).toDomain(it.id) }.filter { it.ativo } }
            .catch { e -> if (e is FirebaseFirestoreException) emit(emptyList()) else throw e }
}
