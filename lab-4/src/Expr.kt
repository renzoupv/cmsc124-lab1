// Expr.kt
// Add these two new expression types to your existing sealed class

sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()

    // NEW: Variable reference (reading a variable's value)
    // Example: x
    data class Variable(val name: Token) : Expr()

    // NEW: Assignment (setting a variable's value)
    // Example: x = 10
    data class Assign(val name: Token, val value: Expr) : Expr()
}
