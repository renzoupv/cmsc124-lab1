class CodeGenerator {
    private var indentLevel = 0
    private val indent get() = "  ".repeat(indentLevel)

    fun generate(ast: GameNode): String {
        val gameConfig = mutableMapOf<String, Any?>()
        gameConfig["game_name"] = ast.name

        for (section in ast.sections) {
            when (section) {
                is ConfigSection -> gameConfig["config"] = section.properties
                is AgentsSection -> gameConfig["agents"] = section.agents.map { agentToJson(it) }
                is WeaponsSection -> gameConfig["weapons"] = section.weapons.map { weaponToJson(it) }
                is MapSection -> gameConfig["map"] = mapToJson(section)
                is EconomySection -> gameConfig["economy"] = section.properties
                is StatusEffectsSection -> gameConfig["status_effects"] = section.effects.map { effectToJson(it) }
                is MatchSection -> gameConfig["match"] = section.properties
            }
        }

        return toJsonString(gameConfig)
    }

    private fun agentToJson(agent: AgentNode): Map<String, Any?> {
        return mapOf(
            "name" to agent.name,
            "decorators" to agent.decorators,
            "stats" to agent.stats,
            "abilities" to agent.abilities.map { abilityToJson(it) }
        )
    }

    private fun abilityToJson(ability: AbilityNode): Map<String, Any?> {
        val json = mutableMapOf<String, Any?>()
        json["name"] = ability.name
        json["decorators"] = ability.decorators
        json["properties"] = ability.properties

        if (ability.cast != null) {
            json["cast"] = pipelineToJson(ability.cast)
        }

        if (ability.events.isNotEmpty()) {
            json["events"] = ability.events.mapValues { pipelineToJson(it.value) }
        }

        return json
    }

    private fun pipelineToJson(pipeline: BehaviorPipeline): List<Map<String, Any?>> {
        return pipeline.steps.map {
            mapOf(
                "action" to it.action,
                "target" to it.target,
                "parameters" to it.parameters
            )
        }
    }

    private fun weaponToJson(weapon: WeaponNode): Map<String, Any?> {
        return mapOf(
            "name" to weapon.name,
            "decorators" to weapon.decorators,
            "properties" to weapon.properties
        )
    }

    private fun mapToJson(map: MapSection): Map<String, Any?> {
        return mapOf(
            "name" to map.name,
            "teams" to map.teams.map { teamToJson(it) },
            "sites" to map.sites.map { siteToJson(it) },
            "callouts" to map.callouts
        )
    }

    private fun teamToJson(team: TeamNode): Map<String, Any?> {
        return mapOf(
            "name" to team.name,
            "properties" to team.properties
        )
    }

    private fun siteToJson(site: SiteNode): Map<String, Any?> {
        return mapOf(
            "name" to site.name,
            "properties" to site.properties
        )
    }

    private fun effectToJson(effect: StatusEffectNode): Map<String, Any?> {
        val json = mutableMapOf<String, Any?>()
        json["name"] = effect.name
        json["decorators"] = effect.decorators
        json["properties"] = effect.properties

        if (effect.events.isNotEmpty()) {
            json["events"] = effect.events.mapValues { pipelineToJson(it.value) }
        }

        return json
    }

    // Simple JSON serializer (no external dependencies)
    private fun toJsonString(obj: Any?, indent: Int = 0): String {
        val ind = "  ".repeat(indent)
        val indNext = "  ".repeat(indent + 1)

        return when (obj) {
            null -> "null"
            is String -> "\"${escapeJson(obj)}\""
            is Number -> obj.toString()
            is Boolean -> obj.toString()
            is Map<*, *> -> {
                if (obj.isEmpty()) "{}"
                else {
                    val entries = obj.entries.joinToString(",\n") { (k, v) ->
                        "$indNext\"$k\": ${toJsonString(v, indent + 1)}"
                    }
                    "{\n$entries\n$ind}"
                }
            }
            is List<*> -> {
                if (obj.isEmpty()) "[]"
                else {
                    val items = obj.joinToString(",\n") {
                        "$indNext${toJsonString(it, indent + 1)}"
                    }
                    "[\n$items\n$ind]"
                }
            }
            else -> "\"$obj\""
        }
    }

    private fun escapeJson(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}