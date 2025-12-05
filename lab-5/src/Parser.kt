import TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    private fun declaration(): Stmt {
        return try {
            when {
                match(FUN) -> function("function")
                match(VAR) -> varDeclaration()
                else -> statement()
            }
        } catch (error: RuntimeException) {
            synchronize()
            throw error
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expected variable name")
        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expected ';' after variable declaration")
        return Stmt.Var(name, initializer)
    }

    private fun function(kind: String): Stmt {
        val name = consume(IDENTIFIER, "Expected $kind name")
        consume(LEFT_PAREN, "Expected '(' after $kind name")
        val parameters = mutableListOf<Token>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters")
                }
                parameters.add(consume(IDENTIFIER, "Expected parameter name"))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expected ')' after parameters")
        consume(LEFT_BRACE, "Expected '{' before $kind body")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    private fun statement(): Stmt {
        return when {
            match(FOR) -> forStatement()
            match(IF) -> ifStatement()
            match(PRINT) -> printStatement()
            match(RETURN) -> returnStatement()
            match(WHILE) -> whileStatement()
            match(LEFT_BRACE) -> Stmt.Block(block())
            else -> expressionStatement()
        }
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expected '(' after 'for'")

        val initializer = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        var condition: Expr? = null
        if (!check(SEMICOLON)) {
            condition = expression()
        }
        consume(SEMICOLON, "Expected ';' after loop condition")

        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = expression()
        }
        consume(RIGHT_PAREN, "Expected ')' after for clauses")

        var body = statement()

        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expected '(' after 'if'")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after if condition")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expected ';' after value")
        return Stmt.Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value = if (!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expected ';' after return value")
        return Stmt.Return(keyword, value)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expected '(' after 'while'")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after condition")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(RIGHT_BRACE, "Expected '}' after block")
        return statements
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expected ';' after expression")
        return Stmt.Expression(expr)
    }

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = or()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            when (expr) {
                is Expr.Variable -> {
                    return Expr.Assign(expr.name, value)
                }

                // ✅ ADD THIS:
                is Expr.ArrayAccess -> {
                    return Expr.ArrayAssign(expr.array, expr.index, value)
                }

                else -> error(equals, "Invalid assignment target")
            }
        }

        return expr
    }


    private fun or(): Expr {
        var expr = and()

        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return call()
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            when {
                match(LEFT_PAREN) -> expr = finishCall(expr)

                // ✅ ADD THIS:
                match(LEFT_BRACKET) -> {
                    val bracket = previous()
                    val index = expression()
                    consume(RIGHT_BRACKET, "Expected ']' after array index")
                    expr = Expr.ArrayAccess(expr, bracket, index)
                }

                else -> break
            }
        }

        return expr
    }


    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }

        val paren = consume(RIGHT_PAREN, "Expected ')' after arguments")
        return Expr.Call(callee, paren, arguments)
    }

    private fun primary(): Expr {
        return when {
            match(FALSE) -> Expr.Literal(false)
            match(TRUE) -> Expr.Literal(true)
            match(NIL) -> Expr.Literal(null)
            match(NUMBER, STRING) -> Expr.Literal(previous().literal)
            match(IDENTIFIER) -> Expr.Variable(previous())
            match(LEFT_PAREN) -> {
                val expr = expression()
                consume(RIGHT_PAREN, "Expected ')' after expression")
                Expr.Grouping(expr)
            }

            // ✅ ADD THIS:
            match(LEFT_BRACKET) -> {
                val bracket = previous()
                val elements = mutableListOf<Expr>()
                if (!check(RIGHT_BRACKET)) {
                    do {
                        elements.add(expression())
                    } while (match(COMMA))
                }
                consume(RIGHT_BRACKET, "Expected ']' after array elements")
                Expr.ArrayLiteral(bracket, elements)
            }

            else -> throw error(peek(), "Expected expression")
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType) = if (isAtEnd()) false else peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek().type == EOF
    private fun peek() = tokens[current]
    private fun previous() = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): RuntimeException {
        println("[Line ${token.line}] Error at '${token.lexeme}': $message")
        return RuntimeException(message)
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }
}
