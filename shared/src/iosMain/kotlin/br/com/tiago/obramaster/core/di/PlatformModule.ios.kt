package br.com.tiago.obramaster.core.di

import br.com.tiago.obramaster.data.db.DatabaseDriverFactory
import br.com.tiago.obramaster.data.db.createDatabase
import br.com.tiago.obramaster.data.repository.AberturaRepository
import br.com.tiago.obramaster.data.repository.ArquivoImportadoRepository
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.ComodoRepository
import br.com.tiago.obramaster.data.repository.ConfigBDIRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.CorRepository
import br.com.tiago.obramaster.data.repository.DiarioObraRepository
import br.com.tiago.obramaster.data.repository.EmpresaRepository
import br.com.tiago.obramaster.data.repository.EquipeRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.FornecedorRepository
import br.com.tiago.obramaster.data.repository.FuncionarioRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.data.repository.MovimentoContaRepository
import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.data.repository.OrcamentoRepository
import br.com.tiago.obramaster.data.repository.PagamentoRepository
import br.com.tiago.obramaster.data.repository.ParedeRepository
import br.com.tiago.obramaster.data.repository.PedidoCompraRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.PlantaBaixaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.RateioLancamentoRepository
import br.com.tiago.obramaster.data.repository.RegistroTrabalhoRepository
import br.com.tiago.obramaster.data.repository.RetencaoLancamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightAberturaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightArquivoImportadoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightCategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.SqlDelightCentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightColaboradorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightComodoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightConfigBDIRepository
import br.com.tiago.obramaster.data.repository.SqlDelightContaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightCorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightDiarioObraRepository
import br.com.tiago.obramaster.data.repository.SqlDelightEmpresaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightEquipeRepository
import br.com.tiago.obramaster.data.repository.SqlDelightEtapaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightFornecedorRepository
import br.com.tiago.obramaster.data.repository.SqlDelightFuncionarioRepository
import br.com.tiago.obramaster.data.repository.SqlDelightLancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.SqlDelightMaterialRepository
import br.com.tiago.obramaster.data.repository.SqlDelightMovimentoContaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightModuleConfigRepository
import br.com.tiago.obramaster.data.repository.SqlDelightOrcamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPagamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightParedeRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPedidoCompraRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPermissaoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPessoaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPlantaBaixaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightProjetoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightRateioLancamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightRegistroTrabalhoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightRetencaoLancamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightTarefaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightUnidadeMedidaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightVendaRepository
import br.com.tiago.obramaster.data.repository.TarefaRepository
import br.com.tiago.obramaster.data.repository.UnidadeMedidaRepository
import br.com.tiago.obramaster.data.repository.VendaRepository
import br.com.tiago.obramaster.platform.AppSettingsFactory
import br.com.tiago.obramaster.platform.ContactsProvider
import br.com.tiago.obramaster.platform.FilePicker
import br.com.tiago.obramaster.platform.ImagePicker
import br.com.tiago.obramaster.platform.ImageStore
import br.com.tiago.obramaster.platform.PdfImageRenderer
import br.com.tiago.obramaster.platform.PdfVectorExtractor
import org.koin.dsl.module

val platformModule = module {
    single { DatabaseDriverFactory() }
    single { AppSettingsFactory() }
    single { createDatabase(get()) }
    single { ContactsProvider() }
    single { ImagePicker() }
    single { ImageStore() }
    single { FilePicker() }
    single { PdfImageRenderer() }
    single { PdfVectorExtractor() }

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
    single<MovimentoContaRepository> { SqlDelightMovimentoContaRepository(get()) }
    single<FuncionarioRepository> { SqlDelightFuncionarioRepository(get()) }
    single<EquipeRepository> { SqlDelightEquipeRepository(get()) }
    single<RegistroTrabalhoRepository> { SqlDelightRegistroTrabalhoRepository(get()) }
    single<RetencaoLancamentoRepository> { SqlDelightRetencaoLancamentoRepository(get()) }
    single<PagamentoRepository> { SqlDelightPagamentoRepository(get()) }
    single<FornecedorRepository> { SqlDelightFornecedorRepository(get()) }
    single<PedidoCompraRepository> { SqlDelightPedidoCompraRepository(get()) }
    single<ConfigBDIRepository> { SqlDelightConfigBDIRepository(get()) }
    single<OrcamentoRepository> { SqlDelightOrcamentoRepository(get()) }
    single<VendaRepository> { SqlDelightVendaRepository(get()) }
    single<TarefaRepository> { SqlDelightTarefaRepository(get()) }
    single<DiarioObraRepository> { SqlDelightDiarioObraRepository(get()) }
}
