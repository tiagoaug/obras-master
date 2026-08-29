package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.ConviteColaborador

/** Fase 10 (pivô Firebase) — implementação Firestore só em mobileMain (ver
 * FirestoreConviteColaboradorRepository); Web ainda não tem Firebase (ver SessionManagerIndisponivel). */
interface ConviteColaboradorRepository {
    suspend fun criar(convite: ConviteColaborador)
    suspend fun listarPendentesDaEmpresa(): List<ConviteColaborador>
    suspend fun buscarPorEmail(email: String): ConviteColaborador?
    suspend fun remover(id: String)
}
