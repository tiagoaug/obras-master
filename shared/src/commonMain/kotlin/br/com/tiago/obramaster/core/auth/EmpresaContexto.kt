package br.com.tiago.obramaster.core.auth

/** Fase 10 (pivô Firebase) — os repositórios Firestore de recursos de negócio vivem em
 * `empresas/{empresaId}/...` (ver plano da migração); em vez de passar `empresaId` em todo método
 * de todo repositório (19 entidades), o `SessionManager` define isso aqui uma vez, logo após
 * descobrir a empresa do uid autenticado, e os repositórios leem daqui. */
class EmpresaContexto {
    var empresaId: String? = null
        private set

    fun definir(empresaId: String) {
        this.empresaId = empresaId
    }

    fun limpar() {
        empresaId = null
    }

    fun exigir(): String = empresaId ?: error("Nenhuma empresa no contexto — usuário não está logado")
}
