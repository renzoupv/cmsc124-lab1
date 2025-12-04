class ConfigStore {
    private val globalConfigs = mutableMapOf<String, Any?>()
    private val agentRegistry = mutableMapOf<String, AgentNode>()
    private val weaponRegistry = mutableMapOf<String, WeaponNode>()
    private val effectRegistry = mutableMapOf<String, StatusEffectNode>()

    fun registerAgent(agent: AgentNode) {
        agentRegistry[agent.name] = agent
    }

    fun getAgent(name: String): AgentNode? {
        return agentRegistry[name]
    }

    fun registerWeapon(weapon: WeaponNode) {
        weaponRegistry[weapon.name] = weapon
    }

    fun getWeapon(name: String): WeaponNode? {
        return weaponRegistry[name]
    }

    fun registerEffect(effect: StatusEffectNode) {
        effectRegistry[effect.name] = effect
    }

    fun getEffect(name: String): StatusEffectNode? {
        return effectRegistry[name]
    }

    fun setGlobalConfig(key: String, value: Any?) {
        globalConfigs[key] = value
    }

    fun getGlobalConfig(key: String): Any? {
        return globalConfigs[key]
    }

    fun getAllAgents(): List<AgentNode> = agentRegistry.values.toList()
    fun getAllWeapons(): List<WeaponNode> = weaponRegistry.values.toList()
    fun getAllEffects(): List<StatusEffectNode> = effectRegistry.values.toList()
}
