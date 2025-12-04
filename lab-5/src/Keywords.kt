val keywords = mapOf(
    // Control Flow
    "DEKLARAR" to TokenType.FUN,
    "basi" to TokenType.VAR,
    "sulat" to TokenType.PRINT,
    "kung" to TokenType.IF,
    "kung_indi" to TokenType.ELSE,
    "samtang" to TokenType.WHILE,
    "kada" to TokenType.FOR,
    "balik" to TokenType.RETURN,
    "ibalik" to TokenType.RETURN,

    // Booleans & Logic
    "korik" to TokenType.TRUE,
    "atik" to TokenType.FALSE,
    "waay" to TokenType.NIL,
    "kag" to TokenType.AND,
    "ukon" to TokenType.OR,

    // Arithmetic Operators (Mapped to TokenTypes)
    "dugang" to TokenType.PLUS,       // +
    "buhin" to TokenType.MINUS,       // -
    "padamo" to TokenType.STAR,       // *
    "dibaydibay" to TokenType.SLASH,  // /
    "kambyo" to TokenType.MODULO,     // %

    // Comparison Operators
    "mas_dako" to TokenType.GREATER,          // >
    "mas_gamay" to TokenType.LESS,            // <
    "dako_ukon_pareho" to TokenType.GREATER_EQUAL, // >=
    "gamay_ukon_pareho" to TokenType.LESS_EQUAL,   // <=
    "parehos" to TokenType.EQUAL_EQUAL,       // ==
    "lain" to TokenType.BANG_EQUAL,           // !=
    "indi" to TokenType.BANG,                 // !

    // Assignment
    "ituon_sa" to TokenType.EQUAL             // =
)