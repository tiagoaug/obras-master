package br.com.tiago.obramaster.domain

/** SPEC_OBRA_MASTER.md §4.7 — checklist de tarefas por etapa. */
data class Tarefa(
    val id: String,
    val etapaId: String,
    val descricao: String,
    val responsavelPessoaId: String? = null,
    val prazo: Long? = null, // epoch millis UTC
    val concluida: Boolean = false,
)
