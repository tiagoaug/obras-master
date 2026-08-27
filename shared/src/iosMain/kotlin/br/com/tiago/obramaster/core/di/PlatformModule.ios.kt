package br.com.tiago.obramaster.core.di

import br.com.tiago.obramaster.data.db.DatabaseDriverFactory
import br.com.tiago.obramaster.data.db.createDatabase
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightColaboradorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightModuleConfigRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPermissaoRepository
import br.com.tiago.obramaster.platform.AppSettingsFactory
import org.koin.dsl.module

val platformModule = module {
    single { DatabaseDriverFactory() }
    single { AppSettingsFactory() }
    single { createDatabase(get()) }

    single<ColaboradorRepository> { SqlDelightColaboradorRepository(get()) }
    single<PermissaoRepository> { SqlDelightPermissaoRepository(get()) }
    single<ModuleConfigRepository> { SqlDelightModuleConfigRepository(get()) }
}
