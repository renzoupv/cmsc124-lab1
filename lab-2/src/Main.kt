// Main.kt
fun main() {
    println("=== CMSC124 Lab 2: Expression Parser ===")
    println("Type an expression (or 'exit' to quit):")

    while (true) {
        print("> ")
        val line = readLine() ?: break
        if (line.lowercase() == "exit") break
        if (line.isBlank()) continue

        val scanner = Scanner(line)
        val tokens = scanner.scanTokens()

        val parser = Parser(tokens)
        val expression = parser.parse()

        val printer = AstPrinter()
        printer.print(expression)
    }
}
