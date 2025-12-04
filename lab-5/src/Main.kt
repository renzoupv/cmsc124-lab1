import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: ILLONGGO GODS  [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runRepl()
    }
}

fun runFile(path: String) {
    val bytes = File(path).readBytes()
    run(String(bytes))
}

fun runRepl() {
    val interpreter = Interpreter()
    println("ILLONGGO GODS REPL (Type 'exit' to quit)")
    while (true) {
        print("> ")
        val line = readLine()
        if (line == null || line == "exit") break
        run(line, interpreter)
    }
}

fun run(source: String, interpreter: Interpreter = Interpreter()) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    // Stop if scanning errors (not fully implemented here, but handled in Scanner)

    val parser = Parser(tokens)
    val statements = parser.parse()

    // Stop if parse errors
    if (statements.isEmpty()) return

    interpreter.interpret(statements)
}