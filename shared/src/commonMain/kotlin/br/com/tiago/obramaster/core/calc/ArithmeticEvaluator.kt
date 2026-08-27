package br.com.tiago.obramaster.core.calc

/**
 * Avaliador de expressões aritméticas básicas (+ − × ÷ % parênteses) — SPEC_OBRA_MASTER.md §5.2.
 * '%' é tratado como sufixo unário "dividir por 100" (ex.: "10%" = 0.1), não como "X% de Y" —
 * a spec não define a semântica exata; esta é a leitura mais simples e sem ambiguidade.
 */
object ArithmeticEvaluator {

    fun avaliar(expressao: String): Double? {
        val tokens = tokenizar(expressao) ?: return null
        if (tokens.isEmpty()) return null
        return try {
            val parser = Parser(tokens)
            val resultado = parser.parseExpressao()
            if (parser.posicaoAtual != tokens.size) null else resultado
        } catch (e: AritmeticaException) {
            null
        }
    }

    private class AritmeticaException(mensagem: String) : Exception(mensagem)

    private sealed interface Token {
        data class Numero(val valor: Double) : Token
        data class Operador(val simbolo: Char) : Token
        data object AbreParenteses : Token
        data object FechaParenteses : Token
        data object Percentual : Token
    }

    private fun tokenizar(expressao: String): List<Token>? {
        val tokens = mutableListOf<Token>()
        var i = 0
        val normalizada = expressao.replace('×', '*').replace('÷', '/').replace(',', '.')
        while (i < normalizada.length) {
            val c = normalizada[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val inicio = i
                    while (i < normalizada.length && (normalizada[i].isDigit() || normalizada[i] == '.')) i++
                    val numeroStr = normalizada.substring(inicio, i)
                    val numero = numeroStr.toDoubleOrNull() ?: return null
                    tokens.add(Token.Numero(numero))
                }
                c == '+' || c == '-' || c == '*' || c == '/' -> {
                    tokens.add(Token.Operador(c))
                    i++
                }
                c == '(' -> {
                    tokens.add(Token.AbreParenteses)
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.FechaParenteses)
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.Percentual)
                    i++
                }
                else -> return null
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<Token>) {
        var posicaoAtual = 0
            private set

        private fun atual(): Token? = tokens.getOrNull(posicaoAtual)

        fun parseExpressao(): Double {
            var valor = parseTermo()
            while (true) {
                val token = atual()
                if (token is Token.Operador && (token.simbolo == '+' || token.simbolo == '-')) {
                    posicaoAtual++
                    val proximo = parseTermo()
                    valor = if (token.simbolo == '+') valor + proximo else valor - proximo
                } else {
                    break
                }
            }
            return valor
        }

        private fun parseTermo(): Double {
            var valor = parseFator()
            while (true) {
                val token = atual()
                if (token is Token.Operador && (token.simbolo == '*' || token.simbolo == '/')) {
                    posicaoAtual++
                    val proximo = parseFator()
                    if (token.simbolo == '/' && proximo == 0.0) throw AritmeticaException("Divisão por zero")
                    valor = if (token.simbolo == '*') valor * proximo else valor / proximo
                } else {
                    break
                }
            }
            return valor
        }

        private fun parseFator(): Double {
            val token = atual() ?: throw AritmeticaException("Expressão incompleta")
            var valor = when (token) {
                is Token.Operador -> {
                    if (token.simbolo != '-') throw AritmeticaException("Operador inesperado")
                    posicaoAtual++
                    -parseFator()
                }
                is Token.Numero -> {
                    posicaoAtual++
                    token.valor
                }
                Token.AbreParenteses -> {
                    posicaoAtual++
                    val interno = parseExpressao()
                    if (atual() != Token.FechaParenteses) throw AritmeticaException("Parêntese não fechado")
                    posicaoAtual++
                    interno
                }
                else -> throw AritmeticaException("Token inesperado")
            }
            while (atual() == Token.Percentual) {
                posicaoAtual++
                valor /= 100.0
            }
            return valor
        }
    }
}
