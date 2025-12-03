// Parser.kt
// Extended to parse both expressions and statements

class Parser(private val tokens: List<Token>) {
    private var current = 0

    /**
     * Parse a program: list of declarations/statements
     */
    fun parse(): ASTNode.Program {
        val firstStatement = parseStatements()
        return ASTNode.Program(firstStatement)
    }

    /**
     * Parse statements as linked nodes
     */
    private fun parseStatements(): ASTNode.StatementNode? {
        if (isAtEnd()) return null

        val stmt = try {
            declaration()
        } catch (error: ParseError) {
            synchronize()
            return parseStatements()  // Skip error and continue
        }

        val next = parseStatements()  // Recursively parse next statement
        return ASTNode.StatementNode(stmt, next)
    }

    // --- Statement Parsing ---

    /**
     * declaration → varDecl | statement
     */
    private fun declaration(): Stmt {
        if (match(TokenType.VAR)) return varDeclaration()
        return statement()
    }

    /**
     * varDecl → "var" IDENTIFIER ( "=" expression )? ";"
     * Example: var x = 10;
     */
    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    /**
     * statement → exprStmt | printStmt | block
     */
    private fun statement(): Stmt {
        if (match(TokenType.PRINT)) return printStatement()
        if (match(TokenType.LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    /**
     * printStmt → "print" expression ";"
     * Example: print "Hello";
     */
    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    /**
     * block → "{" declaration* "}"
     * Example: { var x = 5; print x; }
     */
    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    /**
     * exprStmt → expression ";"
     * Example: 2 + 3;
     */
    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    // --- Expression Parsing ---

    /**
     * expression → assignment
     */
    private fun expression(): Expr = assignment()

    /**
     * assignment → IDENTIFIER "=" assignment | equality
     * Right-associative: a = b = c parses as a = (b = c)
     */
    private fun assignment(): Expr {
        val expr = equality()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment() // Right-associative recursion

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    /**
     * equality → comparison ( ( "!=" | "==" ) comparison )*
     */
    private fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*
     */
    private fun comparison(): Expr {
        var expr = term()

        while (match(
                TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL
            )) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * term → factor ( ( "-" | "+" ) factor )*
     */
    private fun term(): Expr {
        var expr = factor()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * factor → unary ( ( "/" | "*" ) unary )*
     */
    private fun factor(): Expr {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * unary → ( "!" | "-" ) unary | primary
     */
    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    /**
     * primary → NUMBER | STRING | "true" | "false" | "nil"
     *         | "(" expression ")" | IDENTIFIER
     */
    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        error(peek(), "Expect expression.")
        throw ParseError()
    }

    // --- Utility Methods ---

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        error(peek(), message)
        throw ParseError()
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String) {
        println("[line ${token.line}] Error at '${token.lexeme}': $message")
    }

    /**
     * Synchronize after parse error by skipping to next statement boundary
     */
    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR,
                TokenType.FOR, TokenType.IF, TokenType.WHILE,
                TokenType.PRINT, TokenType.RETURN -> return
                else -> advance()
            }
        }
    }

    class ParseError : RuntimeException()
}
