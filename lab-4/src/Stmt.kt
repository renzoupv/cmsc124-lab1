// Stmt.kt
// Defines all statement types in your language

sealed class Stmt {
    // Program root: contains all top-level declarations
    data class Program(val statements: List<Stmt>) : Stmt()

    // Expression statement: evaluate an expression and discard result
    data class Expression(val expression: Expr) : Stmt()

    // Print statement: evaluate and display result
    data class Print(val expression: Expr) : Stmt()

    // Variable declaration
    data class Var(val name: Token, val initializer: Expr?) : Stmt()

    // Block statement
    data class Block(val statements: List<Stmt>) : Stmt()

    // Function declaration
    data class Function(val name: Token, val params: List<Token>, val body: List<Stmt>) : Stmt()

    // Return statement
    data class Return(val keyword: Token, val value: Expr?) : Stmt()

    // While loop
    data class While(val condition: Expr, val body: Stmt) : Stmt()

    // If statement
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
}
