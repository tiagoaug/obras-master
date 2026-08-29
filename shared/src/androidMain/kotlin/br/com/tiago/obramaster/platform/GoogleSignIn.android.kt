package br.com.tiago.obramaster.platform

// Web Client ID (client_type 3) do google-services.json — criado automaticamente pelo Firebase
// quando o provedor Google foi habilitado em Authentication → Sign-in method (Fase 10, pivô
// Firebase). Não é segredo: é um identificador público do OAuth client, igual à api_key.
private const val GOOGLE_WEB_CLIENT_ID = "1012935650002-9pgs5okfn2d88m4n2qiieesg70usggon.apps.googleusercontent.com"

actual suspend fun obterGoogleIdToken(): String? = GoogleSignInBridge.obterIdToken(GOOGLE_WEB_CLIENT_ID)
