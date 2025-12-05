class ConfigInterpreter {
    private val gameConfig = mutableMapOf<String, Any?>()

    fun interpret(ast: GameNode): Map<String, Any?> {
        gameConfig.clear()
        gameConfig["game_name"] = ast.name

        for (section in ast.sections) {
            when (section) {
                is ConfigSection -> interpretConfig(section)
                is AgentsSection -> interpretAgents(section)
                is WeaponsSection -> interpretWeapons(section)
                is MapSection -> interpretMap(section)
                is EconomySection -> interpretEconomy(section)
                is StatusEffectsSection -> interpretStatusEffects(section)
                is MatchSection -> interpretMatch(section)
            }
        }

        return gameConfig
    }

    private fun interpretConfig(section: ConfigSection) {
        gameConfig["config"] = section.properties
    }

    private fun interpretAgents(section: AgentsSection) {
        val agents = mutableListOf<Map<String, Any?>>()
        for (agent in section.agents) {
            val agentData = mutableMapOf<String, Any?>()
            agentData["name"] = agent.name
            agentData["decorators"] = agent.decorators
            agentData["stats"] = agent.stats

            val abilities = mutableListOf<Map<String, Any?>>()
            for (ability in agent.abilities) {
                abilities.add(interpretAbility(ability))
            }
            agentData["abilities"] = abilities
            agents.add(agentData)
        }
        gameConfig["agents"] = agents
    }

    private fun interpretAbility(ability: AbilityNode): Map<String, Any?> {
        val abilityData = mutableMapOf<String, Any?>()
        abilityData["name"] = ability.name
        abilityData["decorators"] = ability.decorators
        abilityData["properties"] = ability.properties

        if (ability.cast != null) {
            abilityData["cast"] = interpretPipeline(ability.cast)
        }

        if (ability.events.isNotEmpty()) {
            val events = mutableMapOf<String, Any?>()
            for ((eventName, pipeline) in ability.events) {
                events[eventName] = interpretPipeline(pipeline)
            }
            abilityData["events"] = events
        }

        return abilityData
    }

    private fun interpretPipeline(pipeline: BehaviorPipeline): List<Map<String, Any?>> {
        val steps = mutableListOf<Map<String, Any?>>()
        for (step in pipeline.steps) {
            steps.add(mapOf(
                "action" to step.action,
                "target" to step.target,
                "parameters" to step.parameters
            ))
        }
        return steps
    }

    private fun interpretWeapons(section: WeaponsSection) {
        val weapons = mutableListOf<Map<String, Any?>>()
        for (weapon in section.weapons) {
            weapons.add(mapOf(
                "name" to weapon.name,
                "decorators" to weapon.decorators,
                "properties" to weapon.properties
            ))
        }
        gameConfig["weapons"] = weapons
    }

    private fun interpretMap(section: MapSection) {
        val mapData = mutableMapOf<String, Any?>()
        mapData["name"] = section.name

        val teams = mutableListOf<Map<String, Any?>>()
        for (team in section.teams) {
            teams.add(mapOf(
                "name" to team.name,
                "properties" to team.properties
            ))
        }
        mapData["teams"] = teams

        val sites = mutableListOf<Map<String, Any?>>()
        for (site in section.sites) {
            sites.add(mapOf(
                "name" to site.name,
                "properties" to site.properties
            ))
        }
        mapData["sites"] = sites
        mapData["callouts"] = section.callouts
        gameConfig["map"] = mapData
    }

    private fun interpretEconomy(section: EconomySection) {
        gameConfig["economy"] = section.properties
    }

    private fun interpretStatusEffects(section: StatusEffectsSection) {
        val effects = mutableListOf<Map<String, Any?>>()
        for (effect in section.effects) {
            val effectData = mutableMapOf<String, Any?>()
            effectData["name"] = effect.name
            effectData["decorators"] = effect.decorators
            effectData["properties"] = effect.properties

            if (effect.events.isNotEmpty()) {
                val events = mutableMapOf<String, Any?>()
                for ((eventName, pipeline) in effect.events) {
                    events[eventName] = interpretPipeline(pipeline)
                }
                effectData["events"] = events
            }

            effects.add(effectData)
        }
        gameConfig["status_effects"] = effects
    }

    private fun interpretMatch(section: MatchSection) {
        gameConfig["match"] = section.properties
    }
}
