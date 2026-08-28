package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ArquivoSelecionado

/**
 * SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §5 — seletor de arquivo genérico (DXF/SVG/PDF),
 * diferente do ImagePicker (que só lida com fotos/galeria).
 */
expect class FilePicker {
    suspend fun isAvailable(): Boolean
    suspend fun escolherArquivo(extensoesAceitas: List<String> = emptyList()): ArquivoSelecionado?
}
