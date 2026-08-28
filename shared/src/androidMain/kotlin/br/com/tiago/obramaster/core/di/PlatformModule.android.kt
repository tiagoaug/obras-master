package br.com.tiago.obramaster.core.di

import android.content.Context
import br.com.tiago.obramaster.data.db.DatabaseDriverFactory
import br.com.tiago.obramaster.data.db.createDatabase
import br.com.tiago.obramaster.data.repository.AberturaRepository
import br.com.tiago.obramaster.data.repository.ArquivoImportadoRepository
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.ComodoRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.CorRepository
import br.com.tiago.obramaster.data.repository.EmpresaRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.ParedeRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.PlantaBaixaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.RateioLancamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightAberturaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightArquivoImportadoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightCategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.SqlDelightCentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightColaboradorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightComodoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightContaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightCorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightEmpresaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightEtapaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightLancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.SqlDelightMaterialRepository
import br.com.tiago.obramaster.data.repository.SqlDelightModuleConfigRepository
import br.com.tiago.obramaster.data.repository.SqlDelightParedeRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPermissaoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPessoaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPlantaBaixaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightProjetoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightRateioLancamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightUnidadeMedidaRepository
import br.com.tiago.obramaster.data.repository.UnidadeMedidaRepository
import br.com.tiago.obramaster.platform.AppSettingsFactory
import br.com.tiago.obramaster.platform.ContactsProvider
import br.com.tiago.obramaster.platform.FilePicker
import br.com.tiago.obramaster.platform.ImagePicker
import br.com.tiago.obramaster.platform.ImageStore
import br.com.tiago.obramaster.platform.PdfImageRenderer
import br.com.tiago.obramaster.platform.PdfVectorExtractor
import org.koin.dsl.module

fun platformModule(context: Context) = module {
    single { DatabaseDriverFactory(context) }
    single { AppSettingsFactory(context) }
    single { createDatabase(get()) }
    single { ContactsProvider(context) }
    single { ImagePicker(context) }
    single { ImageStore(context) }
    single { FilePicker(context) }
    single { PdfImageRenderer(context) }
    single { PdfVectorExtractor(context) }

    single<ColaboradorRepository> { SqlDelightColaboradorRepository(get()) }
    single<PermissaoRepository> { SqlDelightPermissaoRepository(get()) }
    single<ModuleConfigRepository> { SqlDelightModuleConfigRepository(get()) }
    single<EmpresaRepository> { SqlDelightEmpresaRepository(get()) }
    single<ContaRepository> { SqlDelightContaRepository(get()) }
    single<PessoaRepository> { SqlDelightPessoaRepository(get()) }
    single<CorRepository> { SqlDelightCorRepository(get()) }
    single<MaterialRepository> { SqlDelightMaterialRepository(get()) }
    single<UnidadeMedidaRepository> { SqlDelightUnidadeMedidaRepository(get()) }
    single<ProjetoRepository> { SqlDelightProjetoRepository(get()) }
    single<EtapaRepository> { SqlDelightEtapaRepository(get()) }
    single<PlantaBaixaRepository> { SqlDelightPlantaBaixaRepository(get()) }
    single<ComodoRepository> { SqlDelightComodoRepository(get()) }
    single<ParedeRepository> { SqlDelightParedeRepository(get()) }
    single<AberturaRepository> { SqlDelightAberturaRepository(get()) }
    single<ArquivoImportadoRepository> { SqlDelightArquivoImportadoRepository(get()) }
    single<CategoriaFinanceiraRepository> { SqlDelightCategoriaFinanceiraRepository(get()) }
    single<CentroDeCustoRepository> { SqlDelightCentroDeCustoRepository(get()) }
    single<LancamentoFinanceiroRepository> { SqlDelightLancamentoFinanceiroRepository(get()) }
    single<RateioLancamentoRepository> { SqlDelightRateioLancamentoRepository(get()) }
}
