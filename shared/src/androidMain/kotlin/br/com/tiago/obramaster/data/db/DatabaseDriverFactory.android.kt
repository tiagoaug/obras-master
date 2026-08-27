package br.com.tiago.obramaster.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(private val context: Context) {
    // SQLDelight só gera a classe Database (com o Schema) quando existe pelo menos um .sq —
    // a partir da Fase 1, quando as primeiras entidades entrarem. Até lá o contrato existe,
    // mas a implementação real (AndroidSqliteDriver contra o Schema gerado) fica pendente.
    actual fun create(): SqlDriver =
        throw NotImplementedError("Schema SQLDelight chega na Fase 1, junto das primeiras entidades.")
}
