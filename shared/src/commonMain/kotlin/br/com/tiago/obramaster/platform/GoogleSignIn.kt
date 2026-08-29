package br.com.tiago.obramaster.platform

/** Fase 10 (pivô Firebase) — obtém o idToken do Google (fluxo nativo de cada plataforma) pra
 * trocar por credencial do Firebase Auth (ver FirebaseAuthGateway.entrarComGoogle). Ainda não
 * implementado de verdade em nenhuma plataforma: falta o Web Client ID (vem no google-services.json
 * só depois que o provedor Google é habilitado no console — ver server/README.md/histórico da
 * sessão) e, no Android, uma Activity pra hospedar o picker de conta. Retorna null = indisponível,
 * mesmo padrão de FilePicker.ios.kt. */
expect suspend fun obterGoogleIdToken(): String?
