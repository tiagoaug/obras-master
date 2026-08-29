package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.ConviteColaborador
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

/** Fase 10 (pivô Firebase) — coleção raiz `convites/{id}`: o colaborador convidado ainda não tem
 * uid (não existe conta ainda), por isso não pode viver em `colaboradores/{uid}`. Ver
 * ConviteColaborador em :core e a nota em FirebaseAuthGateway sobre por que o convite existe. */
class FirestoreConviteColaboradorRepository(
    private val empresaContexto: EmpresaContexto,
) : ConviteColaboradorRepository {
    private fun colecao() = Firebase.firestore.collection("convites")

    override suspend fun criar(convite: ConviteColaborador) {
        colecao().document(convite.id).set(ConviteColaborador.serializer(), convite)
    }

    override suspend fun listarPendentesDaEmpresa(): List<ConviteColaborador> =
        colecao().where { "empresaId" equalTo empresaContexto.exigir() }.get().documents
            .map { it.data(ConviteColaborador.serializer()) }

    override suspend fun buscarPorEmail(email: String): ConviteColaborador? =
        colecao().where { "email" equalTo email }.get().documents
            .firstOrNull()?.data(ConviteColaborador.serializer())

    override suspend fun remover(id: String) {
        colecao().document(id).delete()
    }
}
