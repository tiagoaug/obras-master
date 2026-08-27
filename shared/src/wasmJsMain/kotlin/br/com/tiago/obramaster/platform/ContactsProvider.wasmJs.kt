package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ContatoImportado

// Web não tem acesso à agenda do dispositivo — SPEC_OBRA_MASTER_KMP.md §4.1 já prevê isso:
// "Importar da agenda" vira "Importar contatos (CSV/vCard)" (ver CsvVCardParser).
actual class ContactsProvider {
    actual suspend fun isAvailable(): Boolean = false

    actual suspend fun pickContacts(): List<ContatoImportado> = emptyList()
}
