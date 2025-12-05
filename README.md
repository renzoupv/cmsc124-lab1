# TacShooter Configuration Language (TSCL)

## Creators

Renz de Arroz & Johann Ross Yap

## Language Overview

**TacShooter DSL** is a specialized domain-specific language designed to let game designers and non-programmers configure the core elements of a 5v5 tactical shooter.

Unlike general-purpose scripting languages, TSCL is **declarative** and **data-driven**. It allows users to define complex game entities—such as Agents, Weapons, Maps, and Status Effects—using a readable syntax that compiles directly into JSON for use in game engines.

**Category:** Declarative, Interpreted, Configuration
**Typing:** Static, Strong (Domain-Specific)
**Data Types:**

| Primitive Types | DSL-Specific Types |
| :--- | :--- |
| 1. Number (Float/Int) | 1. Agent |
| 2. Boolean (yes/no) | 2. Weapon |
| 3. String | 3. Ability |
| 4. Duration (e.g., `5s`) | 4. Team |
| 5. Percentage (e.g., `10%`) | 5. Site |
| 6. List (`[...]`) | 6. Map |
| | 7. Status Effect |

## Keywords

The language reserves the following words to define structure and behavior:

### Structure & Entities
| Keyword | Description |
| :--- | :--- |
| `GAME` | The root block for the entire game configuration. |
| `AGENTS`, `AGENT` | Sections for defining playable characters. |
| `WEAPONS`, `WEAPON` | Sections for defining guns and equipment. |
| `MAP`, `SITES`, `SITE` | Definitions for the level layout and bomb sites. |
| `TEAMS`, `TEAM` | Definitions for faction attributes (e.g., Attackers/Defenders). |
| `STATUS_EFFECTS`, `EFFECT` | Definitions for buffs and debuffs (e.g., Flash, Stun). |
| `MATCH`, `ECONOMY` | Global game rules and economy settings. |

### Properties & Logic
| Keyword | Description |
| :--- | :--- |
| `STATS` | Block for base attributes (health, speed). |
| `ABILITIES`, `ABILITY` | Defines a character's kit. |
| `CAST` | Hook that triggers when an ability is used. |
| `ON_KILL`, `ON_APPLY` | Event hooks for combat interactions. |
| `ON_TICK`, `ON_EXPIRE` | Event hooks for time-based effects. |
| `FALLOFF` | Block for defining weapon damage over distance. |
| `CALLOUTS` | List of map location names. |

### Values & Targets
| Keyword | Description |
| :--- | :--- |
| `yes`, `no` | Boolean literals (True/False). |
| `ENEMY`, `ALLY` | Dynamic targets relative to the caster. |
| `SELF`, `ALL` | Specific target designators. |
| `AOE`, `SINGLE_TARGET` | Targeting types. |
| `OFFENSIVE`, `DEFENSIVE` | Ability archetypes. |
| `BUFF`, `DEBUFF` | Status effect types. |

## Operators

| Operator | Description |
| :--- | :--- |
| `::` | **Property Assignment**. Assigns a value to a key (e.g., `damage :: 50`). |
| `=>` | **Event Pipeline**. Maps a trigger to a sequence of actions (e.g., `CAST => throw`). |
| `->` | **Targeting**. Directs an action toward a specific entity (e.g., `damage -> ENEMY`). |
| `[` `]` | **Scope/Grouping**. Encloses sections, lists, and object definitions. |
| `@` | **Decorator**. Tags an entity with metadata (e.g., `@Duelist`). |
| `with` | **Parameter Injection**. Passes arguments to an action. |

## Literals

| Literal | Description |
| :--- | :--- |
| **Numbers** | Standard integers or floating-point values (e.g., `150`, `4.5`). |
| **Strings** | Text enclosed in double quotes (e.g., `"Classic Rifle"`). |
| **Booleans** | Represented by `yes` and `no` for readability. |
| **Durations** | Numbers suffixed with `s`. Parsed as `{ val: X, unit: "seconds" }`.<br>Example: `5s`, `0.5s`. |
| **Percentages** | Numbers suffixed with `%`. Parsed as `{ val: X, unit: "percent" }`.<br>Example: `50%`, `100%`. |
| **Arrays** | A comma-separated list of values enclosed in brackets.<br>Example: `[ "A", "B", "C" ]`. |

## Identifiers

* **Format:** Can contain letters (`a-z`, `A-Z`) and underscores `_`.
* **Rules:** Must not start with a digit. Case-sensitive.
* **Usage:** Used for naming Agents, Weapons, Map Sites, and custom properties.

## Comments

* **Hash Style:** Comments start with `#` and continue to the end of the line.
    * Example: `# This is a comment`
* **Block Comments:** Not supported (simple syntax preferred).

## Syntax Style

* **Blocks:** The language uses square brackets `[` and `]` to define hierarchy, distinguishing it from C-style languages.
* **Assignments:** Uses double-colon `::` to clearly separate keys from values.
* **Whitespace:** Not significant. Formatting is flexible.

## Sample Code

~~~

GAME ValorStrike [
    # Global Config
    CONFIG [ tick_rate :: 128 rounds :: 13 ]

    # Entity Definitions
    AGENTS [
        AGENT Jett [
            @Duelist
            STATS [ health :: 100 speed :: 6.5 ]
            
            ABILITIES [
                ABILITY Updraft [
                    type :: MOBILITY
                    cooldown :: 12s
                    charges :: 2
                    
                    # Behavior Pipeline
                    CAST => jump -> SELF ( height :: 500 )
                ]
                ABILITY Cloudburst [
                    type :: UTILITY
                    duration :: 4.5s
                    
                    CAST => spawn_smoke -> SELF [ radius :: 300 ]
                ]
            ]
        ]
    ]

    WEAPONS [
        WEAPON Vandal [
            cost :: 2900
            penetration :: high
            
            # Nested Property Block
            STATS [ 
                magazine :: 25 
                fire_rate :: 9.75 
            ]
            
            # Damage Falloff Logic
            FALLOFF [
                range_0_30  [ head :: 160 body :: 40 legs :: 34 ]
                range_30_50 [ head :: 160 body :: 40 legs :: 34 ]
            ]
        ]
    ]
]
~~~

#Design Rationale

TSCL is built to be a safe, designer-first configuration language that prioritizes readability and ease of use over complex programming logic. By supporting domain-specific literals (like 5s for time and 10% for rates) and compiling directly to JSON, it eliminates common data entry errors and ensures that game content remains portable and engine-agnostic. This declarative approach allows non-programmers to define complex behaviors through simple pipelines without the risk of breaking core game code.
