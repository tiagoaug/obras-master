package br.com.tiago.obramaster.data.db

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    // Mesma situação do actual Android: Schema gerado só existe a partir da Fase 1.
    actual fun create(): SqlDriver =
        throw NotImplementedError("Schema SQLDelight chega na Fase 1, junto das primeiras entidades.")
}
