package br.com.tiago.obramaster.core.di

import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.EmpresaRepository
import br.com.tiago.obramaster.data.repository.InMemoryColaboradorRepository
import br.com.tiago.obramaster.data.repository.InMemoryContaRepository
import br.com.tiago.obramaster.data.repository.InMemoryEmpresaRepository
import br.com.tiago.obramaster.data.repository.InMemoryModuleConfigRepository
import br.com.tiago.obramaster.data.repository.InMemoryPermissaoRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.platform.AppSettingsFactory
import org.koin.dsl.module

val platformModule = module {
    single { AppSettingsFactory() }

    single<ColaboradorRepository> { InMemoryColaboradorRepository() }
    single<PermissaoRepository> { InMemoryPermissaoRepository() }
    single<ModuleConfigRepository> { InMemoryModuleConfigRepository() }
    single<EmpresaRepository> { InMemoryEmpresaRepository() }
    single<ContaRepository> { InMemoryContaRepository() }
}
