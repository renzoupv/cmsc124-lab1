class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd() = current >= source.length

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '-' -> {
                if (match('-')) {  // '--' closes blocks
                    addToken(TokenType.RIGHT_BRACE)
                } else {  // '-' opens blocks
                    addToken(TokenType.LEFT_BRACE)
                }
            }
            '.' -> addToken(TokenType.SEMICOLON)   // Dot terminates staments
            ',' -> addToken(TokenType.COMMA)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '%' -> addToken(TokenType.MODULO) // Support % symbol just in case

            // Logic not using keywords
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            // Comments (#) and Division (/)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else if (match('*')) {
                    while (peek() != '*' || peekNext() != '/') {
                        if (peek() == '\n') line++
                        if (isAtEnd()) return
                        advance()
                    }
                    advance(); advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            '#' -> { // KwentoLang Comments
                while (peek() != '\n' && !isAtEnd()) advance()
            }

            ' ', '\r', '\t' -> { }
            '\n' -> line++
            '"' -> string()
            else -> {
                if (isDigit(c)) number()
                else if (isAlpha(c)) identifier()
                else println("[Line $line] Error: Unexpected character: $c")
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            println("[Line $line] Error: Unterminated string.")
            return
        }
        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun number() {
        while (isDigit(peek())) advance()
        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun advance() = source[current++]
    private fun peek() = if (isAtEnd()) '\u0000' else source[current]
    private fun peekNext() = if (current + 1 >= source.length) '\u0000' else source[current + 1]
    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) return false
        current++
        return true
    }
    private fun isDigit(c: Char) = c in '0'..'9'
    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)
    private fun addToken(type: TokenType, literal: Any? = null) {
        tokens.add(Token(type, source.substring(start, current), literal, line))
    }
}