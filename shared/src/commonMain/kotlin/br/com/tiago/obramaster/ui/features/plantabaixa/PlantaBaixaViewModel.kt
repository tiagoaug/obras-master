package br.com.tiago.obramaster.ui.features.plantabaixa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.plantabaixa.DxfImporter
import br.com.tiago.obramaster.core.plantabaixa.PlantaBaixaEngine
import br.com.tiago.obramaster.core.plantabaixa.UnidadeDxf
import br.com.tiago.obramaster.data.repository.AberturaRepository
import br.com.tiago.obramaster.data.repository.ArquivoImportadoRepository
import br.com.tiago.obramaster.data.repository.ComodoRepository
import br.com.tiago.obramaster.data.repository.ParedeRepository
import br.com.tiago.obramaster.data.repository.PlantaBaixaRepository
import br.com.tiago.obramaster.domain.Abertura
import br.com.tiago.obramaster.domain.ArquivoImportado
import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.FormatoImportacao
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.domain.PlantaBaixa
import br.com.tiago.obramaster.domain.PontoXY
import br.com.tiago.obramaster.domain.TipoAbertura
import br.com.tiago.obramaster.platform.FilePicker
import br.com.tiago.obramaster.platform.ImagePicker
import br.com.tiago.obramaster.platform.ImageStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EditorPlantaUiState(
    val planta: PlantaBaixa? = null,
    val comodos: List<Comodo> = emptyList(),
    val paredes: List<Parede> = emptyList(),
    val aberturas: List<Abertura> = emptyList(),
    val ferramentaAtual: FerramentaDesenho = FerramentaDesenho.SELECIONAR,
    val pontosPoligonoEmDesenho: List<PontoXY> = emptyList(),
    val comodoSelecionadoId: String? = null,
    val medidaPontoA: PontoXY? = null,
    val medidaResultadoM: Double? = null,
    val ultimaFormaCriadaId: String? = null,
    val corPreenchimentoPadrao: String = "#90CAF9",
    val imagemFundoBytes: ByteArray? = null,
    val mostrarImagemFundo: Boolean = true,
    val pontoCalibracaoA: PontoXY? = null,
    val linhaCalibracaoPendente: Pair<PontoXY, PontoXY>? = null,
    val importandoImagem: Boolean = false,
    val importandoArquivo: Boolean = false,
    val erroImportacaoArquivo: String? = null,
    val nomeArquivoImportado: String? = null,
    val resultadoImportacaoDxf: DxfImporter.ResultadoImportacaoDxf? = null,
    val camadasSelecionadas: Set<String>? = null,
    val arquivoOrigemMaisRecente: ArquivoImportado? = null,
)

class PlantaBaixaViewModel(
    private val plantaId: String,
    private val plantaBaixaRepository: PlantaBaixaRepository,
    private val comodoRepository: ComodoRepository,
    private val paredeRepository: ParedeRepository,
    private val aberturaRepository: AberturaRepository,
    private val imagePicker: ImagePicker,
    private val imageStore: ImageStore,
    private val filePicker: FilePicker,
    private val arquivoImportadoRepository: ArquivoImportadoRepository,
) : ViewModel() {

    private var conteudoArquivoImportadoAtual: String? = null

    private val _uiState = MutableStateFlow(EditorPlantaUiState())
    val uiState: StateFlow<EditorPlantaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val planta = plantaBaixaRepository.buscarPorId(plantaId)
            _uiState.value = _uiState.value.copy(planta = planta)
            planta?.imagemFundoKey?.let { chave ->
                _uiState.value = _uiState.value.copy(imagemFundoBytes = imageStore.load(chave))
            }
            _uiState.value = _uiState.value.copy(
                arquivoOrigemMaisRecente = arquivoImportadoRepository.listarDaPlanta(plantaId).firstOrNull(),
            )
        }
        viewModelScope.launch {
            comodoRepository.observarDaPlanta(plantaId).collect { comodos ->
                _uiState.value = _uiState.value.copy(comodos = comodos)
            }
        }
        viewModelScope.launch {
            paredeRepository.observarDaPlanta(plantaId).collect { paredes ->
                _uiState.value = _uiState.value.copy(paredes = paredes)
            }
        }
        viewModelScope.launch {
            aberturaRepository.observarTodas().collect { todas ->
                val idsDasParedes = _uiState.value.paredes.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(aberturas = todas.filter { it.paredeId in idsDasParedes })
            }
        }
    }

    private val escala: Double get() = _uiState.value.planta?.escalaPxPorMetro ?: 100.0

    fun selecionarFerramenta(ferramenta: FerramentaDesenho) {
        _uiState.value = _uiState.value.copy(
            ferramentaAtual = ferramenta,
            pontosPoligonoEmDesenho = emptyList(),
            medidaPontoA = null,
            medidaResultadoM = null,
            pontoCalibracaoA = null,
        )
    }

    fun definirEscalaManual(metrosPorQuadroDaGrade: Double, tamanhoGradePx: Double) {
        val planta = _uiState.value.planta ?: return
        if (metrosPorQuadroDaGrade <= 0.0) return
        val novaEscala = tamanhoGradePx / metrosPorQuadroDaGrade
        viewModelScope.launch {
            plantaBaixaRepository.atualizarEscala(plantaId, novaEscala, agora())
            _uiState.value = _uiState.value.copy(planta = planta.copy(escalaPxPorMetro = novaEscala))
        }
    }

    /** Ferramenta RETANGULO: chamado ao soltar o arraste. */
    @OptIn(ExperimentalUuidApi::class)
    fun criarRetangulo(pontoA: PontoXY, pontoB: PontoXY) {
        val cantos = listOf(
            PontoXY(minOf(pontoA.x, pontoB.x), minOf(pontoA.y, pontoB.y)),
            PontoXY(maxOf(pontoA.x, pontoB.x), minOf(pontoA.y, pontoB.y)),
            PontoXY(maxOf(pontoA.x, pontoB.x), maxOf(pontoA.y, pontoB.y)),
            PontoXY(minOf(pontoA.x, pontoB.x), maxOf(pontoA.y, pontoB.y)),
        )
        criarComodoComParedes(cantos)
    }

    /** Ferramenta POLIGONO: um toque por vez; fecha sozinho perto do ponto inicial. */
    fun tocarParaPoligono(pontoBruto: PontoXY, tamanhoGradePx: Double) {
        val pontosAtuais = _uiState.value.pontosPoligonoEmDesenho
        var ponto = PlantaBaixaEngine.snapGrade(pontoBruto, tamanhoGradePx)
        if (pontosAtuais.isNotEmpty()) {
            ponto = PlantaBaixaEngine.snapAngulo(ponto, pontosAtuais.last())
        }

        if (pontosAtuais.size >= 2 && PlantaBaixaEngine.poligonoFechado(pontosAtuais + ponto)) {
            criarComodoComParedes(pontosAtuais)
            _uiState.value = _uiState.value.copy(pontosPoligonoEmDesenho = emptyList())
        } else {
            _uiState.value = _uiState.value.copy(pontosPoligonoEmDesenho = pontosAtuais + ponto)
        }
    }

    fun removerUltimoPontoDoPoligono() {
        val atuais = _uiState.value.pontosPoligonoEmDesenho
        if (atuais.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(pontosPoligonoEmDesenho = atuais.dropLast(1))
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun criarComodoComParedes(pontos: List<PontoXY>) {
        if (pontos.size < 3) return
        val comodoId = Uuid.random().toString()
        val comodo = Comodo(
            id = comodoId,
            plantaId = plantaId,
            nome = "Cômodo ${_uiState.value.comodos.size + 1}",
            pontos = pontos,
            corPreenchimento = _uiState.value.corPreenchimentoPadrao,
            areaM2 = PlantaBaixaEngine.calcularAreaM2(pontos, escala),
            perimetroM = PlantaBaixaEngine.calcularPerimetroM(pontos, escala),
        )
        viewModelScope.launch {
            comodoRepository.salvar(comodo)
            for (indice in pontos.indices) {
                paredeRepository.salvar(
                    Parede(
                        id = Uuid.random().toString(),
                        plantaId = plantaId,
                        pontoInicio = pontos[indice],
                        pontoFim = pontos[(indice + 1) % pontos.size],
                    ),
                )
            }
            _uiState.value = _uiState.value.copy(ultimaFormaCriadaId = comodoId)
        }
    }

    /** Desfaz só a última forma criada nesta sessão do editor (não é histórico completo). */
    fun desfazerUltimaForma() {
        val ultimoId = _uiState.value.ultimaFormaCriadaId ?: return
        // Sem vínculo direto parede<->cômodo no modelo (spec não define esse relacionamento),
        // então só o cômodo é desfeito; as paredes do retângulo/polígono ficam (podem ser
        // apagadas manualmente). Simplificação aceitável para o "desfazer" de um nível só.
        viewModelScope.launch {
            comodoRepository.desativar(ultimoId)
            _uiState.value = _uiState.value.copy(ultimaFormaCriadaId = null)
        }
    }

    fun selecionarComodo(id: String?) {
        _uiState.value = _uiState.value.copy(comodoSelecionadoId = id)
    }

    fun renomearComodoSelecionado(nome: String) {
        val id = _uiState.value.comodoSelecionadoId ?: return
        viewModelScope.launch { comodoRepository.renomear(id, nome) }
    }

    fun excluirComodo(id: String) {
        viewModelScope.launch {
            comodoRepository.desativar(id)
            if (_uiState.value.comodoSelecionadoId == id) {
                _uiState.value = _uiState.value.copy(comodoSelecionadoId = null)
            }
        }
    }

    /** Encontra o cômodo cujo polígono contém [ponto] (ray casting) — usado pela ferramenta SELECIONAR. */
    fun comodoNoPonto(ponto: PontoXY): Comodo? =
        _uiState.value.comodos.firstOrNull { pontoDentroDoPoligono(ponto, it.pontos) }

    private fun pontoDentroDoPoligono(ponto: PontoXY, pontos: List<PontoXY>): Boolean {
        if (pontos.size < 3) return false
        var dentro = false
        var j = pontos.size - 1
        for (i in pontos.indices) {
            val pi = pontos[i]
            val pj = pontos[j]
            if ((pi.y > ponto.y) != (pj.y > ponto.y)) {
                val xIntersecao = (pj.x - pi.x) * (ponto.y - pi.y) / (pj.y - pi.y) + pi.x
                if (ponto.x < xIntersecao) dentro = !dentro
            }
            j = i
        }
        return dentro
    }

    /** Ferramenta MEDIR: dois toques, mostra distância real. */
    fun tocarParaMedir(ponto: PontoXY) {
        val estadoAtual = _uiState.value
        if (estadoAtual.medidaPontoA == null) {
            _uiState.value = estadoAtual.copy(medidaPontoA = ponto, medidaResultadoM = null)
        } else {
            val distanciaPx = hypot(ponto.x - estadoAtual.medidaPontoA.x, ponto.y - estadoAtual.medidaPontoA.y)
            _uiState.value = estadoAtual.copy(medidaResultadoM = distanciaPx / escala, medidaPontoA = null)
        }
    }

    /** Ferramentas PORTA/JANELA: toque perto de uma parede insere a abertura nela. */
    @OptIn(ExperimentalUuidApi::class)
    fun tocarParaAbertura(ponto: PontoXY, tipo: TipoAbertura, toleranciaPx: Double = 20.0) {
        val paredes = _uiState.value.paredes
        val maisProxima = paredes.minByOrNull { distanciaAteSegmento(ponto, it.pontoInicio, it.pontoFim).first } ?: return
        val (distancia, posicao) = distanciaAteSegmento(ponto, maisProxima.pontoInicio, maisProxima.pontoFim)
        if (distancia > toleranciaPx) return

        viewModelScope.launch {
            aberturaRepository.salvar(
                Abertura(
                    id = Uuid.random().toString(),
                    paredeId = maisProxima.id,
                    tipo = tipo,
                    posicaoNaParede = posicao,
                    larguraCm = if (tipo == TipoAbertura.PORTA) 80.0 else 120.0,
                ),
            )
        }
    }

    private fun distanciaAteSegmento(p: PontoXY, a: PontoXY, b: PontoXY): Pair<Double, Double> {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val comprimentoQuadrado = dx * dx + dy * dy
        if (comprimentoQuadrado == 0.0) return hypot(p.x - a.x, p.y - a.y) to 0.0
        var t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / comprimentoQuadrado
        t = t.coerceIn(0.0, 1.0)
        val projX = a.x + t * dx
        val projY = a.y + t * dy
        return hypot(p.x - projX, p.y - projY) to t
    }

    private fun agora(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

    // --- Caminho B (SPEC_PLANTA_BAIXA.md §5) — importar foto e calibrar por ela ---

    suspend fun imagemDisponivel(): Boolean = imagePicker.isAvailable()

    fun importarImagemDaGaleria() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(importandoImagem = true)
            val imagem = imagePicker.pickFromGallery().firstOrNull()
            if (imagem != null) {
                val chave = imageStore.save(imagem)
                plantaBaixaRepository.atualizarImagemFundo(plantaId, chave, agora())
                _uiState.value = _uiState.value.copy(
                    planta = _uiState.value.planta?.copy(imagemFundoKey = chave),
                    imagemFundoBytes = imagem.bytes,
                    importandoImagem = false,
                    mostrarImagemFundo = true,
                )
            } else {
                _uiState.value = _uiState.value.copy(importandoImagem = false)
            }
        }
    }

    fun alternarVisibilidadeImagemFundo() {
        _uiState.value = _uiState.value.copy(mostrarImagemFundo = !_uiState.value.mostrarImagemFundo)
    }

    fun definirOpacidadeImagemFundo(opacidade: Float) {
        val planta = _uiState.value.planta ?: return
        viewModelScope.launch { plantaBaixaRepository.atualizarOpacidadeFundo(plantaId, opacidade) }
        _uiState.value = _uiState.value.copy(planta = planta.copy(imagemFundoOpacidade = opacidade))
    }

    /** Ferramenta CALIBRAR: dois toques marcam uma medida conhecida na foto; ver confirmarCalibracao(). */
    fun tocarParaCalibrar(ponto: PontoXY) {
        val estadoAtual = _uiState.value
        if (estadoAtual.pontoCalibracaoA == null) {
            _uiState.value = estadoAtual.copy(pontoCalibracaoA = ponto)
        } else {
            _uiState.value = estadoAtual.copy(
                linhaCalibracaoPendente = estadoAtual.pontoCalibracaoA to ponto,
                pontoCalibracaoA = null,
            )
        }
    }

    fun cancelarCalibracao() {
        _uiState.value = _uiState.value.copy(pontoCalibracaoA = null, linhaCalibracaoPendente = null)
    }

    /** Usuário informou a distância real da linha traçada — recalcula e grava a escala da planta. */
    fun confirmarCalibracao(distanciaRealM: Double) {
        val linha = _uiState.value.linhaCalibracaoPendente ?: return
        if (distanciaRealM <= 0.0) return
        val novaEscala = PlantaBaixaEngine.calcularEscala(linha.first, linha.second, distanciaRealM)
        val planta = _uiState.value.planta ?: return
        viewModelScope.launch {
            plantaBaixaRepository.atualizarEscala(plantaId, novaEscala, agora())
            // Recalcula área/perímetro dos cômodos já existentes (pontos continuam os mesmos px,
            // só a escala mudou) — sem isso, recalibrar depois de já ter desenhado deixava as
            // áreas erradas (bug pré-existente, exposto agora pela importação de DXF sem escala).
            val comodosAtualizados = _uiState.value.comodos.map { comodo ->
                comodo.copy(
                    areaM2 = PlantaBaixaEngine.calcularAreaM2(comodo.pontos, novaEscala),
                    perimetroM = PlantaBaixaEngine.calcularPerimetroM(comodo.pontos, novaEscala),
                )
            }
            comodosAtualizados.forEach { comodoRepository.atualizarAreaPerimetro(it.id, it.areaM2, it.perimetroM) }
            _uiState.value = _uiState.value.copy(
                planta = planta.copy(escalaPxPorMetro = novaEscala),
                comodos = comodosAtualizados,
                linhaCalibracaoPendente = null,
            )
        }
    }

    // --- SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §2, §5 — importar DXF ---

    suspend fun arquivoDisponivel(): Boolean = filePicker.isAvailable()

    fun importarArquivo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(importandoArquivo = true, erroImportacaoArquivo = null)
            val arquivo = filePicker.escolherArquivo(extensoesAceitas = listOf("dxf"))
            if (arquivo == null) {
                _uiState.value = _uiState.value.copy(importandoArquivo = false)
                return@launch
            }
            if (!arquivo.nomeArquivo.lowercase().endsWith(".dxf")) {
                _uiState.value = _uiState.value.copy(
                    importandoArquivo = false,
                    erroImportacaoArquivo = "Formato ainda não suportado nesta fase — só .dxf por enquanto.",
                )
                return@launch
            }
            val conteudo = arquivo.bytes.decodeToString()
            conteudoArquivoImportadoAtual = conteudo
            val resultado = DxfImporter.importar(conteudo)
            _uiState.value = _uiState.value.copy(
                importandoArquivo = false,
                nomeArquivoImportado = arquivo.nomeArquivo,
                resultadoImportacaoDxf = resultado,
                camadasSelecionadas = null,
            )
        }
    }

    /** Reprocessa o DXF já lido, excluindo/incluindo uma camada — sem reabrir o seletor de arquivo. */
    fun alternarCamadaSelecionada(camada: String) {
        val conteudo = conteudoArquivoImportadoAtual ?: return
        val resultado = _uiState.value.resultadoImportacaoDxf ?: return
        val atuais = _uiState.value.camadasSelecionadas ?: resultado.camadasEncontradas.toSet()
        val novasSelecionadas = if (camada in atuais) atuais - camada else atuais + camada
        _uiState.value = _uiState.value.copy(
            camadasSelecionadas = novasSelecionadas,
            resultadoImportacaoDxf = DxfImporter.importar(conteudo, novasSelecionadas),
        )
    }

    fun cancelarImportacaoArquivo() {
        conteudoArquivoImportadoAtual = null
        _uiState.value = _uiState.value.copy(
            resultadoImportacaoDxf = null,
            nomeArquivoImportado = null,
            erroImportacaoArquivo = null,
            camadasSelecionadas = null,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    fun confirmarImportacaoArquivo() {
        val resultado = _uiState.value.resultadoImportacaoDxf ?: return
        val nomeArquivo = _uiState.value.nomeArquivoImportado ?: return
        val planta = _uiState.value.planta ?: return

        viewModelScope.launch {
            if (resultado.escalaAutomaticaPxPorMetro != null) {
                plantaBaixaRepository.atualizarEscala(plantaId, resultado.escalaAutomaticaPxPorMetro, agora())
                _uiState.value = _uiState.value.copy(planta = planta.copy(escalaPxPorMetro = resultado.escalaAutomaticaPxPorMetro))
            }
            resultado.paredes.forEach { paredeRepository.salvar(it.copy(plantaId = plantaId)) }
            resultado.comodos.forEach { comodoRepository.salvar(it.copy(plantaId = plantaId)) }

            val registroOrigem = ArquivoImportado(
                id = Uuid.random().toString(),
                plantaId = plantaId,
                formatoOrigem = FormatoImportacao.DXF,
                nomeArquivoOriginal = nomeArquivo,
                escalaDetectadaAutomaticamente = resultado.escalaAutomaticaPxPorMetro != null,
                unidadeOrigem = if (resultado.unidadeDetectada != UnidadeDxf.DESCONHECIDA) resultado.unidadeDetectada.name else null,
                camadasImportadas = _uiState.value.camadasSelecionadas?.toList() ?: resultado.camadasEncontradas,
                importadoEm = agora(),
            )
            arquivoImportadoRepository.salvar(registroOrigem)

            conteudoArquivoImportadoAtual = null
            _uiState.value = _uiState.value.copy(
                resultadoImportacaoDxf = null,
                nomeArquivoImportado = null,
                camadasSelecionadas = null,
                arquivoOrigemMaisRecente = registroOrigem,
                ferramentaAtual = if (resultado.escalaAutomaticaPxPorMetro == null) FerramentaDesenho.CALIBRAR else _uiState.value.ferramentaAtual,
            )
        }
    }
}
