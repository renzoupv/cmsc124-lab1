// Environment.kt
// Manages variable storage with support for nested scopes

class Environment(private val enclosing: Environment? = null) {
    // Store variable name -> value mappings
    private val values = mutableMapOf<String, Any?>()

    /**
     * Define a new variable in this scope.
     * Allows redefinition (later you can make it an error if needed).
     */
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    /**
     * Get a variable's value.
     * Searches this scope, then parent scopes recursively.
     * Throws RuntimeError if variable not found.
     */
    fun get(name: Token): Any? {
        // Check current scope
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        // Check parent scope
        if (enclosing != null) {
            return enclosing.get(name)
        }

        // Not found anywhere
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    /**
     * Assign to an existing variable.
     * Searches this scope, then parent scopes recursively.
     * Throws RuntimeError if variable doesn't exist.
     */
    fun assign(name: Token, value: Any?) {
        // Check current scope
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        // Check parent scope
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        // Not found - can't assign to undefined variable
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}
