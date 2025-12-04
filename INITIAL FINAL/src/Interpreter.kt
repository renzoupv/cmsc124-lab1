import TokenType.*

class Interpreter {
    val globals = Environment()
    private var environment = globals

    init {
        // Native function: clock()
        globals.define("clock", object : LoxCallable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                return System.currentTimeMillis() / 1000.0
            }
            override fun toString() = "<native fn>"
        })

        // Native function: input()
        globals.define("input", object : LoxCallable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return readLine()
            }
            override fun toString() = "<native fn>"
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            println("[Line ${error.token.line}] Runtime Error: ${error.message}")
        }
    }

    private fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Expression -> evaluate(stmt.expression)
            is Stmt.Print -> {
                val value = evaluate(stmt.expression)
                println(stringify(value))
            }
            is Stmt.Var -> {
                var value: Any? = null
                if (stmt.initializer != null) {
                    value = evaluate(stmt.initializer)
                }
                environment.define(stmt.name.lexeme, value)
            }
            is Stmt.Block -> executeBlock(stmt.statements, Environment(environment))
            is Stmt.If -> {
                if (isTruthy(evaluate(stmt.condition))) {
                    execute(stmt.thenBranch)
                } else if (stmt.elseBranch != null) {
                    execute(stmt.elseBranch)
                }
            }
            is Stmt.While -> {
                while (isTruthy(evaluate(stmt.condition))) {
                    execute(stmt.body)
                }
            }
            is Stmt.Function -> {
                val function = LoxFunction(stmt, environment)
                environment.define(stmt.name.lexeme, function)
            }
            is Stmt.Return -> {
                var value: Any? = null
                if (stmt.value != null) value = evaluate(stmt.value)
                throw Return(value)
            }
        }
    }

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

    private fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> expr.value

            is Expr.Grouping -> evaluate(expr.expression)

            is Expr.Variable -> environment.get(expr.name)

            is Expr.Assign -> {
                val value = evaluate(expr.value)
                environment.assign(expr.name, value)
                value
            }

            is Expr.Binary -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)

                when (expr.operator.type) {
                    MINUS -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) - (right as Double)
                    }
                    PLUS -> {
                        when {
                            left is Double && right is Double -> left + right
                            left is String && right is String -> left + right
                            else -> throw RuntimeError(expr.operator,
                                "Operands must be two numbers or two strings.")
                        }
                    }
                    SLASH -> {
                        checkNumberOperands(expr.operator, left, right)
                        if ((right as Double) == 0.0) {
                            throw RuntimeError(expr.operator, "Division by zero.")
                        }
                        (left as Double) / right
                    }
                    STAR -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) * (right as Double)
                    }
                    GREATER -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) > (right as Double)
                    }
                    GREATER_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) >= (right as Double)
                    }
                    LESS -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) < (right as Double)
                    }
                    LESS_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) <= (right as Double)
                    }
                    BANG_EQUAL -> !isEqual(left, right)
                    EQUAL_EQUAL -> isEqual(left, right)
                    else -> null
                }
            }

            is Expr.Unary -> {
                val right = evaluate(expr.right)
                when (expr.operator.type) {
                    MINUS -> {
                        checkNumberOperand(expr.operator, right)
                        -(right as Double)
                    }
                    BANG -> !isTruthy(right)
                    else -> null
                }
            }

            is Expr.Logical -> {
                val left = evaluate(expr.left)
                if (expr.operator.type == OR) {
                    if (isTruthy(left)) return left
                } else {
                    if (!isTruthy(left)) return left
                }
                evaluate(expr.right)
            }

            is Expr.Call -> {
                val callee = evaluate(expr.callee)
                val arguments = expr.arguments.map { evaluate(it) }

                if (callee !is LoxCallable) {
                    throw RuntimeError(expr.paren, "Can only call functions and classes.")
                }
                if (arguments.size != callee.arity()) {
                    throw RuntimeError(expr.paren,
                        "Expected ${callee.arity()} arguments but got ${arguments.size}.")
                }
                callee.call(this, arguments)
            }

            is Expr.ArrayLiteral -> {
                val elements = mutableListOf<Any?>()
                for (element in expr.elements) {
                    elements.add(evaluate(element))
                }
                elements  // Return mutable list
            }

            is Expr.ArrayAccess -> {
                val array = evaluate(expr.array)
                val index = evaluate(expr.index)

                if (index !is Double) {
                    throw RuntimeError(expr.bracket, "Index must be a number.")
                }

                val i = index.toInt()

                if (array is String) {
                    if (i < 0 || i >= array.length) {
                        throw RuntimeError(expr.bracket, "String index out of bounds: $i (length: ${array.length}).")
                    }
                    return array[i].toString()  // Return character as string
                }

                // Original array logic
                if (array !is MutableList<*>) {
                    throw RuntimeError(expr.bracket, "Can only index arrays or strings.")
                }

                if (i < 0 || i >= array.size) {
                    throw RuntimeError(expr.bracket, "Array index out of bounds: $i (array size: ${array.size}).")
                }

                array[i]
            }

            is Expr.ArrayAssign -> {
                val array = evaluate(expr.array)
                val index = evaluate(expr.index)
                val value = evaluate(expr.value)

                if (index !is Double) {
                    throw RuntimeError(
                        Token(NUMBER, index.toString(), index, 0),
                        "Index must be a number."
                    )
                }

                val i = index.toInt()

                // ✅ ADD STRING SUPPORT
                if (array is String) {
                    if (i < 0 || i >= array.length) {
                        throw RuntimeError(
                            Token(NUMBER, i.toString(), i.toDouble(), 0),
                            "String index out of bounds: $i (length: ${array.length})."
                        )
                    }

                    // Convert string to mutable character list
                    val chars = array.toMutableList()

                    // Get the character to insert
                    val newChar = when {
                        value is String && value.isNotEmpty() -> value[0]
                        else -> throw RuntimeError(
                            Token(STRING, value.toString(), value, 0),
                            "Can only assign single character to string index."
                        )
                    }

                    chars[i] = newChar
                    val newString = chars.joinToString("")

                    // Update the variable in environment
                    if (expr.array is Expr.Variable) {
                        environment.assign(expr.array.name, newString)
                    }

                    return newString
                }

                // Original array logic
                if (array !is MutableList<*>) {
                    throw RuntimeError(
                        (expr.array as? Expr.Variable)?.name ?: Token(IDENTIFIER, "array", null, 0),
                        "Can only assign to arrays or strings."
                    )
                }

                if (i < 0 || i >= array.size) {
                    throw RuntimeError(
                        Token(NUMBER, i.toString(), i.toDouble(), 0),
                        "Array index out of bounds: $i (array size: ${array.size})."
                    )
                }

                @Suppress("UNCHECKED_CAST")
                (array as MutableList<Any?>)[i] = value
                value
            }
        }
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        if (obj is List<*>) {
            return "[" + obj.joinToString(", ") { stringify(it) } + "]"
        }

        return obj.toString()
    }
}