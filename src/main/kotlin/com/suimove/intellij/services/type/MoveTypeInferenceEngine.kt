package com.suimove.intellij.services.type

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.sui.SuiFrameworkLibrary
import java.util.concurrent.ConcurrentHashMap

/**
 * Type inference engine for Move language.
 * Handles type resolution, generic type parameters, and ability constraints.
 */
@Service(Service.Level.PROJECT)
class MoveTypeInferenceEngine(private val project: Project) {
    
    private val typeCache = MoveTypeCache.getInstance(project)
    
    /**
     * Infers the type of a given PSI element.
     */
    fun inferType(element: PsiElement): MoveType? {
        // Check cache first
        typeCache.getCachedType(element)?.let { return it }
        
        val inferredType = when (element) {
            is MoveExpression -> inferExpressionType(element)
            is MoveNamedElement -> inferNamedElementType(element)
            is MoveTypeElement -> resolveTypeElement(element)
            is MoveStructField -> inferStructFieldType(element)
            is MoveFunctionParameter -> inferParameterType(element)
            else -> null
        }
        
        // Cache the result
        inferredType?.let { typeCache.cacheType(element, it) }
        
        return inferredType
    }
    
    /**
     * Resolves type variables in a generic context.
     */
    fun resolveTypeVariables(
        type: MoveType,
        typeArguments: List<MoveType>,
        typeParameters: List<MoveTypeParameter>
    ): MoveType {
        if (typeArguments.isEmpty() || typeParameters.isEmpty()) {
            return type
        }
        
        val substitutionMap = typeParameters.zip(typeArguments).toMap()
        return substituteTypeVariables(type, substitutionMap)
    }
    
    /**
     * Checks if a type satisfies the given ability constraints.
     */
    fun checkAbilityConstraints(type: MoveType, requiredAbilities: Set<MoveAbility>): Boolean {
        val typeAbilities = getTypeAbilities(type)
        return requiredAbilities.all { it in typeAbilities }
    }
    
    /**
     * Performs type unification for type inference.
     */
    fun unifyTypes(type1: MoveType, type2: MoveType): MoveType? {
        return when {
            type1 == type2 -> type1
            type1 is MoveTypeVariable -> type2
            type2 is MoveTypeVariable -> type1
            type1 is MoveGenericType && type2 is MoveGenericType -> {
                if (type1.baseName == type2.baseName) {
                    val unifiedArgs = type1.typeArguments.zip(type2.typeArguments)
                        .mapNotNull { (arg1, arg2) -> unifyTypes(arg1, arg2) }
                    
                    if (unifiedArgs.size == type1.typeArguments.size) {
                        MoveGenericType(type1.baseName, unifiedArgs)
                    } else null
                } else null
            }
            else -> null
        }
    }
    
    private fun inferExpressionType(expr: MoveExpression): MoveType? {
        return when (expr) {
            is MoveLiteralExpression -> inferLiteralType(expr)
            is MoveReferenceExpression -> inferReferenceType(expr)
            is MoveCallExpression -> inferCallExpressionType(expr)
            is MoveBinaryExpression -> inferBinaryExpressionType(expr)
            is MoveUnaryExpression -> inferUnaryExpressionType(expr)
            is MoveStructLiteralExpression -> inferStructLiteralType(expr)
            is MoveVectorExpression -> inferVectorType(expr)
            is MoveIfExpression -> inferIfExpressionType(expr)
            is MoveBlockExpression -> inferBlockType(expr)
            else -> null
        }
    }
    
    private fun inferLiteralType(literal: MoveLiteralExpression): MoveType {
        return when {
            literal.text.startsWith("@") -> MoveBuiltinType.ADDRESS
            literal.text.startsWith("0x") -> MoveBuiltinType.ADDRESS
            literal.text.toBooleanStrictOrNull() != null -> MoveBuiltinType.BOOL
            literal.text.toIntOrNull() != null -> {
                when {
                    literal.text.endsWith("u8") -> MoveBuiltinType.U8
                    literal.text.endsWith("u16") -> MoveBuiltinType.U16
                    literal.text.endsWith("u32") -> MoveBuiltinType.U32
                    literal.text.endsWith("u64") -> MoveBuiltinType.U64
                    literal.text.endsWith("u128") -> MoveBuiltinType.U128
                    literal.text.endsWith("u256") -> MoveBuiltinType.U256
                    else -> MoveBuiltinType.U64 // Default integer type
                }
            }
            literal.text.startsWith("\"") -> MoveBuiltinType.STRING
            literal.text.startsWith("b\"") -> MoveGenericType("vector", listOf(MoveBuiltinType.U8))
            else -> MoveUnknownType
        }
    }
    
    private fun inferReferenceType(ref: MoveReferenceExpression): MoveType? {
        val resolved = ref.reference?.resolve() ?: return null
        return inferType(resolved)
    }
    
    private fun inferCallExpressionType(call: MoveCallExpression): MoveType? {
        // Try to resolve the function being called
        val functionName = call.functionName ?: return null
        val function = resolveFunctionByName(call, functionName) ?: return null
        val returnType = function.returnType ?: return MoveBuiltinType.UNIT
        
        // Handle generic function calls
        val typeArguments = emptyList<MoveTypeElement>() // TODO: Extract type arguments from call
        val typeParameters = function.typeParameters
        
        return if (typeArguments.isNotEmpty() && typeParameters.isNotEmpty()) {
            resolveTypeVariables(returnType, typeArguments.map { resolveTypeElement(it) ?: MoveUnknownType }, typeParameters)
        } else {
            returnType
        }
    }
    
    private fun resolveFunctionByName(context: PsiElement, name: String): MoveFunction? {
        // TODO: Implement proper function resolution
        return null
    }
    
    private fun inferBinaryExpressionType(expr: MoveBinaryExpression): MoveType? {
        val leftType = inferType(expr.left) ?: return null
        val rightType = inferType(expr.right) ?: return null
        
        return when (expr.operator) {
            "+", "-", "*", "/", "%" -> {
                // Arithmetic operators - result is same as operand type
                if (leftType == rightType && leftType is MoveNumericType) leftType else null
            }
            "==", "!=", "<", ">", "<=", ">=" -> MoveBuiltinType.BOOL
            "&&", "||" -> MoveBuiltinType.BOOL
            "&", "|", "^" -> {
                // Bitwise operators
                if (leftType == rightType && leftType is MoveIntegerType) leftType else null
            }
            else -> null
        }
    }
    
    private fun inferUnaryExpressionType(expr: MoveUnaryExpression): MoveType? {
        val operandType = inferType(expr.operand) ?: return null
        
        return when (expr.operator) {
            "!" -> if (operandType == MoveBuiltinType.BOOL) MoveBuiltinType.BOOL else null
            "-" -> if (operandType is MoveNumericType) operandType else null
            "*" -> {
                // Dereference
                if (operandType is MoveReferenceType) operandType.innerType else null
            }
            "&", "&mut" -> MoveReferenceType(operandType, expr.operator == "&mut")
            else -> null
        }
    }
    
    private fun inferStructLiteralType(expr: MoveStructLiteralExpression): MoveType? {
        // Try to resolve the struct type from the literal
        return expr.structType
    }
    
    private fun inferVectorType(expr: MoveVectorExpression): MoveType? {
        val elements = expr.elements
        if (elements.isEmpty()) {
            // Empty vector, need explicit type annotation
            return expr.typeAnnotation?.let { resolveTypeElement(it) }
        }
        
        // Infer from first element
        val elementType = inferType(elements.first()) ?: return null
        
        // Verify all elements have same type
        for (element in elements.drop(1)) {
            val elemType = inferType(element) ?: return null
            if (unifyTypes(elementType, elemType) == null) {
                return null // Type mismatch
            }
        }
        
        return MoveGenericType("vector", listOf(elementType))
    }
    
    private fun inferIfExpressionType(expr: MoveIfExpression): MoveType? {
        val thenType = inferType(expr.thenBranch) ?: return null
        val elseType = expr.elseBranch?.let { inferType(it) } ?: MoveBuiltinType.UNIT
        
        return unifyTypes(thenType, elseType)
    }
    
    private fun inferBlockType(block: MoveBlockExpression): MoveType? {
        val statements = block.statements
        if (statements.isEmpty()) {
            return MoveBuiltinType.UNIT
        }
        
        val lastStatement = statements.last()
        return if (lastStatement is MoveExpressionStatement && !lastStatement.hasSemicolon) {
            inferType(lastStatement.expression)
        } else {
            MoveBuiltinType.UNIT
        }
    }
    
    private fun inferNamedElementType(element: MoveNamedElement): MoveType? {
        return when (element) {
            is MoveFunction -> element.returnType ?: MoveBuiltinType.UNIT
            is MoveStruct -> MoveNamedType(element.qualifiedName ?: element.name ?: return null)
            is MoveConstant -> element.type
            is MoveVariable -> element.type ?: inferVariableTypeFromContext(element)
            else -> null
        }
    }
    
    private fun inferVariableTypeFromContext(variable: MoveVariable): MoveType? {
        // Try to infer from initialization expression
        val initExpr = variable.initializationExpression
        if (initExpr != null) {
            return inferType(initExpr)
        }
        
        // Try to infer from usage context
        // This would require data flow analysis
        return null
    }
    
    private fun resolveTypeElement(typeElement: MoveTypeElement): MoveType? {
        return when (typeElement) {
            is MoveBuiltinTypeElement -> resolveBuiltinType(typeElement)
            is MoveNamedTypeElement -> resolveNamedType(typeElement)
            is MoveGenericTypeElement -> resolveGenericType(typeElement)
            is MoveReferenceTypeElement -> resolveReferenceType(typeElement)
            is MoveTupleTypeElement -> resolveTupleType(typeElement)
            else -> null
        }
    }
    
    private fun resolveBuiltinType(element: MoveBuiltinTypeElement): MoveBuiltinType {
        return when (element.text) {
            "bool" -> MoveBuiltinType.BOOL
            "u8" -> MoveBuiltinType.U8
            "u16" -> MoveBuiltinType.U16
            "u32" -> MoveBuiltinType.U32
            "u64" -> MoveBuiltinType.U64
            "u128" -> MoveBuiltinType.U128
            "u256" -> MoveBuiltinType.U256
            "address" -> MoveBuiltinType.ADDRESS
            "signer" -> MoveBuiltinType.SIGNER
            else -> MoveBuiltinType.UNIT
        }
    }
    
    private fun resolveNamedType(element: MoveNamedTypeElement): MoveType? {
        val resolved = element.reference?.resolve()
        return when (resolved) {
            is MoveStruct -> MoveNamedType(resolved.qualifiedName ?: resolved.name ?: return null)
            is PsiElement -> {
                if (resolved.javaClass.simpleName == "MoveTypeParameterImpl" || 
                    resolved.javaClass.interfaces.any { it.simpleName == "MoveTypeParameter" }) {
                    MoveTypeVariable((resolved as? com.suimove.intellij.psi.MoveNamedElement)?.name ?: return null)
                } else {
                    null
                }
            }
            else -> null
        }
    }
    
    private fun resolveGenericType(element: MoveGenericTypeElement): MoveType? {
        val baseName = element.baseName ?: return null
        val typeArguments = element.typeArguments.mapNotNull { resolveTypeElement(it) }
        
        return MoveGenericType(baseName, typeArguments)
    }
    
    private fun resolveReferenceType(element: MoveReferenceTypeElement): MoveType? {
        val innerType = resolveTypeElement(element.innerType) ?: return null
        return MoveReferenceType(innerType, element.isMutable)
    }
    
    private fun resolveTupleType(element: MoveTupleTypeElement): MoveType {
        val types = element.types.mapNotNull { resolveTypeElement(it) }
        return MoveTupleType(types)
    }
    
    private fun inferStructFieldType(field: MoveStructField): MoveType? {
        return field.type?.let { resolveTypeElement(it) }
    }
    
    private fun inferParameterType(param: MoveFunctionParameter): MoveType? {
        return param.type?.let { resolveTypeElement(it) }
    }
    
    private fun substituteTypeVariables(
        type: MoveType,
        substitutionMap: Map<MoveTypeParameter, MoveType>
    ): MoveType {
        return when (type) {
            is MoveTypeVariable -> {
                substitutionMap.entries.find { it.key.name == type.name }?.value ?: type
            }
            is MoveGenericType -> {
                val substitutedArgs = type.typeArguments.map { 
                    substituteTypeVariables(it, substitutionMap) 
                }
                MoveGenericType(type.baseName, substitutedArgs)
            }
            is MoveReferenceType -> {
                MoveReferenceType(
                    substituteTypeVariables(type.innerType, substitutionMap),
                    type.isMutable
                )
            }
            is MoveTupleType -> {
                MoveTupleType(type.types.map { substituteTypeVariables(it, substitutionMap) })
            }
            else -> type
        }
    }
    
    private fun getTypeAbilities(type: MoveType): Set<MoveAbility> {
        return when (type) {
            is MoveBuiltinType -> type.abilities
            is MoveNamedType -> {
                // Look up struct definition to get abilities
                val struct = findStructByName(type.name) ?: return emptySet()
                struct.abilities
            }
            is MoveGenericType -> {
                // For generic types, abilities depend on type arguments
                // This is a simplified version
                val baseAbilities = getTypeAbilities(MoveNamedType(type.baseName))
                // TODO: Check type parameter constraints
                baseAbilities
            }
            is MoveReferenceType -> {
                // References have limited abilities
                setOf(MoveAbility.COPY, MoveAbility.DROP)
            }
            else -> emptySet()
        }
    }
    
    private fun findStructByName(name: String): MoveStruct? {
        // TODO: Implement struct lookup using index
        return null
    }
    
    companion object {
        fun getInstance(project: Project): MoveTypeInferenceEngine {
            return project.getService(MoveTypeInferenceEngine::class.java)
        }
    }
}
