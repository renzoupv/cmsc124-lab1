// DSL-specific AST nodes
sealed class ASTNode

// Root
data class GameNode(
    val name: String,
    val sections: List<SectionNode>
) : ASTNode()

// Sections
sealed class SectionNode : ASTNode()

data class ConfigSection(val properties: Map<String, Any?>) : SectionNode()
data class AgentsSection(val agents: List<AgentNode>) : SectionNode()
data class WeaponsSection(val weapons: List<WeaponNode>) : SectionNode()
data class MapSection(
    val name: String,
    val teams: List<TeamNode>,
    val sites: List<SiteNode>,
    val callouts: List<String>
) : SectionNode()
data class EconomySection(val properties: Map<String, Any?>) : SectionNode()
data class StatusEffectsSection(val effects: List<StatusEffectNode>) : SectionNode()
data class MatchSection(val properties: Map<String, Any?>) : SectionNode()

// Entities
data class AgentNode(
    val name: String,
    val decorators: List<String>,
    val stats: Map<String, Any?>,
    val abilities: List<AbilityNode>
) : ASTNode()

data class AbilityNode(
    val name: String,
    val decorators: List<String>,
    val properties: Map<String, Any?>,
    val cast: BehaviorPipeline?,
    val events: Map<String, BehaviorPipeline>
) : ASTNode()

data class WeaponNode(
    val name: String,
    val decorators: List<String>,
    val properties: Map<String, Any?>
) : ASTNode()

data class TeamNode(
    val name: String,
    val properties: Map<String, Any?>
) : ASTNode()

data class SiteNode(
    val name: String,
    val properties: Map<String, Any?>
) : ASTNode()

data class StatusEffectNode(
    val name: String,
    val decorators: List<String>,
    val properties: Map<String, Any?>,
    val events: Map<String, BehaviorPipeline>
) : ASTNode()

// Behavior
data class BehaviorPipeline(
    val steps: List<BehaviorStep>
) : ASTNode()

data class BehaviorStep(
    val action: String,
    val target: String,
    val parameters: Map<String, Any?>
) : ASTNode()
