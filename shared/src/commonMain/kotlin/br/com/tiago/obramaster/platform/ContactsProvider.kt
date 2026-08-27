package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ContatoImportado

/** SPEC_OBRA_MASTER_KMP.md §4 — Web não tem agenda; usa CSV/vCard (fora deste contrato). */
expect class ContactsProvider {
    suspend fun isAvailable(): Boolean
    suspend fun pickContacts(): List<ContatoImportado>
}
