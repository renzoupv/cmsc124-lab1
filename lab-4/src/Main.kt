// Main.kt
// Supports both REPL (interactive) and script file execution

import java.io.File

fun main(args: Array<String>) {
    when {
        args.isEmpty() -> runRepl()
        args.size == 1 -> runFile(args[0])
        else -> {
            println("Usage: kotlin Main [script]")
            System.exit(64)
        }
    }
}

/**
 * REPL Mode: Interactive prompt
 * In REPL, we print the result of expressions automatically
 */
fun runRepl() {
    val interpreter = Interpreter()
    println("Language REPL — type statements or expressions, 'exit' to quit.")

    while (true) {
        print("> ")
        val line = readLine() ?: break
        if (line.trim().lowercase() == "exit") break
        if (line.isBlank()) continue

        run(line, interpreter, isRepl = true)
    }
}

/**
 * Script Mode: Execute file
 * In script mode, only print statements produce output
 */
fun runFile(path: String) {
    val source = try {
        File(path).readText()
    } catch (e: Exception) {
        println("Error reading file: ${e.message}")
        System.exit(66)
        return
    }

    val interpreter = Interpreter()
    run(source, interpreter, isRepl = false)
}

/**
 * Traverse AST tree and collect statements
 */
fun traverseAST(program: ASTNode.Program): List<Stmt> {
    val statements = mutableListOf<Stmt>()
    var current = program.firstStatement

    while (current != null) {
        statements.add(current.statement)
        current = current.next
    }

    return statements
}

/**
 * Run source code
 */
fun run(source: String, interpreter: Interpreter, isRepl: Boolean) {
    // Scan
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    // Check for scan errors
    if (tokens.any { it.type == TokenType.EOF && it.lexeme != "" }) {
        return // Scanner already printed errors
    }

    // Parse
    val parser = Parser(tokens)
    val ast = parser.parse()

    // Convert tree to list for execution (or traverse directly)
    val statements = traverseAST(ast)

    // Check for parse errors
    if (statements.isEmpty() && !isRepl) return

    // In REPL mode, if we have a single expression statement, print its value
    if (isRepl && statements.size == 1) {
        val stmt = statements[0]
        if (stmt is Stmt.Expression) {
            try {
                val value = interpreter.evaluate(stmt.expression)
                println(interpreter.stringify(value))
            } catch (err: RuntimeError) {
                println("[line ${err.token.line}] Runtime error: ${err.message}")
            }
            return
        }
    }

    // Execute
    interpreter.interpret(statements)
}
