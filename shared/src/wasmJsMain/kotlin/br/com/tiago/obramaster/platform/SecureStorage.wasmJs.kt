package br.com.tiago.obramaster.platform

import kotlinx.browser.localStorage

// Provisório: localStorage não é "seguro" como cookie httpOnly (§3.4 da SPEC_OBRA_MASTER_KMP.md).
// Sem backend ainda (Fase 10), não existe quem sete o cookie — troca fica registrada para lá.
actual object SecureStorage {
    actual fun put(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    actual fun get(key: String): String? = localStorage.getItem(key)

    actual fun remove(key: String) {
        localStorage.removeItem(key)
    }
}
