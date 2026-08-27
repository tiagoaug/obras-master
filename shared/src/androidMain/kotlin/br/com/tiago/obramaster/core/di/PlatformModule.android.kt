package br.com.tiago.obramaster.core.di

import android.content.Context
import br.com.tiago.obramaster.data.db.DatabaseDriverFactory
import br.com.tiago.obramaster.data.db.createDatabase
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.CorRepository
import br.com.tiago.obramaster.data.repository.EmpresaRepository
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightColaboradorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightContaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightCorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightEmpresaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightMaterialRepository
import br.com.tiago.obramaster.data.repository.SqlDelightModuleConfigRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPermissaoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPessoaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightUnidadeMedidaRepository
import br.com.tiago.obramaster.data.repository.UnidadeMedidaRepository
import br.com.tiago.obramaster.platform.AppSettingsFactory
import br.com.tiago.obramaster.platform.ContactsProvider
import org.koin.dsl.module

fun platformModule(context: Context) = module {
    single { DatabaseDriverFactory(context) }
    single { AppSettingsFactory(context) }
    single { createDatabase(get()) }
    single { ContactsProvider(context) }

    single<ColaboradorRepository> { SqlDelightColaboradorRepository(get()) }
    single<PermissaoRepository> { SqlDelightPermissaoRepository(get()) }
    single<ModuleConfigRepository> { SqlDelightModuleConfigRepository(get()) }
    single<EmpresaRepository> { SqlDelightEmpresaRepository(get()) }
    single<ContaRepository> { SqlDelightContaRepository(get()) }
    single<PessoaRepository> { SqlDelightPessoaRepository(get()) }
    single<CorRepository> { SqlDelightCorRepository(get()) }
    single<MaterialRepository> { SqlDelightMaterialRepository(get()) }
    single<UnidadeMedidaRepository> { SqlDelightUnidadeMedidaRepository(get()) }
}
