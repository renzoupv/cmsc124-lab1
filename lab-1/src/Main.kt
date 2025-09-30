// REPL Main Function
fun main() {
    println("Type 'scan' on a new line to scan your code, or 'exit' to quit.")
    println()

    while (true) {
        print("> ")
        val lines = mutableListOf<String>()

        // Read multiple lines until 'scan' command
        while (true) {
            val line = readLine()

            if (line == null || line.trim().lowercase() == "exit") {
                println("Goodbye!")
                return
            }

            // 'scan' command triggers scanning
            if (line.trim().lowercase() == "scan") {
                break // Done reading, time to scan
            }

            lines.add(line)
        }

        if (lines.isEmpty()) {
            continue
        }

        // Join all lines with newlines
        val input = lines.joinToString("\n")

        // Scan the input
        val scanner = Scanner(input)
        val tokens = scanner.scanTokens()

        // Print tokens
        for (token in tokens) {
            println(token)
        }
        println()
    }
}