// AST.kt
// Abstract Syntax Tree with linked nodes

sealed class ASTNode {
    // Program root node
    data class Program(val firstStatement: StatementNode?) : ASTNode()

    // Statement node with link to next sibling
    data class StatementNode(
        val statement: Stmt,
        val next: StatementNode?  // Link to next statement
    ) : ASTNode()
}
