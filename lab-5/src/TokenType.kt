enum class TokenType {
    // ========== LOX TOKENS ==========
    // Single-character
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Lox Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    // ========== TACSHOOTER DSL TOKENS ==========
    // DSL Keywords
    GAME, AGENTS, AGENT, ABILITIES, ABILITY, WEAPONS, WEAPON,
    MAP, SITES, SITE, TEAMS, TEAM, ECONOMY, STATUS_EFFECTS, EFFECT,
    MATCH, TIMING, OBJECTIVE, BONUSES, ARMOR, DAMAGE, CAST, ON_KILL,
    ON_APPLY, ON_TICK, ON_EXPIRE,

    // DSL Properties
    STATS, CONFIG, CALLOUTS, ENTRIES, FALLOFF,

    // DSL Types
    AOE, SINGLE_TARGET, MOBILITY, OFFENSIVE, DEFENSIVE, UTILITY,
    SUPPORT, CONTROL, BUFF, DEBUFF, NEUTRAL, PASSIVE,

    // DSL Targets
    ENEMY, ALLY, SELF, ALL,

    // DSL Values
    YES, NO,

    // DSL Operators
    DOUBLE_COLON,    // ::
    ARROW,           // =>
    RIGHT_ARROW,     // ->
    AT,              // @
    LEFT_BRACKET,    // [
    RIGHT_BRACKET,   // ]
    WITH,

    // ========== SHARED ==========
    IDENTIFIER, STRING, NUMBER,
    DURATION,      // 5s
    PERCENTAGE,    // 30%
    EOF
}