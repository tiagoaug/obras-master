package br.com.tiago.obramaster.core.modules

enum class AppModule(val id: String, val labelPtBr: String) {
    PLANEJAMENTO("planejamento", "Planejamento"),
    EXECUCAO("execucao", "Execução"),
    COMPRAS("compras", "Compras"),
    VENDAS("vendas", "Vendas"),
    ORCAMENTOS("orcamentos", "Orçamentos (Material + Mão de Obra)"),
    FINANCEIRO("financeiro", "Financeiro"),
    EQUIPES("equipes", "Equipes e Pagamentos"),
    PROJETOS("projetos", "Projetos e Etapas"),
    PESSOAS("pessoas", "Cadastro de Pessoas"),
    CALCULADORAS("calculadoras", "Calculadoras"),
    METAS("metas", "Metas"),
    CADASTROS_BASE("cadastros_base", "Cadastros Básicos (Cores, Materiais...)"),
    RELATORIOS("relatorios", "Relatórios e Exportação");

    companion object {
        fun fromId(id: String): AppModule? = entries.find { it.id == id }
    }
}
