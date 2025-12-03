// Main.kt (REPL for Lab 3 - evaluation)
fun main() {
    val interpreter = Interpreter()

    println("Evaluator — type expressions, 'exit' to quit.")
    while (true) {
        print("> ")
        val line = readLine() ?: break
        if (line.trim().lowercase() == "exit") break
        if (line.isBlank()) continue

        // Scan
        val scanner = Scanner(line)
        val tokens = scanner.scanTokens()

        // Parse
        val parser = Parser(tokens)
        val expression = try {
            parser.parse()
        } catch (e: Exception) {
            // parser prints errors already; skip evaluation
            continue
        }

        if (expression == null) {
            println("nil")
            continue
        }

        // Evaluate
        try {
            val result = interpreter.evaluate(expression)
            println(interpreter.stringify(result))
        } catch (err: RuntimeError) {
            // Print nicely: [line X] Runtime error: message
            val lineNum = err.token.line
            println("[line $lineNum] Runtime error: ${err.message}")
        } catch (err: Exception) {
            // Unexpected; still show
            println("Runtime error: ${err.message}")
        }
    }
}
