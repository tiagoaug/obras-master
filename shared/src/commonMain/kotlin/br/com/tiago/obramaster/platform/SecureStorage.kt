package br.com.tiago.obramaster.platform

/** Armazenamento seguro de dados sensíveis (token de sessão) — ver SPEC_OBRA_MASTER_KMP.md §3.4/§4. */
expect object SecureStorage {
    fun put(key: String, value: String)
    fun get(key: String): String?
    fun remove(key: String)
}
