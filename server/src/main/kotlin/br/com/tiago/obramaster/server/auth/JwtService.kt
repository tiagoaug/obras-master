package br.com.tiago.obramaster.server.auth

import br.com.tiago.obramaster.server.Env
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

enum class TipoToken { ACCESS, REFRESH }

/** Claims mínimas para o servidor validar permissão sem ir ao banco a cada request:
 * `colaboradorId` (subject), `empresaId` (multi-tenant) e `ehGestor` (atalho — Gestor tem acesso
 * total, ver PermissionEngine em :core). Nível de permissão por módulo continua sendo consultado
 * no banco (Permissoes), não cabe no token sem inflar demais. */
object JwtService {
    private val algorithm = Algorithm.HMAC256(Env.jwtSecret)

    fun gerarToken(colaboradorId: String, empresaId: String, ehGestor: Boolean, tipo: TipoToken): String {
        val expiracaoMillis = when (tipo) {
            TipoToken.ACCESS -> Env.ACCESS_TOKEN_EXPIRATION_MILLIS
            TipoToken.REFRESH -> Env.REFRESH_TOKEN_EXPIRATION_MILLIS
        }
        return JWT.create()
            .withIssuer(Env.jwtIssuer)
            .withAudience(Env.jwtAudience)
            .withSubject(colaboradorId)
            .withClaim("empresaId", empresaId)
            .withClaim("ehGestor", ehGestor)
            .withClaim("tipo", tipo.name)
            .withExpiresAt(Date(System.currentTimeMillis() + expiracaoMillis))
            .sign(algorithm)
    }

    val verifier = JWT.require(algorithm)
        .withIssuer(Env.jwtIssuer)
        .withAudience(Env.jwtAudience)
        .build()
}
