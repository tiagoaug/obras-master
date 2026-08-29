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
import br.com.tiago.obramaster.data.repository.MetaRepository
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
import br.com.tiago.obramaster.data.repository.SqlDelightComodoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightDiarioObraRepository
import br.com.tiago.obramaster.data.repository.SqlDelightMovimentoContaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightParedeRepository
import br.com.tiago.obramaster.data.repository.SqlDelightPlantaBaixaRepository
import br.com.tiago.obramaster.data.repository.SqlDelightRateioLancamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightRetencaoLancamentoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightTarefaRepository
import br.com.tiago.obramaster.data.repository.TarefaRepository
import br.com.tiago.obramaster.data.repository.UnidadeMedidaRepository
import br.com.tiago.obramaster.data.repository.VendaRepository
import br.com.tiago.obramaster.data.repository.DocumentoTecnicoRepository
import br.com.tiago.obramaster.data.repository.SqlDelightDocumentoTecnicoRepository
import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.core.auth.FirebaseSessionManager
import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.data.repository.ConviteColaboradorRepository
import br.com.tiago.obramaster.data.repository.FirestoreColaboradorRepository
import br.com.tiago.obramaster.data.repository.FirestoreConviteColaboradorRepository
import br.com.tiago.obramaster.data.repository.FirestoreCategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.FirestoreCentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.FirestoreContaRepository
import br.com.tiago.obramaster.data.repository.FirestoreConfigBDIRepository
import br.com.tiago.obramaster.data.repository.FirestoreCorRepository
import br.com.tiago.obramaster.data.repository.FirestoreEmpresaRepository
import br.com.tiago.obramaster.data.repository.FirestoreMetaRepository
import br.com.tiago.obramaster.data.repository.FirestoreModuleConfigRepository
import br.com.tiago.obramaster.data.repository.FirestoreOrcamentoRepository
import br.com.tiago.obramaster.data.repository.FirestorePedidoCompraRepository
import br.com.tiago.obramaster.data.repository.FirestoreVendaRepository
import br.com.tiago.obramaster.data.repository.FirestoreEquipeRepository
import br.com.tiago.obramaster.data.repository.FirestoreMaterialRepository
import br.com.tiago.obramaster.data.repository.FirestoreUnidadeMedidaRepository
import br.com.tiago.obramaster.data.repository.FirestoreEtapaRepository
import br.com.tiago.obramaster.data.repository.FirestoreFornecedorRepository
import br.com.tiago.obramaster.data.repository.FirestoreFuncionarioRepository
import br.com.tiago.obramaster.data.repository.FirestorePagamentoRepository
import br.com.tiago.obramaster.data.repository.FirestorePessoaRepository
import br.com.tiago.obramaster.data.repository.FirestoreRegistroTrabalhoRepository
import br.com.tiago.obramaster.data.repository.FirestoreLancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.FirestorePermissaoRepository
import br.com.tiago.obramaster.data.repository.FirestoreProjetoRepository
import br.com.tiago.obramaster.platform.AppSettingsFactory
import br.com.tiago.obramaster.platform.ContactsProvider
import br.com.tiago.obramaster.platform.DocumentStore
import br.com.tiago.obramaster.platform.FileExporter
import br.com.tiago.obramaster.platform.FilePicker
import br.com.tiago.obramaster.platform.ImagePicker
import br.com.tiago.obramaster.platform.ImageStore
import br.com.tiago.obramaster.platform.PdfImageRenderer
import br.com.tiago.obramaster.platform.PdfOpener
import br.com.tiago.obramaster.platform.PdfTextExtractor
import br.com.tiago.obramaster.platform.PdfVectorExtractor
import org.koin.dsl.bind
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
    single { DocumentStore(context) }
    single { PdfOpener(context) }
    single { PdfTextExtractor(context) }
    single { FileExporter(context) }

    single { EmpresaContexto() }
    single { FirestoreColaboradorRepository(get()) } bind ColaboradorRepository::class
    single { FirestorePermissaoRepository(get()) } bind PermissaoRepository::class
    single { FirestoreConviteColaboradorRepository(get()) } bind ConviteColaboradorRepository::class
    single<SessionManager> { FirebaseSessionManager(get(), get(), get(), get()) }
    single<ModuleConfigRepository> { FirestoreModuleConfigRepository(get()) }
    single<EmpresaRepository> { FirestoreEmpresaRepository(get()) }
    single<ContaRepository> { FirestoreContaRepository(get()) }
    single<PessoaRepository> { FirestorePessoaRepository(get()) }
    single<CorRepository> { FirestoreCorRepository(get()) }
    single<MaterialRepository> { FirestoreMaterialRepository(get()) }
    single<UnidadeMedidaRepository> { FirestoreUnidadeMedidaRepository(get()) }
    single<ProjetoRepository> { FirestoreProjetoRepository(get()) }
    single<EtapaRepository> { FirestoreEtapaRepository(get()) }
    single<PlantaBaixaRepository> { SqlDelightPlantaBaixaRepository(get()) }
    single<ComodoRepository> { SqlDelightComodoRepository(get()) }
    single<ParedeRepository> { SqlDelightParedeRepository(get()) }
    single<AberturaRepository> { SqlDelightAberturaRepository(get()) }
    single<ArquivoImportadoRepository> { SqlDelightArquivoImportadoRepository(get()) }
    single<CategoriaFinanceiraRepository> { FirestoreCategoriaFinanceiraRepository(get()) }
    single<CentroDeCustoRepository> { FirestoreCentroDeCustoRepository(get()) }
    single<LancamentoFinanceiroRepository> { FirestoreLancamentoFinanceiroRepository(get()) }
    single<MetaRepository> { FirestoreMetaRepository(get()) }
    single<RateioLancamentoRepository> { SqlDelightRateioLancamentoRepository(get()) }
    single<MovimentoContaRepository> { SqlDelightMovimentoContaRepository(get()) }
    single<FuncionarioRepository> { FirestoreFuncionarioRepository(get()) }
    single<EquipeRepository> { FirestoreEquipeRepository(get()) }
    single<RegistroTrabalhoRepository> { FirestoreRegistroTrabalhoRepository(get()) }
    single<RetencaoLancamentoRepository> { SqlDelightRetencaoLancamentoRepository(get()) }
    single<PagamentoRepository> { FirestorePagamentoRepository(get()) }
    single<FornecedorRepository> { FirestoreFornecedorRepository(get()) }
    single<PedidoCompraRepository> { FirestorePedidoCompraRepository(get()) }
    single<ConfigBDIRepository> { FirestoreConfigBDIRepository(get()) }
    single<OrcamentoRepository> { FirestoreOrcamentoRepository(get()) }
    single<VendaRepository> { FirestoreVendaRepository(get()) }
    single<TarefaRepository> { SqlDelightTarefaRepository(get()) }
    single<DiarioObraRepository> { SqlDelightDiarioObraRepository(get()) }
    single<DocumentoTecnicoRepository> { SqlDelightDocumentoTecnicoRepository(get()) }
}
