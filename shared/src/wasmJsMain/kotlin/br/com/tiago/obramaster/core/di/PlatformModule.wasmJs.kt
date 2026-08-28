package br.com.tiago.obramaster.core.di

import br.com.tiago.obramaster.data.repository.AberturaRepository
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.ComodoRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.CorRepository
import br.com.tiago.obramaster.data.repository.EmpresaRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.InMemoryAberturaRepository
import br.com.tiago.obramaster.data.repository.InMemoryColaboradorRepository
import br.com.tiago.obramaster.data.repository.InMemoryComodoRepository
import br.com.tiago.obramaster.data.repository.InMemoryContaRepository
import br.com.tiago.obramaster.data.repository.InMemoryCorRepository
import br.com.tiago.obramaster.data.repository.InMemoryEmpresaRepository
import br.com.tiago.obramaster.data.repository.InMemoryEtapaRepository
import br.com.tiago.obramaster.data.repository.InMemoryMaterialRepository
import br.com.tiago.obramaster.data.repository.InMemoryModuleConfigRepository
import br.com.tiago.obramaster.data.repository.InMemoryParedeRepository
import br.com.tiago.obramaster.data.repository.InMemoryPermissaoRepository
import br.com.tiago.obramaster.data.repository.InMemoryPessoaRepository
import br.com.tiago.obramaster.data.repository.InMemoryPlantaBaixaRepository
import br.com.tiago.obramaster.data.repository.InMemoryProjetoRepository
import br.com.tiago.obramaster.data.repository.InMemoryUnidadeMedidaRepository
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.ParedeRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.PlantaBaixaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.UnidadeMedidaRepository
import br.com.tiago.obramaster.platform.AppSettingsFactory
import br.com.tiago.obramaster.platform.ContactsProvider
import br.com.tiago.obramaster.platform.ImagePicker
import br.com.tiago.obramaster.platform.ImageStore
import org.koin.dsl.module

val platformModule = module {
    single { AppSettingsFactory() }
    single { ContactsProvider() }
    single { ImagePicker() }
    single { ImageStore() }

    single<ColaboradorRepository> { InMemoryColaboradorRepository() }
    single<PermissaoRepository> { InMemoryPermissaoRepository() }
    single<ModuleConfigRepository> { InMemoryModuleConfigRepository() }
    single<EmpresaRepository> { InMemoryEmpresaRepository() }
    single<ContaRepository> { InMemoryContaRepository() }
    single<PessoaRepository> { InMemoryPessoaRepository() }
    single<CorRepository> { InMemoryCorRepository() }
    single<MaterialRepository> { InMemoryMaterialRepository() }
    single<UnidadeMedidaRepository> { InMemoryUnidadeMedidaRepository() }
    single<ProjetoRepository> { InMemoryProjetoRepository() }
    single<EtapaRepository> { InMemoryEtapaRepository() }
    single<PlantaBaixaRepository> { InMemoryPlantaBaixaRepository() }
    single<ComodoRepository> { InMemoryComodoRepository() }
    single<ParedeRepository> { InMemoryParedeRepository() }
    single<AberturaRepository> { InMemoryAberturaRepository() }
}
