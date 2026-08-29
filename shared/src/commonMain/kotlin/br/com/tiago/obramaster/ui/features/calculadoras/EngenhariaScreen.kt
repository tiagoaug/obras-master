package br.com.tiago.obramaster.ui.features.calculadoras

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.calc.EngenhariaEngine
import br.com.tiago.obramaster.ui.components.BaseNormativaIcon
import br.com.tiago.obramaster.ui.components.rememberNormasCatalogo

private enum class Subcalculadora(val rotulo: String) {
    CONCRETO("Concreto"), ARGAMASSA("Argamassa"), ALVENARIA("Tijolos/blocos"),
    REVESTIMENTO("Piso/revestimento"), TINTA("Tinta"), TELHADO("Telhado"),
    ESCADA("Escada"), FERRAGEM("Ferragem"),
}

private fun numero(texto: String) = texto.replace(',', '.').toDoubleOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngenhariaScreen(onVoltar: () -> Unit) {
    var sub by remember { mutableStateOf(Subcalculadora.CONCRETO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Engenharia") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Subcalculadora.entries.forEach { opcao ->
                    FilterChip(selected = sub == opcao, onClick = { sub = opcao }, label = { Text(opcao.rotulo) })
                }
            }
            HorizontalDivider()
            when (sub) {
                Subcalculadora.CONCRETO -> FormularioConcreto()
                Subcalculadora.ARGAMASSA -> FormularioArgamassa()
                Subcalculadora.ALVENARIA -> FormularioAlvenaria()
                Subcalculadora.REVESTIMENTO -> FormularioRevestimento()
                Subcalculadora.TINTA -> FormularioTinta()
                Subcalculadora.TELHADO -> FormularioTelhado()
                Subcalculadora.ESCADA -> FormularioEscada()
                Subcalculadora.FERRAGEM -> FormularioFerragem()
            }
        }
    }
}

@Composable
private fun ResultadoTracoColuna(resultado: EngenhariaEngine.ResultadoTraco) {
    Text("Cimento: ${formatarNumero(resultado.cimentoKg)} kg (${formatarNumero(resultado.sacosCimento50kg)} sacos de 50kg)", style = MaterialTheme.typography.titleMedium)
    Text("Areia: ${formatarNumero(resultado.areiaKg)} kg", style = MaterialTheme.typography.titleMedium)
    resultado.britaKg?.let { Text("Brita: ${formatarNumero(it)} kg", style = MaterialTheme.typography.titleMedium) }
    Text("Água: ${formatarNumero(resultado.aguaLitros)} L", style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun FormularioConcreto() {
    var volume by remember { mutableStateOf("") }
    var partesCimento by remember { mutableStateOf("1") }
    var partesAreia by remember { mutableStateOf("2") }
    var partesBrita by remember { mutableStateOf("3") }
    var fatorAgua by remember { mutableStateOf("0,5") }
    var resultado by remember { mutableStateOf<EngenhariaEngine.ResultadoTraco?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Traço de concreto (dosagem por volume absoluto)", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(volume, { volume = it }, label = { Text("Volume de concreto (m³)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(partesCimento, { partesCimento = it }, label = { Text("Cimento") }, modifier = Modifier.weight(1f))
            OutlinedTextField(partesAreia, { partesAreia = it }, label = { Text("Areia") }, modifier = Modifier.weight(1f))
            OutlinedTextField(partesBrita, { partesBrita = it }, label = { Text("Brita") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(fatorAgua, { fatorAgua = it }, label = { Text("Fator água/cimento") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val v = numero(volume); val c = numero(partesCimento); val a = numero(partesAreia); val b = numero(partesBrita); val x = numero(fatorAgua)
                resultado = if (v != null && c != null && a != null && b != null && x != null) EngenhariaEngine.tracoConcreto(v, c, a, b, x) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        resultado?.let { ResultadoTracoColuna(it) } ?: if (tocado) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error) else Unit
    }
}

@Composable
private fun FormularioArgamassa() {
    var area by remember { mutableStateOf("") }
    var espessura by remember { mutableStateOf("2") }
    var partesCimento by remember { mutableStateOf("1") }
    var partesAreia by remember { mutableStateOf("4") }
    var fatorAgua by remember { mutableStateOf("0,8") }
    var resultado by remember { mutableStateOf<EngenhariaEngine.ResultadoTraco?>(null) }
    var volumeCalculado by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Argamassa de assentamento ou reboco (por m²)", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(area, { area = it }, label = { Text("Área (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(espessura, { espessura = it }, label = { Text("Espessura da camada (cm)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(partesCimento, { partesCimento = it }, label = { Text("Cimento") }, modifier = Modifier.weight(1f))
            OutlinedTextField(partesAreia, { partesAreia = it }, label = { Text("Areia") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(fatorAgua, { fatorAgua = it }, label = { Text("Fator água/cimento") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val ar = numero(area); val esp = numero(espessura); val c = numero(partesCimento); val a = numero(partesAreia); val x = numero(fatorAgua)
                val vol = if (ar != null && esp != null) EngenhariaEngine.volumeArgamassaPorArea(ar, esp) else null
                volumeCalculado = vol
                resultado = if (vol != null && c != null && a != null && x != null) EngenhariaEngine.tracoArgamassa(vol, c, a, x) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        volumeCalculado?.let { Text("Volume de argamassa: ${formatarNumero(it)} m³", style = MaterialTheme.typography.bodyMedium) }
        resultado?.let { ResultadoTracoColuna(it) } ?: if (tocado) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error) else Unit
    }
}

@Composable
private fun FormularioAlvenaria() {
    var tipoSelecionado by remember { mutableStateOf<EngenhariaEngine.TipoTijolo?>(EngenhariaEngine.TIPOS_TIJOLO_PADRAO.first()) }
    var comprimentoPersonalizado by remember { mutableStateOf("") }
    var alturaPersonalizada by remember { mutableStateOf("") }
    var junta by remember { mutableStateOf("1") }
    var areaParede by remember { mutableStateOf("") }
    var perda by remember { mutableStateOf("10") }
    var resultado by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tijolos/blocos por m² de parede", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EngenhariaEngine.TIPOS_TIJOLO_PADRAO.forEach { tipo ->
                FilterChip(selected = tipoSelecionado == tipo, onClick = { tipoSelecionado = tipo }, label = { Text(tipo.nome) })
            }
            FilterChip(selected = tipoSelecionado == null, onClick = { tipoSelecionado = null }, label = { Text("Personalizado") })
        }
        if (tipoSelecionado == null) {
            OutlinedTextField(comprimentoPersonalizado, { comprimentoPersonalizado = it }, label = { Text("Comprimento da face (cm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(alturaPersonalizada, { alturaPersonalizada = it }, label = { Text("Altura da face (cm)") }, modifier = Modifier.fillMaxWidth())
        }
        OutlinedTextField(junta, { junta = it }, label = { Text("Espessura da junta (cm)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(areaParede, { areaParede = it }, label = { Text("Área da parede (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(perda, { perda = it }, label = { Text("Perda (%)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val tipo = tipoSelecionado
                val comprimento = tipo?.comprimentoCm ?: numero(comprimentoPersonalizado)
                val altura = tipo?.alturaCm ?: numero(alturaPersonalizada)
                val j = numero(junta); val ap = numero(areaParede); val p = numero(perda)
                resultado = if (comprimento != null && altura != null && j != null && ap != null && p != null) {
                    EngenhariaEngine.tijolosPorM2(comprimento, altura, j)?.let { porM2 -> EngenhariaEngine.quantidadeTijolos(ap, porM2, p) }
                } else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        resultado?.let { Text("Unidades necessárias: ${kotlin.math.ceil(it).toInt()}", style = MaterialTheme.typography.titleMedium) }
            ?: if (tocado) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error) else Unit
    }
}

@Composable
private fun FormularioRevestimento() {
    var area by remember { mutableStateOf("") }
    var perda by remember { mutableStateOf("10") }
    var areaPorCaixa by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Piso/revestimento — caixas necessárias", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(area, { area = it }, label = { Text("Área a revestir (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(perda, { perda = it }, label = { Text("Perda (%)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(areaPorCaixa, { areaPorCaixa = it }, label = { Text("Área por caixa (m²)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val a = numero(area); val p = numero(perda); val c = numero(areaPorCaixa)
                resultado = if (a != null && p != null && c != null) EngenhariaEngine.caixasNecessarias(a, p, c) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        resultado?.let { Text("Caixas necessárias: ${kotlin.math.ceil(it).toInt()}", style = MaterialTheme.typography.titleMedium) }
            ?: if (tocado) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error) else Unit
    }
}

@Composable
private fun FormularioTinta() {
    var area by remember { mutableStateOf("") }
    var demaos by remember { mutableStateOf("2") }
    var rendimento by remember { mutableStateOf("") }
    var volumeLata by remember { mutableStateOf("18") }
    var litros by remember { mutableStateOf<Double?>(null) }
    var latas by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tinta — litros e latas necessários", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(area, { area = it }, label = { Text("Área a pintar (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(demaos, { demaos = it }, label = { Text("Número de demãos") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(rendimento, { rendimento = it }, label = { Text("Rendimento (m² por litro)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(volumeLata, { volumeLata = it }, label = { Text("Volume da lata (L)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val a = numero(area); val d = numero(demaos); val r = numero(rendimento); val vl = numero(volumeLata)
                val l = if (a != null && d != null && r != null) EngenhariaEngine.litrosTinta(a, d, r) else null
                litros = l
                latas = if (l != null && vl != null) EngenhariaEngine.latasNecessarias(l, vl) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        litros?.let { Text("Tinta necessária: ${formatarNumero(it)} L", style = MaterialTheme.typography.titleMedium) }
        latas?.let { Text("Latas necessárias: ${kotlin.math.ceil(it).toInt()}", style = MaterialTheme.typography.titleMedium) }
        if (tocado && litros == null) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun FormularioTelhado() {
    var areaPlana by remember { mutableStateOf("") }
    var inclinacao by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Telhado — área inclinada", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(areaPlana, { areaPlana = it }, label = { Text("Área plana (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(inclinacao, { inclinacao = it }, label = { Text("Inclinação (%)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val ap = numero(areaPlana); val inc = numero(inclinacao)
                resultado = if (ap != null && inc != null) EngenhariaEngine.areaInclinadaTelhado(ap, inc) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        resultado?.let { Text("Área inclinada: ${formatarNumero(it)} m²", style = MaterialTheme.typography.titleMedium) }
            ?: if (tocado) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error) else Unit
    }
}

@Composable
private fun FormularioEscada() {
    var alturaTotal by remember { mutableStateOf("") }
    var piso by remember { mutableStateOf("30") }
    var resultado by remember { mutableStateOf<EngenhariaEngine.ResultadoEscada?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Escada — fórmula de Blondel (63 ≤ 2×espelho + piso ≤ 65)", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(alturaTotal, { alturaTotal = it }, label = { Text("Altura total do lance (cm)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(piso, { piso = it }, label = { Text("Piso desejado (cm)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val a = numero(alturaTotal); val p = numero(piso)
                resultado = if (a != null && p != null) EngenhariaEngine.calcularDegraus(a, p) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        resultado?.let { res ->
            Text("Número de degraus: ${res.numeroDegraus}", style = MaterialTheme.typography.titleMedium)
            Text("Altura do espelho: ${formatarNumero(res.alturaEspelhoCm)} cm", style = MaterialTheme.typography.titleMedium)
            Text("Profundidade do piso: ${formatarNumero(res.profundidadePisoCm)} cm", style = MaterialTheme.typography.titleMedium)
            Text("2×espelho + piso: ${formatarNumero(res.valorBlondelCm)} cm", style = MaterialTheme.typography.titleMedium)
            if (!res.dentroDaFaixaRecomendada) {
                Text("Fora da faixa recomendada de Blondel (63–65) — ajuste o piso desejado.", color = MaterialTheme.colorScheme.error)
            }
        } ?: if (tocado) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error) else Unit
    }
}

@Composable
private fun FormularioFerragem() {
    var volume by remember { mutableStateOf("") }
    var taxa by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    val normas = rememberNormasCatalogo()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ferragem — kg de aço estimado", style = MaterialTheme.typography.labelLarge)
        Text(
            "Taxas de referência (kg/m³): laje ~80, viga ~100–120, pilar ~100–150 — ajuste conforme o projeto estrutural.",
            style = MaterialTheme.typography.bodySmall,
        )
        BaseNormativaIcon(normas.filter { it.numero == "NBR 6118" }, titulo = "Base normativa — Ferragem")
        OutlinedTextField(volume, { volume = it }, label = { Text("Volume de concreto (m³)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(taxa, { taxa = it }, label = { Text("Taxa de aço (kg/m³)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val v = numero(volume); val t = numero(taxa)
                resultado = if (v != null && t != null) EngenhariaEngine.kgAcoEstimado(v, t) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        resultado?.let { Text("Aço estimado: ${formatarNumero(it)} kg", style = MaterialTheme.typography.titleMedium) }
            ?: if (tocado) Text("Confira os valores informados.", color = MaterialTheme.colorScheme.error) else Unit
    }
}

private fun formatarNumero(valor: Double): String {
    val arredondado = kotlin.math.round(valor * 100) / 100
    return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
}
