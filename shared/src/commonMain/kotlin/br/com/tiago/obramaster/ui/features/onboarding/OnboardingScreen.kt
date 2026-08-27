package br.com.tiago.obramaster.ui.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.onboarding.OnboardingStep
import br.com.tiago.obramaster.core.onboarding.ValidationResult
import br.com.tiago.obramaster.domain.Colaborador
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(
    onConcluido: (Colaborador) -> Unit,
    viewModel: OnboardingViewModel = koinInject(),
) {
    val estado by viewModel.state.collectAsState()
    val concluindo by viewModel.concluindo.collectAsState()
    val evento by viewModel.evento.collectAsState()
    var mostrarAvisoIA by remember { mutableStateOf(false) }
    var erroValidacao by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(evento) {
        when (val e = evento) {
            is OnboardingEvento.Concluido -> onConcluido(e.colaboradorGestor)
            is OnboardingEvento.Erro -> erroValidacao = e.mensagem
            null -> Unit
        }
        if (evento != null) viewModel.eventoConsumido()
    }

    val etapa = estado.etapaAtual
    val indiceEtapa = OnboardingStep.entries.indexOf(etapa)

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (etapa != OnboardingStep.BOAS_VINDAS) {
                LinearProgressIndicator(
                    progress = { (indiceEtapa + 1) / OnboardingStep.entries.size.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Etapa ${indiceEtapa + 1} de ${OnboardingStep.entries.size}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            Box(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                Column(Modifier.widthIn(max = 480.dp)) {
                    when (etapa) {
                        OnboardingStep.BOAS_VINDAS -> BoasVindasStep(
                            onIniciarWizard = { viewModel.avancar() },
                            onTentarModoIA = { mostrarAvisoIA = true },
                        )
                        OnboardingStep.EMPRESA -> EmpresaStep(estado.empresa, viewModel::atualizarEmpresa)
                        OnboardingStep.GESTOR -> GestorStep(estado.gestor, viewModel::atualizarGestor)
                        OnboardingStep.MODULOS -> ModulosStep(estado.modulosAtivos, viewModel::alternarModulo)
                        OnboardingStep.CONTAS_FINANCEIRAS -> ContasStep(
                            estado.contas,
                            viewModel::adicionarConta,
                            viewModel::removerConta,
                        )
                        OnboardingStep.CATEGORIAS -> CategoriasStep(
                            estado.usarCategoriasDefault,
                            viewModel::atualizarUsarCategoriasDefault,
                        )
                        OnboardingStep.BDI -> BdiStep(estado.usarBdiPadrao, viewModel::atualizarUsarBdiPadrao)
                        OnboardingStep.TEMPLATE_ETAPAS -> TemplateEtapasStep(
                            estado.usarTemplateEtapasPadrao,
                            viewModel::atualizarUsarTemplateEtapasPadrao,
                        )
                        OnboardingStep.COLABORADORES -> ColaboradoresStep(
                            estado.colaboradores,
                            viewModel::adicionarColaborador,
                            viewModel::removerColaborador,
                        )
                        OnboardingStep.PRIMEIRO_PROJETO -> PrimeiroProjetoStep(
                            estado.primeiroProjeto,
                            viewModel::atualizarPrimeiroProjeto,
                        )
                        OnboardingStep.ACESSIBILIDADE -> AcessibilidadeOnboardingStep(
                            estado.acessibilidade,
                            viewModel::atualizarAcessibilidade,
                        )
                        OnboardingStep.RESUMO -> ResumoStep(
                            estado = estado,
                            concluindo = concluindo,
                            onEditar = viewModel::irParaEtapa,
                            onConcluir = viewModel::concluir,
                        )
                    }
                }
            }

            if (etapa != OnboardingStep.BOAS_VINDAS && etapa != OnboardingStep.RESUMO) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (etapa.anterior() != null) {
                        OutlinedButton(onClick = { viewModel.voltar() }, modifier = Modifier.weight(1f)) {
                            Text("Voltar")
                        }
                    }
                    val validacao = viewModel.validacaoAtual()
                    Button(
                        onClick = {
                            if (validacao is ValidationResult.Valido) {
                                viewModel.avancar()
                            } else if (!etapa.obrigatoria) {
                                viewModel.pular()
                            } else {
                                erroValidacao = (validacao as ValidationResult.Invalido).motivo
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (validacao is ValidationResult.Invalido && !etapa.obrigatoria) "Pular esta etapa" else "Continuar")
                    }
                }
            }
        }
    }

    if (mostrarAvisoIA) {
        AlertDialog(
            onDismissRequest = { mostrarAvisoIA = false },
            title = { Text("Assistente de IA indisponível") },
            text = { Text("O modo guiado por IA depende do backend, que chega na Fase 10. Por enquanto, use o formulário — dá no mesmo resultado.") },
            confirmButton = {
                Button(onClick = {
                    mostrarAvisoIA = false
                    viewModel.avancar()
                }) { Text("Usar formulário") }
            },
        )
    }

    erroValidacao?.let { mensagem ->
        AlertDialog(
            onDismissRequest = { erroValidacao = null },
            title = { Text("Antes de continuar") },
            text = { Text(mensagem) },
            confirmButton = { Button(onClick = { erroValidacao = null }) { Text("Entendi") } },
        )
    }

    if (concluindo) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
