package br.com.tiago.obramaster.domain

/** Resultado bruto do FilePicker — qualquer arquivo, não só imagem (ver ImageRef para fotos). */
data class ArquivoSelecionado(val nomeArquivo: String, val bytes: ByteArray)
