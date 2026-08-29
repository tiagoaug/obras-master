package br.com.tiago.obramaster.core.auth

import br.com.tiago.obramaster.data.repository.ConviteColaboradorRepository
import br.com.tiago.obramaster.data.repository.FirestoreColaboradorRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseSessionManager(
    private val firestoreColaboradorRepository: FirestoreColaboradorRepository,
    private val conviteColaboradorRepository: ConviteColaboradorRepository,
    private val permissaoRepository: PermissaoRepository,
    private val empresaContexto: EmpresaContexto,
) : SessionManager {

    private val _colaboradorLogado = MutableStateFlow<Colaborador?>(null)
    override val colaboradorLogado: StateFlow<Colaborador?> = _colaboradorLogado.asStateFlow()

    override suspend fun restaurar() {
        val uid = FirebaseAuthGateway.uidAtual ?: return
        carregarColaboradorDoUid(uid)
    }

    override suspend fun login(email: String, senha: String): SessionManager.LoginResult =
        quandoAutenticado(FirebaseAuthGateway.entrarComEmailSenha(email, senha))

    override suspend fun entrarComGoogle(idToken: String): SessionManager.LoginResult {
        val resultado = FirebaseAuthGateway.entrarComGoogle(idToken)
        if (resultado !is ResultadoAuth.Sucesso) return quandoAutenticado(resultado)
        carregarColaboradorDoUid(resultado.uid)?.let { return it }
        aceitarConviteSeExistir(resultado.uid, resultado.email, nomeSugerido = null)?.let { return it }
        return SessionManager.LoginResult.ContaSemEmpresaVinculada
    }

    private suspend fun quandoAutenticado(resultado: ResultadoAuth): SessionManager.LoginResult = when (resultado) {
        is ResultadoAuth.Sucesso -> carregarColaboradorDoUid(resultado.uid) ?: SessionManager.LoginResult.ContaSemEmpresaVinculada
        ResultadoAuth.CredenciaisInvalidas -> SessionManager.LoginResult.LoginOuSenhaInvalidos
        ResultadoAuth.EmailJaCadastrado -> SessionManager.LoginResult.Erro("E-mail já cadastrado")
        is ResultadoAuth.Erro -> SessionManager.LoginResult.Erro(resultado.mensagem)
    }

    private suspend fun carregarColaboradorDoUid(uid: String): SessionManager.LoginResult.Sucesso? {
        val encontrado = firestoreColaboradorRepository.buscarComEmpresaId(uid) ?: return null
        val (colaborador, empresaId) = encontrado
        empresaContexto.definir(empresaId)
        _colaboradorLogado.value = colaborador
        return SessionManager.LoginResult.Sucesso(colaborador)
    }

    /** Cria o Colaborador de verdade a partir de um ConviteColaborador pendente pra [email] —
     * usado tanto por entrarComGoogle (primeiro acesso via Google) quanto por
     * criarContaEAceitarConvite (primeiro acesso via e-mail/senha). */
    private suspend fun aceitarConviteSeExistir(uid: String, email: String, nomeSugerido: String?): SessionManager.LoginResult.Sucesso? {
        val convite = conviteColaboradorRepository.buscarPorEmail(email) ?: return null
        val colaborador = Colaborador(id = uid, nome = nomeSugerido ?: convite.nome, email = email, ativo = true, ehGestor = convite.ehGestor)
        empresaContexto.definir(convite.empresaId)
        firestoreColaboradorRepository.criar(uid, convite.empresaId, colaborador)
        convite.permissoes.forEach { (moduleId, nivel) -> permissaoRepository.definir(uid, moduleId, nivel) }
        conviteColaboradorRepository.remover(convite.id)
        _colaboradorLogado.value = colaborador
        return SessionManager.LoginResult.Sucesso(colaborador)
    }

    override suspend fun cadastrarGestor(nome: String, email: String, senha: String, empresaId: String): SessionManager.LoginResult {
        val resultado = FirebaseAuthGateway.cadastrarComEmailSenha(email, senha)
        if (resultado !is ResultadoAuth.Sucesso) return quandoAutenticado(resultado)
        val colaborador = Colaborador(id = resultado.uid, nome = nome, email = resultado.email, ativo = true, ehGestor = true)
        firestoreColaboradorRepository.criar(resultado.uid, empresaId, colaborador)
        empresaContexto.definir(empresaId)
        _colaboradorLogado.value = colaborador
        return SessionManager.LoginResult.Sucesso(colaborador)
    }

    override suspend fun criarContaEAceitarConvite(nome: String, email: String, senha: String): SessionManager.LoginResult {
        val resultado = FirebaseAuthGateway.cadastrarComEmailSenha(email, senha)
        if (resultado !is ResultadoAuth.Sucesso) return quandoAutenticado(resultado)
        val aceito = aceitarConviteSeExistir(resultado.uid, resultado.email, nomeSugerido = nome)
        if (aceito != null) return aceito
        FirebaseAuthGateway.apagarContaAtual()
        return SessionManager.LoginResult.Erro("Nenhum convite encontrado para este e-mail")
    }

    override suspend fun logout() {
        FirebaseAuthGateway.sair()
        _colaboradorLogado.value = null
        empresaContexto.limpar()
    }
}
