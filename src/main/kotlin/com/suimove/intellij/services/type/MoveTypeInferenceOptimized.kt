package com.suimove.intellij.services.type

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.performance.MovePerformanceMonitor
import java.util.concurrent.ConcurrentHashMap

/**
 * Optimized type inference engine for Move language with performance improvements.
 * 
 * Key optimizations:
 * - Aggressive caching with proper invalidation
 * - Lazy evaluation of complex types
 * - Parallel type resolution for independent expressions
 * - Memory-efficient type representation
 */
@Service(Service.Level.PROJECT)
class MoveTypeInferenceOptimized(private val project: Project) {
    
    private val cache = project.getService(MoveTypeCacheOptimized::class.java)
    private val performanceMonitor = MovePerformanceMonitor.getInstance(project)
    
    // Type resolution context for batch operations
    private val typeResolutionContexts = ConcurrentHashMap<String, TypeResolutionContext>()
    
    /**
     * Infer type with optimized caching and performance tracking.
     */
    fun inferType(element: PsiElement): MoveType? {
        return performanceMonitor.measureOperation("type_inference") {
            cache.getOrComputeType(element) {
                inferTypeInternal(element)
            }
        }
    }
    
    /**
     * Batch infer types for multiple elements.
     */
    fun inferTypes(elements: List<PsiElement>): Map<PsiElement, MoveType?> {
        return performanceMonitor.measureOperation("batch_type_inference") {
            // Group elements by containing file for better cache locality
            val byFile = elements.groupBy { it.containingFile }
            
            val results = ConcurrentHashMap<PsiElement, MoveType?>()
            
            byFile.entries.parallelStream().forEach { (file, fileElements) ->
                // Create a resolution context for the file
                val context = getOrCreateResolutionContext(file)
                
                // Process elements in the file
                fileElements.forEach { element ->
                    results[element] = inferTypeWithContext(element, context)
                }
            }
            
            results
        }
    }
    
    private fun inferTypeInternal(element: PsiElement): MoveType? {
        return when (element) {
            is MoveExpression -> inferExpressionType(element)
            is MoveType -> resolveTypeReference(element)
            is MoveVariable -> inferVariableType(element)
            is MoveFunction -> inferFunctionType(element)
            is MoveStruct -> inferStructType(element)
            is MoveStructField -> inferFieldType(element)
            is MoveFunctionParameter -> element.type?.let { resolveTypeElementOptimized(it) }
            else -> null
        }
    }
    
    private fun inferTypeWithContext(element: PsiElement, context: TypeResolutionContext): MoveType? {
        // Check context cache first
        context.resolvedTypes[element]?.let { return it }
        
        val type = inferTypeInternal(element)
        if (type != null) {
            context.resolvedTypes[element] = type
        }
        
        return type
    }
    
    private fun inferExpressionType(expr: MoveExpression): MoveType? {
        return when (expr) {
            is MoveLiteralExpression -> inferLiteralType(expr)
            is MovePathExpression -> inferPathType(expr)
            is MoveFunctionCall -> inferCallType(expr)
            is MoveStructLiteral -> inferStructLiteralType(expr)
            is MoveBinaryExpression -> inferBinaryType(expr)
            is MoveUnaryExpression -> inferUnaryType(expr)
            is MoveIfExpression -> inferIfType(expr)
            is MoveBlockExpression -> inferBlockType(expr)
            is MoveVectorExpression -> inferVectorType(expr)
            is MoveFieldAccessExpression -> inferFieldAccessType(expr)
            is MoveIndexExpression -> inferIndexType(expr)
            is MoveBorrowExpression -> inferBorrowType(expr)
            is MoveDereferenceExpression -> inferDereferenceType(expr)
            is MoveMoveExpression -> inferMoveType(expr)
            is MoveCopyExpression -> inferCopyType(expr)
            else -> null
        }
    }
    
    private fun inferLiteralType(literal: MoveLiteralExpression): MoveType? {
        // For now, we'll infer based on the text content
        val text = literal.text
        return when {
            text == "true" || text == "false" -> MoveBuiltinType.BOOL
            text.startsWith("0x") -> MoveBuiltinType.ADDRESS
            text.startsWith("b\"") || text.startsWith("x\"") -> MoveGenericType("vector", listOf(MoveBuiltinType.U8))
            text.matches(Regex("\\d+u8")) -> MoveBuiltinType.U8
            text.matches(Regex("\\d+u16")) -> MoveBuiltinType.U16
            text.matches(Regex("\\d+u32")) -> MoveBuiltinType.U32
            text.matches(Regex("\\d+u64")) -> MoveBuiltinType.U64
            text.matches(Regex("\\d+u128")) -> MoveBuiltinType.U128
            text.matches(Regex("\\d+u256")) -> MoveBuiltinType.U256
            text.matches(Regex("\\d+")) -> MoveBuiltinType.U64 // Default for untyped integers
            else -> null
        }
    }
    
    private fun inferPathType(path: MovePathExpression): MoveType? {
        val resolved = path.reference?.resolve() ?: return null
        return inferType(resolved)
    }
    
    private fun inferCallType(call: MoveFunctionCall): MoveType? {
        val functionName = call.functionName ?: return null
        val function = call.reference?.resolve() as? MoveFunction ?: return null
        
        val functionType = inferFunctionType(function) ?: return null
        
        // For now, just return the return type without generic substitution
        // TODO: Implement proper generic type substitution when type arguments are available
        return (functionType as? MoveFunctionType)?.returnType
    }
    
    private fun inferStructLiteralType(literal: MoveStructLiteral): MoveType? {
        val structType = literal.structType ?: return null
        
        // If structType is already a MoveType, return it
        if (structType is MoveNamedType) return structType
        
        // Otherwise, try to resolve it from the path
        val path = (literal as? MoveStructLiteralExpression)?.reference?.resolve() as? MoveStruct
        val struct = path ?: return null
        
        return MoveNamedType(
            name = struct.name ?: "",
            moduleAddress = struct.parent?.parent?.let { (it as? MoveModule)?.address },
            moduleName = struct.parent?.parent?.let { (it as? MoveModule)?.name }
        )
    }
    
    private fun inferBinaryType(expr: MoveBinaryExpression): MoveType? {
        val operator = expr.operator
        
        return when (operator) {
            "+", "-", "*", "/", "%" -> {
                // Arithmetic operators preserve type
                inferType(expr.left)
            }
            "<", ">", "<=", ">=", "==", "!=" -> {
                // Comparison operators return bool
                MoveBuiltinType.BOOL
            }
            "&&", "||" -> {
                // Logical operators return bool
                MoveBuiltinType.BOOL
            }
            else -> null
        }
    }
    
    private fun inferUnaryType(expr: MoveUnaryExpression): MoveType? {
        val operator = expr.operator
        
        return when (operator) {
            "!" -> MoveBuiltinType.BOOL
            "-" -> inferType(expr.operand)
            "&", "&mut" -> {
                val inner = inferType(expr.operand) ?: return null
                MoveReferenceType(inner, operator == "&mut")
            }
            "*" -> {
                val refType = inferType(expr.operand) ?: return null
                when (refType) {
                    is MoveReferenceType -> refType.innerType
                    else -> null
                }
            }
            else -> null
        }
    }
    
    private fun inferIfType(expr: MoveIfExpression): MoveType? {
        val thenType = inferType(expr.thenBranch) ?: return null
        val elseType = expr.elseBranch?.let { inferType(it) } ?: MoveBuiltinType.UNIT
        
        // For now, just return the then type if they match
        return if (thenType == elseType) thenType else null
    }
    
    private fun inferBlockType(block: MoveBlockExpression): MoveType? {
        // Type is the type of the last expression if it's not followed by a semicolon
        val statements = block.statements
        val lastStatement = statements.lastOrNull() ?: return MoveBuiltinType.UNIT
        
        return if (lastStatement is MoveExpressionStatement && !lastStatement.hasSemicolon) {
            inferType(lastStatement.expression)
        } else {
            MoveBuiltinType.UNIT
        }
    }
    
    private fun inferVectorType(expr: MoveVectorExpression): MoveType? {
        val elements = expr.elements
        
        return if (elements.isNotEmpty()) {
            val elementType = inferType(elements.first()) ?: return null
            MoveGenericType("vector", listOf(elementType))
        } else {
            // Empty vector, need type annotation
            expr.typeAnnotation?.let { resolveTypeElementOptimized(it) }
        }
    }
    
    private fun inferFieldAccessType(expr: MoveFieldAccessExpression): MoveType? {
        val baseType = inferType(expr.expression ?: return null) ?: return null
        val fieldName = expr.identifier?.text ?: return null
        
        return when (baseType) {
            is MoveNamedType -> {
                // Look up field type in struct definition
                val struct = resolveStruct(baseType) ?: return null
                struct.fields.find { it.name == fieldName }?.let {
                    inferFieldType(it)
                }
            }
            is MoveReferenceType -> inferFieldAccessType(baseType.innerType, fieldName)
            else -> null
        }
    }
    
    private fun inferFieldAccessType(baseType: MoveType, fieldName: String): MoveType? {
        return when (baseType) {
            is MoveNamedType -> {
                val struct = resolveStruct(baseType) ?: return null
                struct.fields.find { it.name == fieldName }?.let {
                    inferFieldType(it)
                }
            }
            else -> null
        }
    }
    
    private fun inferIndexType(expr: MoveIndexExpression): MoveType? {
        val baseType = inferType(expr.expression ?: return null) ?: return null
        
        return when (baseType) {
            is MoveGenericType -> {
                when (baseType.baseName) {
                    "vector" -> baseType.typeArguments.first()
                    else -> null
                }
            }
            is MoveReferenceType -> {
                when (baseType.innerType) {
                    is MoveGenericType -> {
                        when (baseType.innerType.baseName) {
                            "vector" -> baseType.innerType.typeArguments.first()
                            else -> null
                        }
                    }
                    else -> null
                }
            }
            else -> null
        }
    }
    
    private fun inferBorrowType(expr: MoveBorrowExpression): MoveType? {
        val inner = inferType(expr.expression ?: return null) ?: return null
        val mutable = expr.borrowMut != null
        
        return MoveReferenceType(inner, mutable)
    }
    
    private fun inferDereferenceType(expr: MoveDereferenceExpression): MoveType? {
        val refType = inferType(expr.expression ?: return null) ?: return null
        
        return when (refType) {
            is MoveReferenceType -> refType.innerType
            else -> null
        }
    }
    
    private fun inferMoveType(expr: MoveExpression): MoveType? {
        return inferType(expr)
    }
    
    private fun inferCopyType(expr: MoveExpression): MoveType? {
        return inferType(expr)
    }
    
    private fun inferVariableType(variable: MoveVariable): MoveType? {
        // Check if the variable has a type annotation
        val typeAnnotation = (variable as? PsiElement)?.let { element ->
            PsiTreeUtil.findChildOfType(element, MoveTypeElement::class.java)
        }
        if (typeAnnotation != null) {
            return resolveTypeElementOptimized(typeAnnotation)
        }
        
        // Otherwise, try to infer from initialization
        val letStatement = PsiTreeUtil.getParentOfType(variable, MoveLetStatement::class.java)
        return letStatement?.initializationExpression?.let { inferType(it) }
    }
    
    private fun inferFunctionType(function: MoveFunction): MoveType? {
        val paramTypes: List<MoveType> = function.parameters.mapNotNull { param ->
            val paramTypeElement: MoveTypeElement? = param.type
            if (paramTypeElement != null) {
                val resolvedType: MoveType? = resolveTypeElementOptimized(paramTypeElement)
                resolvedType
            } else {
                null
            }
        }
        
        val returnTypeElement: MoveTypeElement? = function.returnTypeElement
        val finalReturnType: MoveType = if (returnTypeElement != null) {
            val resolvedReturnType: MoveType? = resolveTypeElementOptimized(returnTypeElement)
            resolvedReturnType ?: MoveBuiltinType.UNIT
        } else {
            MoveBuiltinType.UNIT
        }
        
        return MoveFunctionType(paramTypes, finalReturnType)
    }
    
    private fun inferStructType(struct: MoveStruct): MoveType? {
        return MoveNamedType(
            name = struct.name ?: "",
            moduleAddress = struct.parent?.parent?.let { (it as? MoveModule)?.address },
            moduleName = struct.parent?.parent?.let { (it as? MoveModule)?.name }
        )
    }
    
    private fun inferFieldType(field: MoveStructField): MoveType? {
        return field.type?.let { resolveTypeElementOptimized(it) }
    }
    
    private fun resolveTypeReference(type: MoveType): MoveType? {
        // For now, just return the type as-is
        // In the future, this could resolve type aliases, etc.
        return type
    }
    
    private fun resolveTypeElementOptimized(type: MoveTypeElement): MoveType? {
        return when (type) {
            is MoveNamedTypeElement -> resolveNamedType(type)
            is MoveGenericTypeElement -> {
                // For generic types like vector<T>, we need to handle them specially
                val baseName = type.baseName ?: return null
                if (baseName == "vector" && type.typeArguments.isNotEmpty()) {
                    val elementType = resolveTypeElementOptimized(type.typeArguments.first()) ?: return null
                    MoveGenericType("vector", listOf(elementType))
                } else {
                    // Other generic types - treat as named types for now
                    null
                }
            }
            is MoveReferenceTypeElement -> {
                val inner = resolveTypeElementOptimized(type.innerType) ?: return null
                MoveReferenceType(inner, type.isMutable)
            }
            is MoveTupleTypeElement -> {
                val elements = type.types.mapNotNull { resolveTypeElementOptimized(it) }
                if (elements.isNotEmpty()) {
                    MoveTupleType(elements)
                } else null
            }
            is MoveBuiltinTypeElement -> {
                val typeName = type.text
                when (typeName) {
                    "bool" -> MoveBuiltinType.BOOL
                    "u8" -> MoveBuiltinType.U8
                    "u16" -> MoveBuiltinType.U16
                    "u32" -> MoveBuiltinType.U32
                    "u64" -> MoveBuiltinType.U64
                    "u128" -> MoveBuiltinType.U128
                    "u256" -> MoveBuiltinType.U256
                    "address" -> MoveBuiltinType.ADDRESS
                    "signer" -> MoveBuiltinType.SIGNER
                    else -> null
                }
            }
            else -> null
        }
    }
    
    private fun resolveNamedType(type: MoveNamedTypeElement): MoveType? {
        val name = type.text?.trim() ?: return null
        
        // Check primitive types
        val builtinType = when (name) {
            "bool" -> MoveBuiltinType.BOOL
            "u8" -> MoveBuiltinType.U8
            "u16" -> MoveBuiltinType.U16
            "u32" -> MoveBuiltinType.U32
            "u64" -> MoveBuiltinType.U64
            "u128" -> MoveBuiltinType.U128
            "u256" -> MoveBuiltinType.U256
            "address" -> MoveBuiltinType.ADDRESS
            "signer" -> MoveBuiltinType.SIGNER
            else -> null
        }
        
        if (builtinType != null) return builtinType
        
        // Resolve to struct or type parameter
        val resolved = type.reference?.resolve() ?: return null
        
        // Check for MoveStruct first
        if (resolved is MoveStruct) {
            val psiElement = resolved as PsiElement
            return MoveNamedType(
                name = resolved.name ?: "",
                moduleAddress = psiElement.parent?.parent?.let { (it as? MoveModule)?.address },
                moduleName = psiElement.parent?.parent?.let { (it as? MoveModule)?.name }
            )
        }
        
        // Check for MoveTypeParameter
        val resolvedName = (resolved as? PsiNamedElement)?.name
        if (resolvedName != null && resolved.javaClass.simpleName == "MoveTypeParameterImpl") {
            return MoveTypeVariable(resolvedName)
        }
        
        return null
    }
    
    private fun resolveStruct(type: MoveNamedType): MoveStruct? {
        // This would need to be implemented to look up the struct definition
        // For now, return null
        return null
    }
    
    private fun substituteType(type: MoveType, substitutions: Map<String, MoveType>): MoveType {
        return when (type) {
            is MoveTypeVariable -> substitutions[type.name] ?: type
            is MoveGenericType -> {
                if (type.baseName == "vector" && type.typeArguments.size == 1) {
                    MoveGenericType("vector", listOf(substituteType(type.typeArguments[0], substitutions)))
                } else {
                    type.copy(typeArguments = type.typeArguments.map { substituteType(it, substitutions) })
                }
            }
            is MoveReferenceType -> MoveReferenceType(
                substituteType(type.innerType, substitutions),
                type.isMutable
            )
            is MoveNamedType -> type
            is MoveTupleType -> MoveTupleType(
                type.types.map { substituteType(it, substitutions) }
            )
            else -> type
        }
    }
    
    private fun getOrCreateResolutionContext(file: PsiElement): TypeResolutionContext {
        val key = file.containingFile?.virtualFile?.path ?: return TypeResolutionContext()
        return typeResolutionContexts.computeIfAbsent(key) { TypeResolutionContext() }
    }
    
    companion object {
        private val PRIMITIVE_TYPES = setOf(
            "bool", "u8", "u16", "u32", "u64", "u128", "u256",
            "address", "signer"
        )
        
        fun getInstance(project: Project): MoveTypeInferenceOptimized {
            return project.getService(MoveTypeInferenceOptimized::class.java)
        }
    }
}

/**
 * Context for batch type resolution within a file.
 */
private class TypeResolutionContext {
    val resolvedTypes = ConcurrentHashMap<PsiElement, MoveType>()
    val moduleImports = mutableMapOf<String, String>()
    val typeAliases = mutableMapOf<String, MoveType>()
}
