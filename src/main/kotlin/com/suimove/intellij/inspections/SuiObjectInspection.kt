package com.suimove.intellij.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.quickfix.AddUidFieldQuickFix
import com.suimove.intellij.services.type.MoveTypeInferenceEngine

/**
 * Inspection for Sui object requirements.
 */
class SuiObjectInspection : LocalInspectionTool() {
    
    private lateinit var typeEngine: MoveTypeInferenceEngine
    
    override fun getDisplayName(): String = "Sui Object Validation"
    
    override fun getStaticDescription(): String = """
        Checks that Sui objects follow the required patterns:
        - Objects with 'key' ability must have a UID field as the first field
        - Objects should use object::new(ctx) to create UID
        - Transfer functions should be used appropriately
    """.trimIndent()
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        typeEngine = MoveTypeInferenceEngine(holder.project)
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is MoveStruct -> checkStruct(element, holder)
                    is MoveFieldAccess -> checkFieldAccess(element, holder)
                    is MoveFunctionCall -> checkFunctionCall(element, holder)
                    is MoveFunction -> checkTransferFunction(element, holder)
                }
            }
        }
    }
    
    private fun checkStruct(struct: MoveStruct, holder: ProblemsHolder) {
        // Check if struct has key ability
        if (!hasKeyAbility(struct)) return
        
        val fields = struct.fields
        if (fields.isEmpty()) {
            holder.registerProblem(
                struct.nameIdentifier ?: (struct as PsiElement),
                "Sui object with 'key' ability must have at least one field",
                ProblemHighlightType.ERROR
            )
            return
        }
        
        // Check first field is UID
        val firstField = fields.first()
        if (firstField.name != "id" || !isUidType(firstField.type)) {
            holder.registerProblem(
                firstField,
                "First field of Sui object must be 'id: UID'",
                ProblemHighlightType.ERROR,
                AddUidFieldQuickFix(struct)
            )
        }
        
        // Check for store ability without key
        if (struct.hasAbility("store") && !struct.hasAbility("key")) {
            holder.registerProblem(
                struct.nameIdentifier ?: (struct as PsiElement),
                "Objects with 'store' ability typically also need 'key' ability",
                ProblemHighlightType.WARNING
            )
        }
        
        // Check drop ability for object structs
        if (!struct.hasAbility("drop")) {
            holder.registerProblem(
                struct.nameIdentifier ?: (struct as PsiElement),
                "Sui object structs should not have the 'drop' ability",
                ProblemHighlightType.WARNING
            )
        }
    }
    
    private fun checkUidField(struct: MoveStruct, holder: ProblemsHolder) {
        val hasKeyAbility = hasKeyAbility(struct)
        val uidField = struct.fields.find { field ->
            field.name == "id" && isUidType(field.type)
        }
        
        if (hasKeyAbility && uidField == null) {
            holder.registerProblem(
                struct.nameIdentifier ?: (struct as PsiElement),
                "Sui object with 'key' ability must have 'id: UID' as first field",
                ProblemHighlightType.ERROR,
                AddUidFieldQuickFix(struct)
            )
        } else if (!hasKeyAbility && uidField != null) {
            holder.registerProblem(
                uidField as PsiElement,
                "Only structs with 'key' ability should have UID field",
                ProblemHighlightType.WARNING
            )
        }
    }
    
    private fun checkStructLiteral(literal: MoveStructLiteral, holder: ProblemsHolder) {
        val structType = literal.structType ?: return
        val struct = resolveStruct(structType.text) ?: return
        
        if (!struct.hasAbility("key")) return
        
        // Check if id field is properly initialized
        val idField = literal.fields.find { it.name == "id" }
        if (idField == null) {
            holder.registerProblem(
                literal,
                "Sui object must initialize 'id' field",
                ProblemHighlightType.ERROR
            )
            return
        }
        
        // Check if object::new is used
        val idValue = idField.value as? MoveFunctionCall
        if (idValue == null || !isObjectNewCall(idValue)) {
            holder.registerProblem(
                idField,
                "Use 'object::new(ctx)' to create UID",
                ProblemHighlightType.WARNING
            )
        }
        
        // Check for missing fields
        for (field in struct.fields) {
            val fieldName = (field.nameIdentifier as? PsiElement)?.text ?: continue
            val fieldType = (field.type as? PsiElement)?.text ?: ""
            val defaultValue = ""  // TODO: Implement default value extraction
            
            if (literal.fields.none { it.name == fieldName } && defaultValue.isEmpty()) {
                holder.registerProblem(
                    literal as PsiElement,
                    "Missing required field: $fieldName",
                    ProblemHighlightType.ERROR
                )
            }
        }
    }
    
    private fun checkFunctionCall(call: MoveFunctionCall, holder: ProblemsHolder) {
        if (isObjectNewCall(call)) {
            // Check if the created object has key ability
            val structType = inferType(call as MoveExpression)
            if (structType is com.suimove.intellij.services.type.MoveNamedType) {
                val struct = resolveStruct(structType.name)
                if (struct != null && !hasKeyAbility(struct)) {
                    holder.registerProblem(
                        call as PsiElement,
                        "object::new requires a struct with 'key' ability",
                        ProblemHighlightType.ERROR
                    )
                }
            }
        }
        
        val functionName = call.functionName ?: return
        
        // Check transfer functions
        if (isTransferFunction(functionName)) {
            checkTransferCall(call, holder)
        }
    }
    
    private fun checkFieldAccess(element: PsiElement, holder: ProblemsHolder) {
        // Find field access expressions (dot expressions)
        val fieldAccesses = PsiTreeUtil.findChildrenOfType(element, PsiElement::class.java).filter {
            it.javaClass.simpleName == "MoveDotExpressionImpl"
        }
        
        fieldAccesses.forEach { access ->
            val children = access.children
            if (children.size >= 2) {
                val expr = children[0]
                val exprType = typeEngine.inferType(expr as? MoveExpression ?: return@forEach)
                if (exprType is com.suimove.intellij.services.type.MoveNamedType) {
                    // TODO: Resolve struct from type
                    val struct: MoveStruct? = null // resolveStruct(exprType)
                    if (struct != null && hasKeyAbility(struct)) {
                        val fieldName = children.getOrNull(1)?.text
                        if (fieldName == "id") {
                            holder.registerProblem(
                                access,
                                "Cannot directly access 'id' field of Sui objects",
                                ProblemHighlightType.ERROR
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun checkFunctionCalls(element: PsiElement, holder: ProblemsHolder) {
        // Find function call expressions
        val functionCalls = PsiTreeUtil.findChildrenOfType(element, MoveCallExpression::class.java)
        
        functionCalls.forEach { call ->
            val functionPath = (call as? PsiElement)?.children?.firstOrNull()
            val functionName = functionPath?.text
            
            // Check for object::delete calls
            if (functionName?.endsWith("::delete") == true || functionName == "delete") {
                val args = call.arguments
                if (args.isNotEmpty()) {
                    val argType = typeEngine.inferType(args[0])
                    if (argType is com.suimove.intellij.services.type.MoveNamedType) {
                        // TODO: Check if type has key ability
                        holder.registerProblem(
                            call as PsiElement,
                            "Sui objects with 'key' ability cannot be deleted",
                            ProblemHighlightType.ERROR
                        )
                    }
                }
            }
        }
    }
    
    private fun checkTransferCall(call: MoveFunctionCall, holder: ProblemsHolder) {
        // Check if the transferred object has key ability
        val args = call.arguments
        if (args.isNotEmpty()) {
            val firstArg = args.first()
            val argType = inferType(firstArg)
            if (argType is com.suimove.intellij.services.type.MoveNamedType) {
                val struct = resolveStruct(argType.name)
                if (struct != null && !hasKeyAbility(struct)) {
                    holder.registerProblem(
                        firstArg as PsiElement,
                        "Can only transfer objects with 'key' ability",
                        ProblemHighlightType.ERROR
                    )
                }
            }
        }
    }
    
    private fun checkTransferFunction(function: MoveFunction, holder: ProblemsHolder) {
        if (!isTransferFunction(function.name ?: "")) return
        
        val params = function.parameters
        if (params.isEmpty()) return
        
        val firstParam = params.first()
        val paramType = firstParam.type
        if (paramType != null) {
            val resolvedType = typeEngine.inferType(paramType as MoveExpression)
            if (resolvedType is com.suimove.intellij.services.type.MoveNamedType) {
                val struct = resolveStruct(resolvedType.name)
                if (struct != null && !hasKeyAbility(struct)) {
                    holder.registerProblem(
                        firstParam.nameIdentifier ?: (firstParam as PsiElement),
                        "Transfer functions require objects with 'key' ability",
                        ProblemHighlightType.ERROR
                    )
                }
            }
        }
    }
    
    private fun resolveStruct(name: String): MoveStruct? {
        // TODO: Implement struct resolution
        return null
    }
    
    private fun isUidType(type: MoveTypeElement?): Boolean {
        if (type == null) return false
        val typeText = (type as? PsiElement)?.text ?: return false
        return typeText == "UID" || typeText == "sui::object::UID" || typeText == "0x2::object::UID"
    }
    
    private fun isObjectNewCall(call: MoveFunctionCall): Boolean {
        val name = call.functionName ?: return false
        return name == "new" && call.moduleName?.contains("object") == true
    }
    
    private fun isTransferFunction(name: String): Boolean {
        return name in listOf("transfer", "public_transfer", "share_object", "freeze_object")
    }
    
    private fun inferType(expr: MoveExpression): com.suimove.intellij.services.type.MoveType? {
        return typeEngine.inferType(expr)
    }
    
    private fun hasKeyAbility(struct: MoveStruct): Boolean {
        val abilities = struct.abilities
        return abilities.any { ability ->
            (ability as? PsiElement)?.text == "key" 
        }
    }
    
    private fun hasStoreAbility(type: com.suimove.intellij.services.type.MoveType): Boolean {
        // TODO: Check type abilities
        return true
    }
    
    private fun hasMutableReferenceTo(function: MoveFunction, expr: MoveExpression): Boolean {
        // TODO: Check for mutable references
        return false
    }
    
    private fun containsMutableFields(type: com.suimove.intellij.services.type.MoveType): Boolean {
        // TODO: Check if type has mutable fields
        return false
    }
    
    private fun hasDropAbility(type: com.suimove.intellij.services.type.MoveType): Boolean {
        // TODO: Check type abilities
        return true
    }
    
    private fun findUsageContext(access: MoveFieldAccess): UsageContext {
        // TODO: Implement usage context detection
        return UsageContext.UNKNOWN
    }
    
    private fun createAddAbilityQuickFix(structName: String, ability: String): LocalQuickFix {
        return object : LocalQuickFix {
            override fun getName() = "Add '$ability' ability to $structName"
            override fun getFamilyName() = "Add ability"
            
            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                val element = descriptor.psiElement
                val struct = findStructDefinition(element, structName) ?: return
                
                // Add ability to struct
                val factory = MoveElementFactory.getInstance(project)
                val newAbility = factory.createAbility(ability)
                
                val abilityList = struct.abilityList
                if (abilityList != null) {
                    abilityList.add(newAbility)
                } else {
                    // Create new ability list
                    val newAbilityList = factory.createAbilityList(ability)
                    struct.addAfter(newAbilityList, struct.nameIdentifier)
                }
            }
        }
    }
    
    private fun findStructDefinition(element: PsiElement, structName: String): MoveStruct? {
        // TODO: Implement struct definition detection
        return null
    }
    
    private fun getStructType(element: PsiElement): com.suimove.intellij.services.type.MoveType? {
        // TODO: Implement proper struct type resolution
        return null
    }
    
    private fun isFieldAccessible(field: MoveStructField, fieldAccess: MoveFieldAccess): Boolean {
        // TODO: Implement field accessibility check
        return true
    }
    
    private fun isDroppedInContext(expr: MoveExpression, context: UsageContext): Boolean {
        // TODO: Implement dropped in context check
        return false
    }
    
    private fun checkTransferFunctions(struct: MoveStruct, holder: ProblemsHolder) {
        if (!hasKeyAbility(struct)) return
        
        val module = PsiTreeUtil.getParentOfType(struct, MoveModule::class.java) ?: return
        val structName = struct.name ?: return
        
        // Check for public transfer function
        val hasPublicTransfer = module.functions.any { function ->
            function.name == "transfer_${structName.lowercase()}" && function.isPublic
        }
        
        if (!hasPublicTransfer) {
            holder.registerProblem(
                struct.nameIdentifier ?: (struct as PsiElement),
                "Sui object should have a public transfer function",
                ProblemHighlightType.WARNING
            )
        }
    }
    
    private fun checkStructLiteralUsage(element: PsiElement, holder: ProblemsHolder) {
        // Find struct literal expressions
        val structLiterals = PsiTreeUtil.findChildrenOfType(element, MoveStructLiteralExpression::class.java)
        
        structLiterals.forEach { literal ->
            val structType = resolveStructType(literal)
            if (structType != null && hasKeyAbility(structType)) {
                holder.registerProblem(
                    literal as PsiElement,
                    "Sui objects with 'key' ability cannot be created with struct literals",
                    ProblemHighlightType.ERROR
                )
            }
        }
    }
    
    private fun resolveStructType(literal: MoveStructLiteralExpression): MoveStruct? {
        // TODO: Implement proper struct resolution
        return null
    }
}

/**
 * Temporary PSI interfaces for Move types
 */
interface MoveStructType : MoveType {
    val name: String
}

interface MoveType {
    val text: String
}

/**
 * Extension properties for struct abilities.
 */
fun MoveStruct.hasAbility(ability: String): Boolean {
    val abilityList = (this as? PsiElement)?.children?.find { 
        it.node?.elementType?.toString() == "ABILITY_LIST" 
    }
    return abilityList?.text?.contains(ability) == true
}

val MoveStruct.abilities: List<PsiElement>?
    get() = null // TODO: Implement ability parsing

val MoveStruct.abilityList: PsiElement?
    get() = null // TODO: Implement proper ability list access

val MoveStruct.structType: MoveType?
    get() = null // TODO: Implement struct type creation

val MoveFunctionCall.functionName: String?
    get() = this.reference?.canonicalText

val MoveFunctionCall.moduleName: String?
    get() = null // TODO: Extract module name

val MoveFunctionCall.arguments: List<MoveExpression>
    get() = emptyList() // TODO: Extract arguments

val MoveStructLiteral.structType: MoveType?
    get() = null // TODO: Extract struct type

val MoveStructLiteral.fields: List<MoveStructLiteralField>
    get() = emptyList() // TODO: Extract fields

interface MoveFieldAccess : PsiElement {
    val fieldName: String?
}

interface MoveStructLiteral : PsiElement {
    val structType: MoveType?
    val fields: List<MoveStructLiteralField>
}

interface MoveStructLiteralField : PsiElement {
    val name: String?
    val value: MoveExpression?
}

interface MoveElementFactory {
    fun createAbility(ability: String): PsiElement
    fun createAbilityList(ability: String): PsiElement
    fun createParameter(name: String, type: MoveType): PsiElement
    fun createParameter(text: String): MoveFunctionParameter
    
    companion object {
        fun getInstance(project: Project): MoveElementFactory {
            // TODO: Implement proper factory
            return object : MoveElementFactory {
                override fun createAbility(ability: String): PsiElement {
                    throw NotImplementedError("MoveElementFactory not implemented")
                }
                override fun createAbilityList(ability: String): PsiElement {
                    throw NotImplementedError("MoveElementFactory not implemented")
                }
                override fun createParameter(name: String, type: MoveType): PsiElement {
                    throw NotImplementedError("MoveElementFactory not implemented")
                }
                override fun createParameter(text: String): MoveFunctionParameter {
                    throw NotImplementedError("MoveElementFactory not implemented")
                }
            }
        }
    }
}

enum class UsageContext {
    UNKNOWN,
    DROPPED
}

/**
 * Move ability enum.
 */
enum class MoveAbility {
    COPY,
    DROP,
    STORE,
    KEY
}
