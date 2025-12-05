import TokenType.*

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
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            // Lox characters
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)

            // TacShooter DSL characters
            '[' -> addToken(LEFT_BRACKET)
            ']' -> addToken(RIGHT_BRACKET)
            '@' -> addToken(AT)

            // Shared operators
            '+' -> addToken(PLUS)
            '-' -> {
                if (match('>')) {
                    addToken(RIGHT_ARROW) // DSL: ->
                } else if (peek().isDigit()) {
                    // Negative number: -50, -50%, -5s
                    number()
                } else {
                    addToken(MINUS)
                }
            }

            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)

            '=' -> {
                if (match('=')) {
                    addToken(EQUAL_EQUAL)
                } else if (match('>')) {
                    addToken(ARROW) // DSL: =>
                } else {
                    addToken(EQUAL)
                }
            }

            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)

            ':' -> {
                if (match(':')) {
                    addToken(DOUBLE_COLON) // DSL: ::
                } else {
                    error("Unexpected character ':'. Did you mean '::'?")
                }
            }

            '/' -> {
                if (match('/')) {
                    // Comment until end of line
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(SLASH)
                }
            }

            // ✅ ADD HASH COMMENT SUPPORT
            '#' -> {
                // Comment until end of line (Python/Shell style)
                while (peek() != '\n' && !isAtEnd()) advance()
            }

            ' ', '\r', '\t' -> {} // Ignore whitespace
            '\n' -> line++
            '"' -> string()

            else -> {
                when {
                    c.isDigit() -> number()
                    c.isLetter() || c == '_' -> identifier()
                    else -> error("Unexpected character: $c")
                }
            }
        }
    }

    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') advance()
        val text = source.substring(start, current)
        val type = keywords[text] ?: IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (peek().isDigit()) advance()

        // Look for decimal part
        if (peek() == '.' && peekNext().isDigit()) {
            advance() // consume '.'
            while (peek().isDigit()) advance()
        }

        // Check for duration suffix (DSL)
        if (peek() == 's' && !peekNext().isLetterOrDigit()) {
            advance()
            val value = source.substring(start, current - 1).toDouble()
            addToken(DURATION, value)
            return
        }

        // Check for percentage suffix (DSL)
        if (peek() == '%') {
            advance()
            val value = source.substring(start, current - 1).toDouble()
            addToken(PERCENTAGE, value)
            return
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            error("Unterminated string")
            return
        }

        advance() // closing "
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek() = if (isAtEnd()) '\u0000' else source[current]
    private fun peekNext() = if (current + 1 >= source.length) '\u0000' else source[current + 1]
    private fun isAtEnd() = current >= source.length
    private fun advance() = source[current++]

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun error(message: String) {
        println("[Line $line] Error: $message")
    }
}