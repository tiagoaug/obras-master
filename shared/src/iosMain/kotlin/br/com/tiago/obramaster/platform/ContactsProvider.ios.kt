package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ContatoImportado

// Import real via CNContactStore fica pendente: é cinterop com Contacts.framework que eu não
// tenho como compilar nem validar nesta máquina (sem Mac/Xcode). Em vez de arriscar código
// quebrado sem forma de testar, isAvailable() = false por enquanto — o botão de importar
// contatos some da UI no iOS (mesma "regra de degradação" da própria spec), sem travar nada.
// Android já tem a implementação real (ContactsProvider.android.kt) como referência.
actual class ContactsProvider {
    actual suspend fun isAvailable(): Boolean = false

    actual suspend fun pickContacts(): List<ContatoImportado> = emptyList()
}
