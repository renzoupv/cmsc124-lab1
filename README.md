# Unified Language System

## Creator

Renz de Arroz & Johann Ross Yap

## Language Overview

The **Unified Language System** is a hybrid programming environment designed to bridge the gap between imperative game logic and declarative game configuration. It addresses the specific needs of game development by operating in two distinct modes:

1.  **Lox Mode (Imperative):** A general-purpose, dynamically typed scripting language used for implementing core logic, calculations, and control flow.
2.  **TacShooter DSL Mode (Declarative):** A Domain-Specific Language (DSL) optimized for defining tactical shooter entities (Agents, Weapons, Maps). It parses high-level configuration syntax and compiles it directly into JSON for consumption by external game engines.

## Keywords

The following words are reserved and cannot be used as identifiers:

### Control Flow & Logic (Lox)
* `var`: Declares a new variable.
* `fun`: Defines a function.
* `return`: Returns a value from a function.
* `if`, `else`: Conditional branching.
* `while`, `for`: Loop constructs.
* `print`: Outputs text to the console.
* `and`, `or`: Logical operators.
* `true`, `false`, `nil`: Boolean and null literals.
* `class`, `this`, `super`: Reserved for object-oriented features.

### Game Configuration (DSL)
* **Structure:** `GAME`, `CONFIG`, `MATCH`, `ECONOMY`, `STATUS_EFFECTS`, `MAP`, `TEAMS`, `SITES`, `CALLOUTS`.
* **Entities:** `AGENTS`, `AGENT`, `WEAPONS`, `WEAPON`, `ABILITIES`, `ABILITY`, `EFFECT`, `TEAM`, `SITE`.
* **Properties:** `STATS`, `ENTRIES`, `FALLOFF`.
* **Events:** `CAST`, `ON_KILL`, `ON_APPLY`, `ON_TICK`, `ON_EXPIRE`.
* **Values:** `yes`, `no` (Boolean aliases), `with` (Parameter injection).
* **Targets:** `ENEMY`, `ALLY`, `SELF`, `ALL`.
* **Types:** `AOE`, `SINGLE_TARGET`, `MOBILITY`, `OFFENSIVE`, `DEFENSIVE`, `UTILITY`, `SUPPORT`, `CONTROL`, `BUFF`, `DEBUFF`, `NEUTRAL`, `PASSIVE`.

## Operators

### Arithmetic & String
* `+`: Addition (numbers) or concatenation (strings).
* `-`: Subtraction.
* `*`: Multiplication.
* `/`: Division.

### Comparison & Logic
* `>`, `>=`: Greater than, Greater than or equal.
* `<`, `<=`: Less than, Less than or equal.
* `==`, `!=`: Equality and inequality checks.
* `!`: Logical NOT.

### Assignment & Definition
* `=`: Variable assignment.
* `::`: Property assignment (DSL only).
* `@`: Decorator prefix (DSL only).

### Flow & Grouping
* `=>`: Event pipeline mapping (DSL only).
* `->`: Action targeting (DSL only).
* `[` `]`: Array/List initialization or Section grouping.
* `(` `)`: Grouping expressions or function parameters.
* `{` `}`: Block scope delimiter (Lox only).

## Literals

* **Numbers:** Double-precision floating point numbers (e.g., `123`, `10.5`).
* **Strings:** Text enclosed in double quotes (e.g., `"Hello"`).
* **Booleans:** `true`/`false` (Lox) or `yes`/`no` (DSL).
* **Arrays:** Ordered lists of values enclosed in brackets (e.g., `[1, 2, 3]`).
* **Durations:** Numeric values suffixed with 's' (e.g., `5s`). Parsed as `{value: 5.0, unit: "seconds"}`.
* **Percentages:** Numeric values suffixed with '%' (e.g., `25%`). Parsed as `{value: 25.0, unit: "percent"}`.

## Identifiers

* **Format:** Must start with a letter (`a-z`, `A-Z`) or underscore `_`. Subsequent characters can be letters, digits (`0-9`), or underscores.
* **Case Sensitivity:** Identifiers are case-sensitive (e.g., `myVar` is different from `MyVar`).
* **Usage:** Used for variable names, function names, DSL entity names (e.g., `Jett`), and action names.

## Comments

* **C-Style:** Starts with `//` and continues to the end of the line.
* **Hash-Style:** Starts with `#` and continues to the end of the line (Scripting style).
* **Nested Comments:** Not supported.

## Syntax Style

* **Lox Mode:**
    * Statements are terminated by semicolons `;`.
    * Blocks are delimited by curly braces `{}`.
    * Whitespace is insignificant.
* **DSL Mode:**
    * Structure is hierarchical using square brackets `[]`.
    * Properties use `key :: value` syntax.
    * Pipelines use arrow notation `=>` and `->`.

## Design Rationale
This system splits game development into two parts: a scripting language for logic and a simple configuration tool for game data. This allows programmers to write complex code while designers safely tweak stats like damage or cooldowns using easy-to-read text. Everything saves as standard JSON files so it works with any game engine, and we added shortcuts for time and percentages to make game balancing faster and error-free.

## Sample Code

### Lox Mode (Scripting)
```javascript
// Function definition
fun calculateDamage(base, multiplier) {
    return base * multiplier;
}

# Array manipulation
var scores = [10, 20, 30];
scores[1] = 50;

print "Final Damage: " + calculateDamage(scores[1], 1.5);
