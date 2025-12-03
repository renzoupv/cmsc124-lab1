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

        // Add End Of File token when done
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            // Handle comments or division
            '/' -> {
                if (match('/')) {
                    // Single-line comment, skip until end of line
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else if (match('*')) {
                    // Multi-line comment
                    multiLineComment()
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            // Ignore spaces, tabs, and carriage return
            ' ', '\r', '\t' -> { }

            // New line -> move to next line
            '\n' -> line++

            // Handle string literals
            '"' -> string()

            // Everything else
            else -> {
                if (isDigit(c)) {
                    number()  // Numbers
                } else if (isAlpha(c)) {
                    identifier() // Identifiers or keywords
                } else {
                    // Unknown symbol → show error but continue
                    println("[Line $line] Error: Unexpected character: $c")
                }
            }
        }
    }

    // Move forward and return the current character
    private fun advance(): Char {
        return source[current++]
    }

    // Match next character only if it’s the expected one
    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    // Look at current character without consuming it
    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    // Look ahead one more character
    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    // Handle multi-line comments (/* ... */)
    private fun multiLineComment() {
        while (peek() != '*' || peekNext() != '/') {
            if (peek() == '\n') line++   // Count new lines inside comment
            if (isAtEnd()) {
                println("[Line $line] Error: Unterminated comment.")
                return
            }
            advance()
        }
        // Consume the closing */
        advance() // *
        advance() // /
    }

    // Handle string literals
    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++  // Allow multi-line strings
            advance()
        }

        if (isAtEnd()) {
            println("[Line $line] Error: Unterminated string.")
            return
        }

        advance() // Skip the closing "

        // Remove quotes and save actual text
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun number() {
        while (isDigit(peek())) advance()

        // Check for decimals
        if (peek() == '.' && isDigit(peekNext())) {
            advance() // consume "."
            while (isDigit(peek())) advance()
        }

        val value = source.substring(start, current).toDouble()
        addToken(TokenType.NUMBER, value)
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()

        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    // Utility functions
    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    // Add token without literal value
    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    // Add token with literal value
    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}