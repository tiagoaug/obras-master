package br.com.tiago.obramaster.server

/** SPEC_OBRA_MASTER_KMP.md §6.2 — segredos sempre via variável de ambiente, nunca hardcoded.
 * `jwtSecret` tem um valor de desenvolvimento padrão só para rodar localmente sem configurar nada;
 * em produção é obrigatório sobrescrever via env var (documentado no README do server). */
object Env {
    val jwtSecret: String = System.getenv("OBRAMASTER_JWT_SECRET") ?: "dev-secret-troque-em-producao"
    val jwtIssuer: String = System.getenv("OBRAMASTER_JWT_ISSUER") ?: "obramaster-server"
    val jwtAudience: String = System.getenv("OBRAMASTER_JWT_AUDIENCE") ?: "obramaster-clients"
    val dbUrl: String = System.getenv("OBRAMASTER_DB_URL") ?: "jdbc:h2:./data/obramaster-server;AUTO_SERVER=TRUE"
    val dbUser: String = System.getenv("OBRAMASTER_DB_USER") ?: "sa"
    val dbPassword: String = System.getenv("OBRAMASTER_DB_PASSWORD") ?: ""
    val port: Int = System.getenv("OBRAMASTER_PORT")?.toIntOrNull() ?: 8080

    const val ACCESS_TOKEN_EXPIRATION_MILLIS = 15 * 60 * 1000L // 15 minutos
    const val REFRESH_TOKEN_EXPIRATION_MILLIS = 30 * 24 * 60 * 60 * 1000L // 30 dias
}
