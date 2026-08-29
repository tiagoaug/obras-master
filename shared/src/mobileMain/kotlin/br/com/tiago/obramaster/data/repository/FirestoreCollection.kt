package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer

/** Fase 10 (pivô Firebase) — todo recurso de negócio vira `empresas/{empresaId}/{colecao}/{id}`
 * (ver plano da migração); esse helper concentra a mecânica repetida (montar a referência da
 * coleção escopada pela empresa atual, ler/escrever com o serializer certo) pra cada
 * `Firestore*Repository` ficar só implementando a interface específica dela por cima disso, sem
 * reescrever a mesma leitura/escrita 19 vezes. */
class FirestoreCollection<T : Any>(
    private val empresaContexto: EmpresaContexto,
    private val nomeColecao: String,
    private val serializer: KSerializer<T>,
) {
    fun colecao(): CollectionReference = Firebase.firestore.collection("empresas/${empresaContexto.exigir()}/$nomeColecao")

    suspend fun listarTodos(): List<T> = colecao().get().documents.map { it.data(serializer) }

    suspend fun buscarPorId(id: String): T? {
        val doc = colecao().document(id).get()
        return if (doc.exists) doc.data(serializer) else null
    }

    suspend fun salvar(id: String, item: T) {
        colecao().document(id).set(serializer, item)
    }

    suspend fun atualizarCampos(id: String, vararg campos: Pair<String, Any?>) {
        colecao().document(id).update(*campos)
    }

    suspend fun excluir(id: String) {
        colecao().document(id).delete()
    }

    fun observarTodos(): Flow<List<T>> =
        colecao().snapshots.map { snapshot -> snapshot.documents.map { it.data(serializer) } }

    suspend fun consultar(filtro: Query.() -> Query): List<T> =
        filtro(colecao()).get().documents.map { it.data(serializer) }
}
