package br.com.tiago.obramaster.data.db

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    // sql.js (driver web do SQLDelight) inicializa de forma assíncrona (carrega um .wasm),
    // incompatível com a assinatura síncrona de create(). Web é online-first (seção 6.1 da
    // SPEC_OBRA_MASTER_KMP.md) — este driver local só passa a ser necessário a partir da
    // Fase 10 (cache leve), quando resolvemos o init assíncrono de verdade.
    actual fun create(): SqlDriver =
        throw NotImplementedError("Driver local Web será implementado na Fase 10 (cache/sync).")
}
