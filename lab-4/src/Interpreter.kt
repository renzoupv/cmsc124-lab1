// Interpreter.kt
// Extended to execute statements and manage variables

class Interpreter {
    // Global environment for variables
    private var environment = Environment()

    /**
     * Interpret a list of statements
     */
    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            println("[line ${error.token.line}] Runtime error: ${error.message}")
        }
    }

    // --- Statement Execution ---

    /**
     * Execute a single statement
     */
    private fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Expression -> executeExpressionStmt(stmt)
            is Stmt.Print -> executePrintStmt(stmt)
            is Stmt.Var -> executeVarStmt(stmt)
            is Stmt.Block -> executeBlockStmt(stmt)
        }
    }

    /**
     * Execute expression statement: evaluate and discard result
     */
    private fun executeExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    /**
     * Execute print statement: evaluate and display result
     */
    private fun executePrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    /**
     * Execute variable declaration
     */
    private fun executeVarStmt(stmt: Stmt.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
    }

    /**
     * Execute block: create new environment, execute statements, restore old environment
     */
    private fun executeBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    /**
     * Execute block with given environment
     */
    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    // --- Expression Evaluation ---

    /**
     * Evaluate an expression and return its value
     */
    fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> expr.value
            is Expr.Grouping -> evaluate(expr.expression)
            is Expr.Unary -> evaluateUnary(expr)
            is Expr.Binary -> evaluateBinary(expr)
            is Expr.Variable -> environment.get(expr.name)
            is Expr.Assign -> evaluateAssign(expr)
        }
    }

    /**
     * Evaluate assignment expression
     */
    private fun evaluateAssign(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    private fun evaluateUnary(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.BANG -> return !isTruthy(right)
            TokenType.MINUS -> {
                if (right !is Double) {
                    throw RuntimeError(expr.operator, "Operand must be a number.")
                }
                return -right
            }
            else -> throw RuntimeError(expr.operator, "Unknown unary operator ${expr.operator.lexeme}")
        }
    }

    private fun evaluateBinary(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        val op = expr.operator

        when (op.type) {
            TokenType.PLUS -> {
                // number + number or string + string (concatenate)
                if (left is Double && right is Double) return left + right
                if (left is String && right is String) return left + right
                // Allow mixed string concatenation
                if (left is String || right is String) {
                    return stringify(left) + stringify(right)
                }
                throw RuntimeError(op, "Operands must be two numbers or two strings.")
            }
            TokenType.MINUS -> {
                checkNumberOperands(op, left, right)
                return (left as Double) - (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(op, left, right)
                return (left as Double) * (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(op, left, right)
                val r = right as Double
                if (r == 0.0) throw RuntimeError(op, "Division by zero.")
                return (left as Double) / r
            }
            // Comparisons: require numbers
            TokenType.GREATER -> {
                checkNumberOperands(op, left, right)
                return (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(op, left, right)
                return (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(op, left, right)
                return (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(op, left, right)
                return (left as Double) <= (right as Double)
            }
            // Equality (works on any types)
            TokenType.EQUAL_EQUAL -> return isEqual(left, right)
            TokenType.BANG_EQUAL -> return !isEqual(left, right)
            else -> throw RuntimeError(op, "Unknown binary operator ${op.lexeme}")
        }
    }

    private fun checkNumberOperands(op: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(op, "Operands must be numbers.")
    }

    private fun isTruthy(value: Any?): Boolean {
        // Only false and null are falsey — everything else is truthy.
        if (value == null) return false
        if (value is Boolean) return value
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        // null equality: nil == nil -> true
        if (a == null && b == null) return true
        if (a == null) return false
        // for Double and String and Boolean, rely on Kotlin equals
        return a == b
    }

    fun stringify(value: Any?): String {
        if (value == null) return "nil"
        if (value is Boolean) return value.toString()
        if (value is Double) {
            // print integer values without .0, e.g. 42.0 -> 42
            if (value % 1.0 == 0.0) {
                // safe to print as Long if fits
                val longVal = value.toLong()
                return longVal.toString()
            } else {
                return value.toString()
            }
        }
        return value.toString()
    }
}
