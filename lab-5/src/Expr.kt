sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()
    data class Variable(val name: Token) : Expr()
    data class Assign(val name: Token, val value: Expr) : Expr()
    data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr()
    data class ArrayLiteral(val bracket: Token, val elements: List<Expr>) : Expr()
    data class ArrayAccess(val array: Expr, val bracket: Token, val index: Expr) : Expr()
    data class ArrayAssign(val array: Expr, val index: Expr, val value: Expr) : Expr()
}
