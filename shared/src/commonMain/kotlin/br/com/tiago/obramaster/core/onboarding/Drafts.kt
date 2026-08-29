package br.com.tiago.obramaster.core.onboarding

import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.domain.TipoConta
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DadosEmpresaDraft(
    val nome: String = "",
    val logoUri: String? = null,
    val cnpj: String? = null,
    val telefone: String? = null,
    val endereco: String? = null,
    val cidade: String? = null,
)

// senha nunca é persistida em disco (fica de fora do JSON salvo como rascunho) — só vive em
// memória durante a sessão atual do onboarding. Fechar o app no meio pede a senha de novo.
@Serializable
data class GestorDraft(
    val nome: String = "",
    val email: String = "",
    @Transient val senha: String = "",
)

@Serializable
data class ContaDraft(
    val nome: String,
    val tipo: TipoConta,
    val saldoInicialCentavos: Long,
)

// Fase 10 (pivô Firebase) — sem senha: o colaborador é convidado por e-mail, não cadastrado com
// senha pelo Gestor (ver ConviteColaborador em :core e a nota em FirebaseAuthGateway).
@Serializable
data class ColaboradorDraft(
    val nome: String,
    val email: String,
    val permissoes: Map<String, NivelPermissao> = emptyMap(), // moduleId -> nivel
)

@Serializable
data class ProjetoDraft(
    val nome: String,
    val endereco: String = "",
    val areaConstruidaM2Vezes100: Long? = null, // evita Double no rascunho serializado; /100.0 na UI
    val orcamentoTotalCentavos: Long = 0,
)
