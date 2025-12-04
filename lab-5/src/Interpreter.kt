class Interpreter {
    val globals = Environment()
    private var environment = globals

    init {
        // Existing clock function
        globals.define("clock", object : LoxCallable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>) =
                System.currentTimeMillis() / 1000.0
            override fun toString() = "<native fn clock>"
        })

        // kalabaon(value): Returns the length of a string
        globals.define("kalabaon", object : LoxCallable {
            override fun arity() = 1
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                val value = arguments[0]
                return when (value) {
                    is String -> value.length.toDouble()
                    else -> throw RuntimeError(
                        Token(TokenType.IDENTIFIER, "kalabaon", null, 0),
                        "kalabaon expects a string argument."
                    )
                }
            }
            override fun toString() = "<native fn kalabaon>"
        })

        // tingob(value1, value2): Concatenates two values as strings
        globals.define("tingob", object : LoxCallable {
            override fun arity() = 2
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                val str1 = stringify(arguments[0])
                val str2 = stringify(arguments[1])
                return str1 + str2
            }
            override fun toString() = "<native fn tingob>"
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) execute(statement)
        } catch (error: RuntimeError) {
            println("[line ${error.token.line}] Runtime error: ${error.message}")
        }
    }

    private fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Expression -> evaluate(stmt.expression)
            is Stmt.Print -> println(stringify(evaluate(stmt.expression)))
            is Stmt.Var -> {
                val value = if (stmt.initializer != null) evaluate(stmt.initializer) else null
                environment.define(stmt.name.lexeme, value)
            }
            is Stmt.Block -> executeBlock(stmt.statements, Environment(environment))
            is Stmt.If -> if (isTruthy(evaluate(stmt.condition))) execute(stmt.thenBranch) else if (stmt.elseBranch != null) execute(stmt.elseBranch)
            is Stmt.While -> while (isTruthy(evaluate(stmt.condition))) execute(stmt.body)
            is Stmt.Function -> environment.define(stmt.name.lexeme, LoxFunction(stmt, environment))
            is Stmt.Return -> throw Return(if (stmt.value != null) evaluate(stmt.value) else null)
        }
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) execute(statement)
        } finally {
            this.environment = previous
        }
    }

    fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> expr.value
            is Expr.Grouping -> evaluate(expr.expression)
            is Expr.Unary -> evaluateUnary(expr)
            is Expr.Binary -> evaluateBinary(expr)
            is Expr.Variable -> environment.get(expr.name)
            is Expr.Assign -> {
                val value = evaluate(expr.value)
                environment.assign(expr.name, value)
                value
            }
            is Expr.Logical -> {
                val left = evaluate(expr.left)
                if (expr.operator.type == TokenType.OR) { if (isTruthy(left)) left else evaluate(expr.right) }
                else { if (!isTruthy(left)) left else evaluate(expr.right) }
            }
            is Expr.Call -> {
                val callee = evaluate(expr.callee)
                val args = expr.arguments.map { evaluate(it) }
                if (callee !is LoxCallable) throw RuntimeError(expr.paren, "Can only call functions.")
                if (args.size != callee.arity()) throw RuntimeError(expr.paren, "Expected ${callee.arity()} args but got ${args.size}.")
                callee.call(this, args)
            }
        }
    }

    private fun evaluateUnary(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> { checkNumber(expr.operator, right); -(right as Double) }
            else -> null
        }
    }

    private fun evaluateBinary(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        val op = expr.operator
        return when (op.type) {
            TokenType.GREATER -> { checkNumbers(op, left, right); (left as Double) > (right as Double) }
            TokenType.GREATER_EQUAL -> { checkNumbers(op, left, right); (left as Double) >= (right as Double) }
            TokenType.LESS -> { checkNumbers(op, left, right); (left as Double) < (right as Double) }
            TokenType.LESS_EQUAL -> { checkNumbers(op, left, right); (left as Double) <= (right as Double) }
            TokenType.BANG_EQUAL -> left != right
            TokenType.EQUAL_EQUAL -> left == right
            TokenType.MINUS -> { checkNumbers(op, left, right); (left as Double) - (right as Double) }
            TokenType.SLASH -> { checkNumbers(op, left, right); if (right as Double == 0.0) throw RuntimeError(op, "Div by zero") else (left as Double) / right }
            TokenType.STAR -> { checkNumbers(op, left, right); (left as Double) * (right as Double) }
            TokenType.PLUS -> {
                if (left is Double && right is Double) left + right
                else if (left is String || right is String) stringify(left) + stringify(right)
                else throw RuntimeError(op, "Operands must be two numbers or two strings.")
            }
            TokenType.MODULO -> {
                checkNumbers(op, left, right)
                if (right as Double == 0.0) throw RuntimeError(op, "Modulo by zero")
                (left as Double) % (right as Double)
            }
            else -> null
        }
    }

    private fun checkNumber(op: Token, operand: Any?) { if (operand !is Double) throw RuntimeError(op, "Operand must be a number.") }
    private fun checkNumbers(op: Token, left: Any?, right: Any?) { if (left !is Double || right !is Double) throw RuntimeError(op, "Operands must be numbers.") }
    private fun isTruthy(obj: Any?) = if (obj == null) false else if (obj is Boolean) obj else true
    fun stringify(obj: Any?): String {
        if (obj == null) return "waay"
        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) text = text.substring(0, text.length - 2)
            return text
        }
        return obj.toString()
    }
}