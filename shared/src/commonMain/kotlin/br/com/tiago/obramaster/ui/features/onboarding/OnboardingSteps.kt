package br.com.tiago.obramaster.ui.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.core.onboarding.ColaboradorDraft
import br.com.tiago.obramaster.core.onboarding.ContaDraft
import br.com.tiago.obramaster.core.onboarding.DadosEmpresaDraft
import br.com.tiago.obramaster.core.onboarding.GestorDraft
import br.com.tiago.obramaster.core.onboarding.OnboardingState
import br.com.tiago.obramaster.core.onboarding.OnboardingStep
import br.com.tiago.obramaster.core.onboarding.ProjetoDraft
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.TipoConta
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.theme.FontePreferencia
import br.com.tiago.obramaster.ui.theme.PrefsAcessibilidade
import br.com.tiago.obramaster.ui.theme.TemaPreferencia

@Composable
private fun DicaContextual(texto: String) {
    Card(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(texto, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun BoasVindasStep(onIniciarWizard: () -> Unit, onTentarModoIA: () -> Unit) {
    Column {
        Text("Bem-vindo ao ObraMaster", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Vamos deixar sua empresa, seu acesso de Gestor e o essencial prontos para uso. " +
                "Leva poucos minutos, e tudo que for opcional pode ser preenchido depois.",
            Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Button(onClick = onIniciarWizard, modifier = Modifier.fillMaxWidth()) {
            Text("Preencher formulário")
        }
        OutlinedButton(onClick = onTentarModoIA, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Configurar com ajuda da IA")
        }
    }
}

@Composable
fun EmpresaStep(draft: DadosEmpresaDraft, onChange: (DadosEmpresaDraft) -> Unit) {
    Column {
        Text("Dados da Empresa", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Esses dados aparecem nos PDFs exportados (orçamentos, relatórios) — só o nome é obrigatório agora.")
        Campo("Nome da empresa *", draft.nome) { onChange(draft.copy(nome = it)) }
        Campo("CNPJ (opcional)", draft.cnpj ?: "") { onChange(draft.copy(cnpj = it.ifBlank { null })) }
        Campo("Telefone (opcional)", draft.telefone ?: "") { onChange(draft.copy(telefone = it.ifBlank { null })) }
        Campo("Endereço (opcional)", draft.endereco ?: "") { onChange(draft.copy(endereco = it.ifBlank { null })) }
        Campo("Cidade (opcional)", draft.cidade ?: "") { onChange(draft.copy(cidade = it.ifBlank { null })) }
    }
}

@Composable
fun GestorStep(draft: GestorDraft, onChange: (GestorDraft) -> Unit) {
    var confirmarSenha by remember { mutableStateOf(draft.senha) }
    Column {
        Text("Conta do Gestor", style = MaterialTheme.typography.titleLarge)
        DicaContextual("O Gestor tem acesso total ao sistema — esse cadastro não pode ser excluído depois.")
        Campo("Nome *", draft.nome) { onChange(draft.copy(nome = it)) }
        Campo("E-mail *", draft.email) { onChange(draft.copy(email = it)) }
        CampoSenha("Senha *", draft.senha) { onChange(draft.copy(senha = it)) }
        CampoSenha("Confirmar senha *", confirmarSenha) { confirmarSenha = it }
        if (confirmarSenha.isNotBlank() && confirmarSenha != draft.senha) {
            Text("As senhas não conferem", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ModulosStep(ativos: Set<AppModule>, onAlternar: (AppModule, Boolean) -> Unit) {
    Column {
        Text("Módulos Iniciais", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Já vem marcado um conjunto recomendado para começar — mude à vontade, e dá para ligar/desligar depois em Configurações.")
        AppModule.entries.forEach { modulo ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Checkbox(checked = modulo in ativos, onCheckedChange = { onAlternar(modulo, it) })
                Text(modulo.labelPtBr, Modifier.padding(top = 12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContasStep(contas: List<ContaDraft>, onAdicionar: (ContaDraft) -> Unit, onRemover: (Int) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoConta.CAIXA) }
    var saldoCentavos by remember { mutableStateOf(0L) }

    Column {
        Text("Contas Financeiras", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Cadastre ao menos uma conta (caixa da obra, conta corrente...) — é o mínimo para o Financeiro funcionar.")

        contas.forEachIndexed { indice, conta ->
            ListItem(
                headlineContent = { Text(conta.nome) },
                supportingContent = { Text("${conta.tipo.name} · ${MoneyFormatter.formatar(conta.saldoInicialCentavos)}") },
                trailingContent = {
                    IconButton(onClick = { onRemover(indice) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remover")
                    }
                },
            )
        }

        Campo("Nome da conta", nome) { nome = it }

        Text("Tipo", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        Row {
            TipoConta.entries.forEach { opcao ->
                FilterChip(
                    selected = tipo == opcao,
                    onClick = { tipo = opcao },
                    label = { Text(opcao.name) },
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }

        CalculatorTextField(
            valueCentavos = saldoCentavos,
            onValueChange = { saldoCentavos = it },
            label = "Saldo inicial",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        Button(
            onClick = {
                if (nome.isNotBlank()) {
                    onAdicionar(ContaDraft(nome, tipo, saldoCentavos))
                    nome = ""
                    saldoCentavos = 0L
                }
            },
            enabled = nome.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text("Adicionar conta") }
    }
}

@Composable
fun CategoriasStep(usarPadrao: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        Text("Categorias Financeiras", style = MaterialTheme.typography.titleLarge)
        DicaContextual("As categorias entram de verdade quando o módulo Financeiro completo chegar — aqui você só decide se quer começar com o padrão do sistema.")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = usarPadrao, onCheckedChange = onChange)
            Text("Usar categorias padrão do sistema (recomendado)", Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
fun BdiStep(usarPadrao: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        Text("Perfil de BDI", style = MaterialTheme.typography.titleLarge)
        DicaContextual("BDI (Bonificação e Despesas Indiretas) é usado nos Orçamentos — chega na Fase 6. Por enquanto só guardamos sua preferência.")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = usarPadrao, onCheckedChange = onChange)
            Text("Usar perfil de BDI sugerido de mercado", Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
fun TemplateEtapasStep(usarPadrao: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        Text("Template de Etapas de Obra", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Template padrão: Fundação → Estrutura → Alvenaria → Instalações → Acabamento → Entrega. Aplicado nos projetos a partir da Fase 3.")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = usarPadrao, onCheckedChange = onChange)
            Text("Usar template padrão", Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
fun ColaboradoresStep(
    colaboradores: List<ColaboradorDraft>,
    onAdicionar: (ColaboradorDraft) -> Unit,
    onRemover: (Int) -> Unit,
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    Column {
        Text("Colaboradores", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Opcional — pode pular e cadastrar depois em Configurações, com permissão módulo a módulo (aqui entram sem nenhuma permissão, ajustável depois). Essas credenciais já ficam prontas pra pessoa entrar — não é preciso e-mail real, só um identificador único.")

        colaboradores.forEachIndexed { indice, colaborador ->
            ListItem(
                headlineContent = { Text(colaborador.nome) },
                supportingContent = { Text(colaborador.email) },
                trailingContent = {
                    IconButton(onClick = { onRemover(indice) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remover")
                    }
                },
            )
        }

        Campo("Nome", nome) { nome = it }
        Campo("E-mail", email) { email = it }
        CampoSenha("Senha", senha) { senha = it }
        Button(
            onClick = {
                if (nome.isNotBlank() && email.isNotBlank() && senha.length >= 6) {
                    onAdicionar(ColaboradorDraft(nome, email, senha))
                    nome = ""; email = ""; senha = ""
                }
            },
            enabled = nome.isNotBlank() && email.isNotBlank() && senha.length >= 6,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text("Adicionar colaborador") }
    }
}

@Composable
fun PrimeiroProjetoStep(draft: ProjetoDraft?, onChange: (ProjetoDraft?) -> Unit) {
    val atual = draft ?: ProjetoDraft(nome = "")
    Column {
        Text("Primeiro Projeto", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Opcional — deixa a primeira obra pronta. A gestão completa de projetos chega na Fase 3; por enquanto isso fica guardado.")
        Campo("Nome do projeto", atual.nome) { onChange(atual.copy(nome = it)) }
        Campo("Endereço", atual.endereco) { onChange(atual.copy(endereco = it)) }
        CalculatorTextField(
            valueCentavos = atual.orcamentoTotalCentavos,
            onValueChange = { onChange(atual.copy(orcamentoTotalCentavos = it)) },
            label = "Orçamento total",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

@Composable
fun AcessibilidadeOnboardingStep(prefs: PrefsAcessibilidade, onChange: (PrefsAcessibilidade) -> Unit) {
    Column {
        Text("Acessibilidade", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Pode ajustar a qualquer momento depois, em Configurações.")

        Text("Tema", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        Row {
            TemaPreferencia.entries.forEach { tema ->
                FilterChip(
                    selected = prefs.tema == tema,
                    onClick = { onChange(prefs.copy(tema = tema)) },
                    label = { Text(tema.name) },
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }

        Text("Fonte", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
        Row {
            FontePreferencia.entries.forEach { fonte ->
                FilterChip(
                    selected = prefs.fonte == fonte,
                    onClick = { onChange(prefs.copy(fonte = fonte)) },
                    label = { Text(fonte.name) },
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }

        Text(
            "Tamanho da fonte: ${(prefs.escalaFonte * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Slider(
            value = prefs.escalaFonte,
            onValueChange = { onChange(prefs.copy(escalaFonte = it)) },
            valueRange = 0.85f..1.4f,
        )
    }
}

@Composable
fun ResumoStep(
    estado: OnboardingState,
    concluindo: Boolean,
    onEditar: (OnboardingStep) -> Unit,
    onConcluir: () -> Unit,
) {
    Column {
        Text("Resumo e Conclusão", style = MaterialTheme.typography.titleLarge)
        DicaContextual("Nada foi gravado ainda — revise e confirme para criar tudo de uma vez.")

        BlocoResumo("Empresa", estado.empresa.nome.ifBlank { "—" }) { onEditar(OnboardingStep.EMPRESA) }
        BlocoResumo("Gestor", "${estado.gestor.nome} (${estado.gestor.email})") { onEditar(OnboardingStep.GESTOR) }
        BlocoResumo("Módulos", "${estado.modulosAtivos.size} ativos") { onEditar(OnboardingStep.MODULOS) }
        BlocoResumo("Contas", "${estado.contas.size} cadastrada(s)") { onEditar(OnboardingStep.CONTAS_FINANCEIRAS) }
        BlocoResumo("Categorias", if (estado.usarCategoriasDefault) "Padrão do sistema" else "—") { onEditar(OnboardingStep.CATEGORIAS) }
        BlocoResumo("BDI", if (estado.usarBdiPadrao) "Sugerido de mercado" else "—") { onEditar(OnboardingStep.BDI) }
        BlocoResumo("Template de etapas", if (estado.usarTemplateEtapasPadrao) "Padrão" else "—") { onEditar(OnboardingStep.TEMPLATE_ETAPAS) }
        BlocoResumo("Colaboradores", "${estado.colaboradores.size} adicional(is)") { onEditar(OnboardingStep.COLABORADORES) }
        BlocoResumo("Primeiro projeto", estado.primeiroProjeto?.nome ?: "Pulado") { onEditar(OnboardingStep.PRIMEIRO_PROJETO) }
        BlocoResumo("Acessibilidade", estado.acessibilidade.tema.name) { onEditar(OnboardingStep.ACESSIBILIDADE) }

        Button(
            onClick = onConcluir,
            enabled = !concluindo,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text("Concluir e Começar a Usar") }
    }
}

@Composable
private fun BlocoResumo(titulo: String, valor: String, onEditar: () -> Unit) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = { Text(valor) },
        trailingContent = { OutlinedButton(onClick = onEditar) { Text("Editar") } },
    )
}

@Composable
private fun Campo(label: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
}

@Composable
private fun CampoSenha(label: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(label) },
        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
}
