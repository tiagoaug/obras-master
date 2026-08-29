package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.domain.Permissao
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Fase 10 (pivô Firebase) — `empresas/{empresaId}/permissoes/{colaboradorId}_{moduleId}`. */
@kotlinx.serialization.Serializable
private data class PermissaoDoc(val colaboradorId: String = "", val moduleId: String = "", val nivel: String = "")

class FirestorePermissaoRepository(
    private val empresaContexto: EmpresaContexto,
) : PermissaoRepository {

    private fun colecao() = Firebase.firestore.collection("empresas/${empresaContexto.exigir()}/permissoes")
    private fun idDoc(colaboradorId: String, moduleId: String) = "${colaboradorId}_$moduleId"

    private fun PermissaoDoc.toDomain() = Permissao(colaboradorId = colaboradorId, moduleId = moduleId, nivel = NivelPermissao.valueOf(nivel))

    override suspend fun listarTodas(): List<Permissao> =
        colecao().get().documents.map { it.data(PermissaoDoc.serializer()).toDomain() }

    override suspend fun listarPorColaborador(colaboradorId: String): List<Permissao> =
        colecao().where { "colaboradorId" equalTo colaboradorId }.get().documents.map { it.data(PermissaoDoc.serializer()).toDomain() }

    override suspend fun definir(colaboradorId: String, moduleId: String, nivel: NivelPermissao) {
        colecao().document(idDoc(colaboradorId, moduleId))
            .set(PermissaoDoc.serializer(), PermissaoDoc(colaboradorId, moduleId, nivel.name))
    }

    override suspend fun removerTodasDoColaborador(colaboradorId: String) {
        colecao().where { "colaboradorId" equalTo colaboradorId }.get().documents.forEach { it.reference.delete() }
    }

    // Ver o comentário em FirestoreCollection.observarTodos() sobre listeners sobrevivendo a logout.
    override fun observarTodas(): Flow<List<Permissao>> =
        colecao().snapshots
            .map { snapshot -> snapshot.documents.map { it.data(PermissaoDoc.serializer()).toDomain() } }
            .catch { e -> if (e is FirebaseFirestoreException) emit(emptyList()) else throw e }
}
