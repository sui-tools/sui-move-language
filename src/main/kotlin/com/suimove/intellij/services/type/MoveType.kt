package com.suimove.intellij.services.type

/**
 * Base interface for all Move types.
 */
sealed interface MoveType {
    fun displayName(): String
}

/**
 * Built-in primitive types in Move.
 */
enum class MoveBuiltinType(
    private val typeName: String,
    val abilities: Set<MoveAbility>
) : MoveType {
    BOOL("bool", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    U8("u8", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    U16("u16", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    U32("u32", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    U64("u64", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    U128("u128", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    U256("u256", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    ADDRESS("address", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    SIGNER("signer", setOf(MoveAbility.DROP)),
    UNIT("()", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE)),
    STRING("string", setOf(MoveAbility.COPY, MoveAbility.DROP, MoveAbility.STORE));
    
    override fun displayName(): String = typeName
}

/**
 * Marker interface for numeric types.
 */
interface MoveNumericType : MoveType

/**
 * Marker interface for integer types.
 */
interface MoveIntegerType : MoveNumericType

/**
 * Extension to make built-in types implement numeric interfaces.
 */
val MoveBuiltinType.isNumeric: Boolean
    get() = this in setOf(MoveBuiltinType.U8, MoveBuiltinType.U16, MoveBuiltinType.U32, MoveBuiltinType.U64, MoveBuiltinType.U128, MoveBuiltinType.U256)

val MoveBuiltinType.isInteger: Boolean
    get() = this in setOf(MoveBuiltinType.U8, MoveBuiltinType.U16, MoveBuiltinType.U32, MoveBuiltinType.U64, MoveBuiltinType.U128, MoveBuiltinType.U256)

/**
 * Named type (struct or type alias).
 */
data class MoveNamedType(
    val name: String,
    val moduleAddress: String? = null,
    val moduleName: String? = null
) : MoveType {
    override fun displayName(): String {
        return if (moduleAddress != null && moduleName != null) {
            "$moduleAddress::$moduleName::$name"
        } else {
            name
        }
    }
    
    val qualifiedName: String
        get() = displayName()
}

/**
 * Generic type with type arguments.
 */
data class MoveGenericType(
    val baseName: String,
    val typeArguments: List<MoveType>
) : MoveType {
    override fun displayName(): String {
        val args = typeArguments.joinToString(", ") { it.displayName() }
        return "$baseName<$args>"
    }
}

/**
 * Reference type (&T or &mut T).
 */
data class MoveReferenceType(
    val innerType: MoveType,
    val isMutable: Boolean
) : MoveType {
    override fun displayName(): String {
        val prefix = if (isMutable) "&mut " else "&"
        return "$prefix${innerType.displayName()}"
    }
}

/**
 * Tuple type.
 */
data class MoveTupleType(
    val types: List<MoveType>
) : MoveType {
    override fun displayName(): String {
        return if (types.isEmpty()) {
            "()"
        } else {
            "(${types.joinToString(", ") { it.displayName() }})"
        }
    }
}

/**
 * Type variable (generic parameter).
 */
data class MoveTypeVariable(
    val name: String,
    val constraints: Set<MoveAbility> = emptySet()
) : MoveType {
    override fun displayName(): String = name
}

/**
 * Unknown type (used when type inference fails).
 */
object MoveUnknownType : MoveType {
    override fun displayName(): String = "<unknown>"
}

/**
 * Function type (not first-class in Move, but useful for type checking).
 */
data class MoveFunctionType(
    val parameterTypes: List<MoveType>,
    val returnType: MoveType
) : MoveType {
    override fun displayName(): String {
        val params = parameterTypes.joinToString(", ") { it.displayName() }
        return "($params) -> ${returnType.displayName()}"
    }
}

/**
 * Abilities that types can have in Move.
 */
enum class MoveAbility {
    COPY,
    DROP,
    STORE,
    KEY
}

/**
 * Type parameter with ability constraints.
 */
data class MoveTypeParameter(
    val name: String,
    val constraints: Set<MoveAbility> = emptySet()
) {
    fun displayName(): String {
        return if (constraints.isEmpty()) {
            name
        } else {
            "$name: ${constraints.joinToString(" + ")}"
        }
    }
}

/**
 * Extension functions for type checking.
 */
fun MoveType.isAssignableTo(other: MoveType): Boolean {
    return when {
        this == other -> true
        this is MoveUnknownType || other is MoveUnknownType -> true
        this is MoveReferenceType && other is MoveReferenceType -> {
            // Mutable references are not assignable to immutable references
            (!this.isMutable || other.isMutable) && 
            this.innerType.isAssignableTo(other.innerType)
        }
        else -> false
    }
}

fun MoveType.hasAbility(ability: MoveAbility): Boolean {
    return when (this) {
        is MoveBuiltinType -> ability in this.abilities
        is MoveReferenceType -> {
            // References only have copy and drop
            ability in setOf(MoveAbility.COPY, MoveAbility.DROP)
        }
        is MoveTupleType -> {
            // Tuple has ability if all components have it
            types.all { it.hasAbility(ability) }
        }
        // TODO: Handle other types by looking up their definitions
        else -> false
    }
}

/**
 * Check if a type is a numeric type that supports arithmetic operations.
 */
fun MoveType.isNumericType(): Boolean {
    return this is MoveBuiltinType && this.isNumeric
}

/**
 * Check if a type is an integer type that supports bitwise operations.
 */
fun MoveType.isIntegerType(): Boolean {
    return this is MoveBuiltinType && this.isInteger
}
