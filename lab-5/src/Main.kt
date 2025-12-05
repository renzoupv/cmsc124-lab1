import java.io.File

fun main(args: Array<String>) {
    when {
        args.isEmpty() -> runRepl()
        else -> runFile(args[0])
    }
}

fun runRepl() {
    println("""
        ════════════════════════════════════════════════════════
        Unified Language System - REPL Mode
        ════════════════════════════════════════════════════════
        Type 'exit' or 'quit' to exit
        Type 'help' for usage information
        ════════════════════════════════════════════════════════
    """.trimIndent())

    val interpreter = Interpreter()

    while (true) {
        print(">>> ")
        val line = readLine() ?: break

        when (line.trim().lowercase()) {
            "exit", "quit" -> {
                println("Goodbye!")
                break
            }
            "help" -> printUsage()
            "" -> continue
            else -> {
                try {
                    // Try to detect if it's a DSL snippet or Lox code
                    val scanner = Scanner(line)
                    val tokens = scanner.scanTokens()

                    if (tokens.isNotEmpty() && tokens[0].type == TokenType.GAME) {
                        println("Error: DSL mode requires a complete file. Use: java -jar language.jar game.txt")
                    } else {
                        // Execute as Lox
                        val parser = Parser(tokens)
                        val statements = parser.parse()
                        interpreter.interpret(statements)
                    }
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
        }
    }
}

fun runFile(filename: String) {
    val file = File(filename)

    if (!file.exists()) {
        println("Error: File '$filename' not found")
        return
    }

    val source = file.readText()

    // Auto-detect mode based on first token
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    if (tokens.isNotEmpty() && tokens[0].type == TokenType.GAME) {
        // TacShooter DSL mode
        runTacShooterDSL(source, filename)
    } else {
        // Lox mode
        runLox(source)
    }
}

fun printUsage() {
    println("""
        ════════════════════════════════════════════════════════
        Unified Language System
        ════════════════════════════════════════════════════════
        
        Supports THREE modes:
        
        1. REPL MODE - Interactive Programming
           - Run without arguments: java -jar language.jar
           - Execute Lox statements interactively
           - Type 'exit' or 'quit' to exit
           
        2. LOX MODE - Imperative Programming
           - If/else, loops, functions, closures, arrays
           - Example: java -jar language.jar script.lox
           
        3. TACSHOOTER DSL MODE - Game Configuration
           - Starts with "GAME" keyword
           - Example: java -jar language.jar game.txt
        
        Mode is detected automatically!
        ════════════════════════════════════════════════════════
    """.trimIndent())
}

fun runLox(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    val parser = Parser(tokens)
    val statements = parser.parse()

    val interpreter = Interpreter()
    interpreter.interpret(statements)
}

fun runTacShooterDSL(source: String, filename: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    val parser = TacParser(tokens)
    val ast = parser.parseGame()

    val interpreter = ConfigInterpreter()
    val config = interpreter.interpret(ast)

    val codegen = CodeGenerator()
    val json = codegen.generate(ast)

    // Write to file
    File("output").mkdirs()
    val outputFile = "output/${File(filename).nameWithoutExtension}_config.json"
    File(outputFile).writeText(json)

    println("✅ Generated: $outputFile")
    println("\nPreview:")
    println(json.take(600) + if (json.length > 600) "\n..." else "")
}