import TokenType.*

class TacParser(private val tokens: List<Token>) {
    private var current = 0

    fun parseGame(): GameNode {
        consume(GAME, "Expected 'GAME' keyword")
        val name = consume(IDENTIFIER, "Expected game name").lexeme
        consume(LEFT_BRACKET, "Expected '[' after game name")

        val sections = mutableListOf<SectionNode>()
        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            when {
                match(CONFIG) -> sections.add(parseConfig())
                match(AGENTS) -> sections.add(parseAgents())
                match(WEAPONS) -> sections.add(parseWeapons())
                match(MAP) -> sections.add(parseMap())
                match(ECONOMY) -> sections.add(parseEconomy())
                match(STATUS_EFFECTS) -> sections.add(parseStatusEffects())
                match(MATCH) -> sections.add(parseMatch())
                else -> throw error(peek(), "Unexpected section: ${peek().lexeme}")
            }
        }

        consume(RIGHT_BRACKET, "Expected ']' after game body")
        return GameNode(name, sections)
    }

    private fun parseConfig(): ConfigSection {
        consume(LEFT_BRACKET, "Expected '[' after config")
        val properties = parsePropertyMap()
        consume(RIGHT_BRACKET, "Expected ']' after config body")
        return ConfigSection(properties)
    }

    private fun parseAgents(): AgentsSection {
        consume(LEFT_BRACKET, "Expected '[' after AGENTS")
        val agents = mutableListOf<AgentNode>()
        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            if (match(AGENT)) {
                agents.add(parseAgent())
            }
        }
        consume(RIGHT_BRACKET, "Expected ']' after agents")
        return AgentsSection(agents)
    }

    private fun parseAgent(): AgentNode {
        val name = consume(IDENTIFIER, "Expected agent name").lexeme
        consume(LEFT_BRACKET, "Expected '[' after agent name")

        val decorators = mutableListOf<String>()
        val stats = mutableMapOf<String, Any?>()
        val abilities = mutableListOf<AbilityNode>()

        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            when {
                match(AT) -> {
                    val decorator = consume(IDENTIFIER, "Expected decorator name").lexeme
                    consume(DOUBLE_COLON, "Expected '::'")
                    val value = advance().lexeme
                    decorators.add("$decorator=$value")
                }
                match(STATS) -> {
                    consume(LEFT_BRACKET, "Expected '['")
                    stats.putAll(parsePropertyMap())
                    consume(RIGHT_BRACKET, "Expected ']'")
                }
                match(ABILITIES) -> {
                    consume(LEFT_BRACKET, "Expected '['")
                    while (!check(RIGHT_BRACKET)) {
                        if (match(ABILITY)) {
                            abilities.add(parseAbility())
                        }
                    }
                    consume(RIGHT_BRACKET, "Expected ']'")
                }
                else -> break
            }
        }

        consume(RIGHT_BRACKET, "Expected ']' after agent body")
        return AgentNode(name, decorators, stats, abilities)
    }

    private fun parseAbility(): AbilityNode {
        val name = consume(IDENTIFIER, "Expected ability name").lexeme
        consume(LEFT_BRACKET, "Expected '[' after ability name")

        val decorators = mutableListOf<String>()
        val properties = mutableMapOf<String, Any?>()
        var cast: BehaviorPipeline? = null
        val events = mutableMapOf<String, BehaviorPipeline>()

        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            when {
                match(AT) -> {
                    val decorator = advance().lexeme
                    decorators.add(decorator)
                }
                match(CAST) -> {
                    consume(ARROW, "Expected '=>'")
                    cast = parseBehaviorPipeline()
                }
                match(ON_KILL, ON_APPLY, ON_TICK, ON_EXPIRE) -> {
                    val eventType = previous().lexeme
                    consume(ARROW, "Expected '=>'")
                    events[eventType] = parseBehaviorPipeline()
                }
                else -> {
                    // Accept IDENTIFIER or DSL keywords as property names
                    val key = when {
                        check(IDENTIFIER) -> advance().lexeme
                        check(FALLOFF) -> advance().lexeme
                        check(ENTRIES) -> advance().lexeme
                        else -> consume(IDENTIFIER, "Expected property name").lexeme
                    }
                    consume(DOUBLE_COLON, "Expected '::'")
                    val value = parseValue()
                    properties[key] = value
                }
            }
        }

        consume(RIGHT_BRACKET, "Expected ']' after ability body")
        return AbilityNode(name, decorators, properties, cast, events)
    }

    private fun parseBehaviorPipeline(): BehaviorPipeline {
        val steps = mutableListOf<BehaviorStep>()
        steps.add(parseBehaviorStep())

        while (match(ARROW)) {
            steps.add(parseBehaviorStep())
        }

        return BehaviorPipeline(steps)
    }

    private fun parseBehaviorStep(): BehaviorStep {
        val action = consume(IDENTIFIER, "Expected action name").lexeme
        consume(RIGHT_ARROW, "Expected '->'")

        // ✅ FIX: Accept both IDENTIFIER and target keywords (SELF, ALLY, ENEMY, ALL)
        val target = when {
            check(IDENTIFIER) -> advance().lexeme
            check(SELF) -> advance().lexeme
            check(ALLY) -> advance().lexeme
            check(ENEMY) -> advance().lexeme
            check(ALL) -> advance().lexeme
            else -> throw error(peek(), "Expected target (SELF, ALLY, ENEMY, ALL, or identifier)")
        }

        val parameters = mutableMapOf<String, Any?>()
        if (match(LEFT_PAREN)) {
            if (!check(RIGHT_PAREN)) {
                do {
                    val key = consume(IDENTIFIER, "Expected parameter name").lexeme
                    consume(DOUBLE_COLON, "Expected '::'")
                    val value = parseValue()
                    parameters[key] = value
                } while (match(COMMA))
            }
            consume(RIGHT_PAREN, "Expected ')'")
        }

        if (match(WITH)) {
            consume(LEFT_BRACKET, "Expected '['")
            parameters.putAll(parsePropertyMap())
            consume(RIGHT_BRACKET, "Expected ']'")
        }

        return BehaviorStep(action, target, parameters)
    }

    private fun parsePropertyMap(): Map<String, Any?> {
        val properties = mutableMapOf<String, Any?>()
        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            // Accept IDENTIFIER or DSL keywords as property names
            val key = when {
                check(IDENTIFIER) -> advance().lexeme
                check(FALLOFF) -> advance().lexeme
                check(ENTRIES) -> advance().lexeme
                check(STATS) -> advance().lexeme
                check(CONFIG) -> advance().lexeme
                check(CALLOUTS) -> advance().lexeme
                else -> consume(IDENTIFIER, "Expected property name").lexeme
            }
            consume(DOUBLE_COLON, "Expected '::'")
            val value = parseValue()
            properties[key] = value
        }
        return properties
    }

    private fun parseValue(): Any? {
        return when {
            match(NUMBER) -> previous().literal as Double
            match(DURATION) -> mapOf("value" to previous().literal, "unit" to "seconds")
            match(PERCENTAGE) -> mapOf("value" to previous().literal, "unit" to "percent")
            match(STRING) -> previous().literal as String
            match(YES, TRUE) -> true
            match(NO, FALSE) -> false
            match(LEFT_BRACKET) -> parseList()
            else -> advance().lexeme
        }
    }

    private fun parseList(): List<Any?> {
        val list = mutableListOf<Any?>()
        if (!check(RIGHT_BRACKET)) {
            do {
                list.add(parseValue())
            } while (match(COMMA))
        }
        consume(RIGHT_BRACKET, "Expected ']'")
        return list
    }

    private fun parseWeapons(): WeaponsSection {
        consume(LEFT_BRACKET, "Expected '['")
        val weapons = mutableListOf<WeaponNode>()
        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            if (match(WEAPON)) {
                weapons.add(parseWeapon())
            }
        }
        consume(RIGHT_BRACKET, "Expected ']'")
        return WeaponsSection(weapons)
    }

    private fun parseWeapon(): WeaponNode {
        val name = consume(IDENTIFIER, "Expected weapon name").lexeme
        consume(LEFT_BRACKET, "Expected '[' after weapon name")

        val decorators = mutableListOf<String>()
        val properties = mutableMapOf<String, Any?>()

        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            when {
                match(AT) -> {
                    val decorator = consume(IDENTIFIER, "Expected decorator name").lexeme
                    consume(DOUBLE_COLON, "Expected '::'")
                    val value = advance().lexeme
                    decorators.add("$decorator=$value")
                }
                else -> {
                    // Accept IDENTIFIER or DSL keywords as property names
                    val key = when {
                        check(IDENTIFIER) -> advance().lexeme
                        check(FALLOFF) -> advance().lexeme
                        check(ENTRIES) -> advance().lexeme
                        else -> consume(IDENTIFIER, "Expected property name").lexeme
                    }

                    // Check if it's a nested block or a simple property
                    if (match(LEFT_BRACKET)) {
                        // Nested block: falloff [ ... ]
                        val nestedProps = parsePropertyMap()
                        consume(RIGHT_BRACKET, "Expected ']' after nested block")
                        properties[key] = nestedProps
                    } else {
                        // Simple property: damage :: 40
                        consume(DOUBLE_COLON, "Expected '::'")
                        val value = parseValue()
                        properties[key] = value
                    }
                }
            }
        }

        consume(RIGHT_BRACKET, "Expected ']' after weapon body")
        return WeaponNode(name, decorators, properties)
    }

    private fun parseMap(): MapSection {
        val name = consume(IDENTIFIER, "Expected map name").lexeme
        consume(LEFT_BRACKET, "Expected '['")

        val teams = mutableListOf<TeamNode>()
        val sites = mutableListOf<SiteNode>()
        val callouts = mutableListOf<String>()

        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            when {
                match(TEAMS) -> {
                    consume(LEFT_BRACKET, "Expected '['")
                    while (!check(RIGHT_BRACKET)) {
                        if (match(TEAM)) {
                            teams.add(parseTeam())
                        }
                    }
                    consume(RIGHT_BRACKET, "Expected ']'")
                }
                match(SITES) -> {
                    consume(LEFT_BRACKET, "Expected '['")
                    while (!check(RIGHT_BRACKET)) {
                        if (match(SITE)) {
                            sites.add(parseSite())
                        }
                    }
                    consume(RIGHT_BRACKET, "Expected ']'")
                }
                match(CALLOUTS) -> {
                    consume(DOUBLE_COLON, "Expected '::'")
                    val list = parseValue()
                    if (list is List<*>) {
                        callouts.addAll(list.map { it.toString() })
                    }
                }
                else -> advance()
            }
        }

        consume(RIGHT_BRACKET, "Expected ']'")
        return MapSection(name, teams, sites, callouts)
    }

    private fun parseTeam(): TeamNode {
        val name = consume(IDENTIFIER, "Expected team name").lexeme
        consume(LEFT_BRACKET, "Expected '['")
        val properties = parsePropertyMap()
        consume(RIGHT_BRACKET, "Expected ']'")
        return TeamNode(name, properties)
    }

    private fun parseSite(): SiteNode {
        val name = consume(IDENTIFIER, "Expected site name").lexeme
        consume(LEFT_BRACKET, "Expected '['")
        val properties = parsePropertyMap()
        consume(RIGHT_BRACKET, "Expected ']'")
        return SiteNode(name, properties)
    }

    private fun parseEconomy(): EconomySection {
        consume(LEFT_BRACKET, "Expected '['")
        val properties = parsePropertyMap()
        consume(RIGHT_BRACKET, "Expected ']'")
        return EconomySection(properties)
    }

    private fun parseStatusEffects(): StatusEffectsSection {
        consume(LEFT_BRACKET, "Expected '['")
        val effects = mutableListOf<StatusEffectNode>()
        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            if (match(EFFECT)) {
                effects.add(parseStatusEffect())
            }
        }
        consume(RIGHT_BRACKET, "Expected ']'")
        return StatusEffectsSection(effects)
    }

    private fun parseStatusEffect(): StatusEffectNode {
        val name = consume(IDENTIFIER, "Expected effect name").lexeme
        consume(LEFT_BRACKET, "Expected '['")

        val decorators = mutableListOf<String>()
        val properties = mutableMapOf<String, Any?>()
        val events = mutableMapOf<String, BehaviorPipeline>()

        while (!check(RIGHT_BRACKET) && !isAtEnd()) {
            when {
                match(AT) -> {
                    val decorator = advance().lexeme
                    decorators.add(decorator)
                }
                match(ON_APPLY, ON_TICK, ON_EXPIRE) -> {
                    val eventType = previous().lexeme
                    consume(ARROW, "Expected '=>'")
                    events[eventType] = parseBehaviorPipeline()
                }
                else -> {
                    val key = consume(IDENTIFIER, "Expected property name").lexeme
                    consume(DOUBLE_COLON, "Expected '::'")
                    val value = parseValue()
                    properties[key] = value
                }
            }
        }

        consume(RIGHT_BRACKET, "Expected ']'")
        return StatusEffectNode(name, decorators, properties, events)
    }

    private fun parseMatch(): MatchSection {
        consume(LEFT_BRACKET, "Expected '['")
        val properties = parsePropertyMap()
        consume(RIGHT_BRACKET, "Expected ']'")
        return MatchSection(properties)
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType) = if (isAtEnd()) false else peek().type == type
    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }
    private fun isAtEnd() = peek().type == EOF
    private fun peek() = tokens[current]
    private fun previous() = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): RuntimeException {
        println("[Line ${token.line}] Error at '${token.lexeme}': $message")
        return RuntimeException(message)
    }
}