// Interpreter.kt
class Interpreter {

    fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> expr.value
            is Expr.Grouping -> evaluate(expr.expression)
            is Expr.Unary -> evaluateUnary(expr)
            is Expr.Binary -> evaluateBinary(expr)
        }
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
