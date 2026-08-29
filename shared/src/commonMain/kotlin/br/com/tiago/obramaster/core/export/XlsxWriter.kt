package br.com.tiago.obramaster.core.export

import br.com.tiago.obramaster.domain.ExportableDocument

/** SPEC_OBRA_MASTER_KMP.md §5.2 — gerador de XLSX escrito à mão em commonMain (decisão da Fase
 * 9.4, mesmo raciocínio do PdfWriter da 9.3): XLSX é OOXML — um ZIP (ver ZipWriter.kt) com
 * algumas partes XML fixas + a planilha em si. Todas as células saem como texto simples
 * (`inlineStr`) — os valores já chegam formatados pela tela chamadora (ex.: "R$ 1.234,56"),
 * então tratar como número exigiria reinterpretar um texto já formatado em pt-BR, arriscando
 * errar (separador decimal, símbolo de moeda). Sem sharedStrings (evita indexação), sem estilos
 * além do mínimo exigido pelo formato. */
object XlsxWriter {

    fun escrever(doc: ExportableDocument): ByteArray {
        val arquivos = listOf(
            "[Content_Types].xml" to contentTypesXml(),
            "_rels/.rels" to relsRaizXml(),
            "xl/workbook.xml" to workbookXml(),
            "xl/_rels/workbook.xml.rels" to workbookRelsXml(),
            "xl/styles.xml" to stylesXml(),
            "xl/worksheets/sheet1.xml" to sheetXml(doc),
        ).map { (nome, conteudo) -> nome to conteudo.encodeToByteArray() }

        return ZipWriter.escrever(arquivos)
    }

    private fun contentTypesXml() = """
        |<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        |<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
        |<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
        |<Default Extension="xml" ContentType="application/xml"/>
        |<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
        |<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        |<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        |</Types>
    """.trimMargin()

    private fun relsRaizXml() = """
        |<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        |<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        |<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        |</Relationships>
    """.trimMargin()

    private fun workbookXml() = """
        |<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        |<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
        |<sheets><sheet name="Relatorio" sheetId="1" r:id="rId1"/></sheets>
        |</workbook>
    """.trimMargin()

    private fun workbookRelsXml() = """
        |<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        |<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        |<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
        |<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        |</Relationships>
    """.trimMargin()

    private fun stylesXml() = """
        |<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        |<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        |<fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
        |<fills count="1"><fill><patternFill patternType="none"/></fill></fills>
        |<borders count="1"><border/></borders>
        |<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
        |<cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
        |</styleSheet>
    """.trimMargin()

    private fun sheetXml(doc: ExportableDocument): String {
        val linhas = mutableListOf<List<String>>()
        doc.empresa?.let { linhas += listOf(it.nome) }
        linhas += listOf(doc.titulo)
        doc.subtitulo?.let { linhas += listOf(it) }
        linhas += doc.colunas
        linhas += doc.linhas
        if (doc.resumo.isNotEmpty()) doc.resumo.forEach { (rotulo, valor) -> linhas += listOf(rotulo, valor) }
        doc.rodape?.let { linhas += listOf(it) }

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n<sheetData>\n")
        linhas.forEachIndexed { indiceLinha, celulas ->
            val numeroLinha = indiceLinha + 1
            sb.append("<row r=\"$numeroLinha\">")
            celulas.forEachIndexed { indiceColuna, valor ->
                val ref = "${colunaExcel(indiceColuna)}$numeroLinha"
                sb.append("<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${escaparXml(valor)}</t></is></c>")
            }
            sb.append("</row>\n")
        }
        sb.append("</sheetData>\n</worksheet>")
        return sb.toString()
    }

    private fun colunaExcel(indiceZeroBased: Int): String {
        var n = indiceZeroBased + 1
        val sb = StringBuilder()
        while (n > 0) {
            val resto = (n - 1) % 26
            sb.insert(0, ('A' + resto))
            n = (n - 1) / 26
        }
        return sb.toString()
    }

    private fun escaparXml(texto: String): String = texto
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
