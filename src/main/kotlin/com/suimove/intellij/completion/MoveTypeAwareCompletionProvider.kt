package com.suimove.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveIcons
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.type.*
import com.suimove.intellij.services.sui.SuiFrameworkLibrary
import javax.swing.Icon
import com.intellij.openapi.project.Project

/**
 * Provides type-aware code completion for Move language.
 */
class MoveTypeAwareCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val project = position.project
        val typeEngine = MoveTypeInferenceEngine.getInstance(project)
        
        // Analyze the completion context
        val completionContext = analyzeContext(position, typeEngine)
        
        // Add completions based on context
        when (completionContext.type) {
            CompletionContextType.EXPRESSION -> {
                addExpressionCompletions(completionContext, result, typeEngine)
            }
            CompletionContextType.TYPE -> {
                addTypeCompletions(completionContext, result)
            }
            CompletionContextType.STRUCT_FIELD -> {
                addStructFieldCompletions(completionContext, result, typeEngine)
            }
            CompletionContextType.FUNCTION_CALL -> {
                addFunctionCallCompletions(completionContext, result, typeEngine)
            }
            CompletionContextType.DOT_ACCESS -> {
                addDotAccessCompletions(completionContext, result, typeEngine)
            }
        }
    }
    
    private fun analyzeContext(position: PsiElement, typeEngine: MoveTypeInferenceEngine): CompletionContext {
        val parent = position.parent
        
        // Check for dot access (e.g., object.field)
        if (parent is MoveReferenceExpression && position.prevSibling?.text == ".") {
            val receiver = findReceiver(parent)
            val receiverType = receiver?.let { typeEngine.inferType(it) }
            return CompletionContext(
                type = CompletionContextType.DOT_ACCESS,
                expectedType = null,
                receiverType = receiverType,
                position = position
            )
        }
        
        // Check if we're in a type position
        if (PsiTreeUtil.getParentOfType(position, MoveTypeElement::class.java) != null) {
            return CompletionContext(
                type = CompletionContextType.TYPE,
                expectedType = null,
                position = position
            )
        }
        
        // Check if we're in a struct literal
        val structLiteral = PsiTreeUtil.getParentOfType(position, MoveStructLiteralExpression::class.java)
        if (structLiteral != null) {
            val structType = structLiteral.let { typeEngine.inferType(it) }
            return CompletionContext(
                type = CompletionContextType.STRUCT_FIELD,
                expectedType = structType,
                structLiteral = structLiteral,
                position = position
            )
        }
        
        // Check if we're in a function call
        val callExpr = PsiTreeUtil.getParentOfType(position, MoveCallExpression::class.java)
        if (callExpr != null) {
            val function = callExpr.reference?.resolve() as? MoveFunction
            if (function != null) {
                val argIndex = findArgumentIndex(position, callExpr)
                val param = function.parameters.getOrNull(argIndex)
                val paramType = param?.type?.let { typeElement ->
                    typeEngine.inferType(typeElement)
                }
                return CompletionContext(
                    type = CompletionContextType.FUNCTION_CALL,
                    expectedType = paramType,
                    callExpression = callExpr,
                    position = position
                )
            }
        }
        
        // Default to expression context
        val expectedType = inferExpectedType(position, typeEngine)
        return CompletionContext(
            type = CompletionContextType.EXPRESSION,
            expectedType = expectedType,
            position = position
        )
    }
    
    private fun addExpressionCompletions(
        context: CompletionContext,
        result: CompletionResultSet,
        typeEngine: MoveTypeInferenceEngine
    ) {
        val position = context.position
        val file = position.containingFile as? MoveFile ?: return
        
        // Add local variables
        val scope = PsiTreeUtil.getParentOfType(position, MoveBlockExpression::class.java)
        if (scope != null) {
            collectLocalVariables(position).forEach { variable ->
                val type = when (variable) {
                    is MoveVariable -> variable.type ?: typeEngine.inferType(variable)
                    is MoveFunctionParameter -> variable.type?.let { typeElement ->
                        typeEngine.inferType(typeElement)
                    }
                    else -> null
                }
                
                if (context.expectedType == null || (type != null && type.isAssignableTo(context.expectedType))) {
                    result.addElement(createVariableLookupElement(variable, type))
                }
            }
        }
        
        // Add function completions
        collectFunctions(position).forEach { function ->
            val expectedType = context.expectedType
            val returnType = function.children.find { it.text.contains(":") && it != function.parameterList }?.text?.substringAfter(":")?.trim()
            
            // Check if function return type matches expected type
            if (expectedType == null || returnType != null) {
                val element = LookupElementBuilder.create(function.name ?: "")
                    .withIcon(MoveIcons.FUNCTION)
                    .withTypeText(returnType ?: "")
                    .withTailText(getFunctionSignature(function))
                    .withInsertHandler { ctx, _ ->
                        handleFunctionInsert(ctx, function)
                    }
                result.addElement(element)
            }
        }
        
        // Add constants
        collectVisibleConstants(position).forEach { constant ->
            val type = constant.type
            if (context.expectedType == null || (type as? MoveType)?.isAssignableTo(context.expectedType) == true) {
                result.addElement(createConstantLookupElement(constant, type))
            }
        }
        
        // Add struct constructors
        if (context.expectedType is MoveNamedType) {
            val struct = findStructByType(context.expectedType, position.project)
            if (struct != null) {
                result.addElement(createStructConstructorLookupElement(struct))
            }
        }
    }
    
    private fun addTypeCompletions(context: CompletionContext, result: CompletionResultSet) {
        val position = context.position
        val file = position.containingFile as? MoveFile ?: return
        
        // Add built-in types
        listOf("bool", "u8", "u16", "u32", "u64", "u128", "u256", "address", "signer").forEach { type ->
            result.addElement(
                LookupElementBuilder.create(type)
                    .withIcon(MoveIcons.TYPE)
                    .withTypeText("built-in type")
                    .withBoldness(true)
            )
        }
        
        // Add visible structs
        collectVisibleStructs(position).forEach { struct ->
            result.addElement(createStructTypeLookupElement(struct))
        }
        
        // Add type parameters if in generic context
        val function = PsiTreeUtil.getParentOfType(position, MoveFunction::class.java)
        function?.typeParameters?.forEach { typeParam ->
            result.addElement(
                LookupElementBuilder.create(typeParam.name)
                    .withIcon(MoveIcons.TYPE_PARAMETER)
                    .withTypeText("type parameter")
                    .withTailText(
                        if (typeParam.constraints.isNotEmpty()) 
                            ": ${typeParam.constraints.joinToString(" + ")}" 
                        else ""
                    )
            )
        }
    }
    
    private fun addStructFieldCompletions(
        context: CompletionContext,
        result: CompletionResultSet,
        typeEngine: MoveTypeInferenceEngine
    ) {
        val structLiteral = context.structLiteral ?: return
        val structType = context.expectedType as? MoveNamedType ?: return
        val struct = findStructByType(structType, structLiteral.project) ?: return
        
        // Get already provided fields
        val providedFields = structLiteral.fields.mapNotNull { it.name }.toSet()
        
        // Add missing fields
        struct.fields.filter { it.name !in providedFields }.forEach { field ->
            val fieldType = field.type?.text ?: "?"
            result.addElement(
                LookupElementBuilder.create(field.name ?: "")
                    .withIcon(MoveIcons.FIELD)
                    .withTypeText(fieldType)
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val offset = editor.caretModel.offset
                        editor.document.insertString(offset, ": ")
                        editor.caretModel.moveToOffset(offset + 2)
                    }
            )
        }
    }
    
    private fun addFunctionCallCompletions(
        context: CompletionContext,
        result: CompletionResultSet,
        typeEngine: MoveTypeInferenceEngine
    ) {
        // Add completions based on expected parameter type
        if (context.expectedType != null) {
            addExpressionCompletions(context, result, typeEngine)
        }
    }
    
    private fun addDotAccessCompletions(
        context: CompletionContext,
        result: CompletionResultSet,
        typeEngine: MoveTypeInferenceEngine
    ) {
        val receiverType = context.receiverType ?: return
        
        when (receiverType) {
            is MoveNamedType -> {
                val struct = findStructByType(receiverType, context.position.project) ?: return
                
                // Add struct fields
                struct.fields.forEach { field ->
                    val fieldType = field.type?.text ?: "?"
                    result.addElement(
                        LookupElementBuilder.create(field.name ?: "")
                            .withIcon(MoveIcons.FIELD)
                            .withTypeText(fieldType)
                    )
                }
                
                // Add methods (if any)
                // Move doesn't have methods, but we might add associated functions
            }
            else -> {
                // Handle other types (vectors, references, etc.)
            }
        }
    }
    
    private fun addVectorMethods(result: CompletionResultSet) {
        val vectorMethods = listOf(
            "length" to "(): u64",
            "is_empty" to "(): bool",
            "push_back" to "(T)",
            "pop_back" to "(): T",
            "borrow" to "(u64): &T",
            "borrow_mut" to "(u64): &mut T"
        )
        
        vectorMethods.forEach { (name, signature) ->
            result.addElement(
                LookupElementBuilder.create(name)
                    .withIcon(MoveIcons.FUNCTION)
                    .withTypeText(signature)
                    .withTailText("()")
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val offset = editor.caretModel.offset
                        editor.document.insertString(offset, "()")
                        editor.caretModel.moveToOffset(offset + 1)
                    }
            )
        }
    }
    
    private fun createVariableLookupElement(variable: MoveNamedElement, type: MoveType?): LookupElement {
        return LookupElementBuilder.create(variable.name ?: "")
            .withIcon(MoveIcons.VARIABLE)
            .withTypeText(type?.toString() ?: "")
    }
    
    private fun createConstantLookupElement(constant: MoveConstant, type: MoveType?): LookupElement {
        return LookupElementBuilder.create(constant.name ?: "")
            .withIcon(MoveIcons.CONSTANT)
            .withTypeText(type?.toString() ?: "")
            .withBoldness(true)
    }
    
    private fun createStructConstructorLookupElement(struct: MoveStruct): LookupElement {
        val fields = struct.fields.joinToString(", ") { it.name ?: "" }
        
        return LookupElementBuilder.create(struct.name ?: "")
            .withIcon(MoveIcons.STRUCT)
            .withTypeText("struct")
            .withTailText(" { $fields }")
            .withInsertHandler { context, item ->
                val editor = context.editor
                val offset = editor.caretModel.offset
                editor.document.insertString(offset, " { }")
                editor.caretModel.moveToOffset(offset + 3)
            }
    }
    
    private fun createStructTypeLookupElement(struct: MoveStruct): LookupElement {
        val typeParams = if (struct.typeParameters.isNotEmpty()) {
            "<${struct.typeParameters.joinToString(", ") { it.name }}>"
        } else ""
        
        return LookupElementBuilder.create(struct.name ?: "")
            .withIcon(MoveIcons.STRUCT)
            .withTypeText("struct")
            .withTailText(typeParams)
    }
    
    // Helper functions
    
    private fun findReceiver(position: PsiElement): MoveExpression? {
        // Walk back from position to find the expression before the dot
        var current = position.prevSibling
        while (current != null && (current.text.isBlank() || current.text == ".")) {
            current = current.prevSibling
        }
        
        // Check if we found an expression
        return when (current) {
            is MoveExpression -> current
            is PsiElement -> {
                // Try to find expression as parent
                PsiTreeUtil.getParentOfType(current, MoveExpression::class.java)
            }
            else -> null
        }
    }
    
    private fun findArgumentIndex(position: PsiElement, call: MoveCallExpression): Int {
        val args = call.arguments
        return args.indexOfFirst { arg -> PsiTreeUtil.isAncestor(arg, position, false) }
    }
    
    private fun inferExpectedType(position: PsiElement, typeEngine: MoveTypeInferenceEngine): MoveType? {
        val parent = position.parent ?: return null
        
        return when (parent) {
            is MoveLetStatement -> {
                // If position is after '=', expected type is from the pattern
                if (position.prevSibling?.text == "=") {
                    parent.typeAnnotation?.let { typeEngine.inferType(it) }
                } else null
            }
            is MoveAssignment -> {
                // Expected type is the type of the lvalue
                parent.lvalue?.let { lvalue ->
                    typeEngine.inferType(lvalue)
                }
            }
            is MoveCallExpression -> {
                // Find which argument position we're at
                val argIndex = findArgumentIndex(position, parent)
                if (argIndex >= 0) {
                    val function = resolveFunction(parent)
                    val param = function?.parameters?.getOrNull(argIndex)
                    val paramType = param?.type?.let { typeElement ->
                        typeEngine.inferType(typeElement)
                    }
                    paramType
                } else null
            }
            is MoveReturnStatement -> {
                // Expected type is the function's return type
                val function = PsiTreeUtil.getParentOfType(position, MoveFunction::class.java)
                function?.children?.find { it.text.contains(":") && it != function.parameterList }?.text?.substringAfter(":")?.trim()?.let { returnTypeText ->
                    // Parse the return type text to get the actual type
                    null // TODO: Parse return type from text
                }
            }
            is MoveIfExpression -> {
                // Expected type is boolean for condition
                if (position.parent == parent.condition) {
                    MoveBuiltinType.BOOL
                } else {
                    null
                }
            }
            is MoveBinaryExpression -> {
                // For binary expressions, depends on the operator
                when (parent.operator) {
                    "+", "-", "*", "/", "%" -> MoveBuiltinType.U64
                    "&&", "||" -> MoveBuiltinType.BOOL
                    "==", "!=", "<", ">", "<=", ">=" -> {
                        // Expected type is the type of the other operand
                        if (parent.left == position.parent) {
                            parent.right?.let { typeEngine.inferType(it) }
                        } else {
                            parent.left?.let { typeEngine.inferType(it) }
                        }
                    }
                    else -> null
                }
            }
            else -> null
        }
    }
    
    private fun collectFunctions(position: PsiElement): List<MoveFunction> {
        val functions = mutableListOf<MoveFunction>()
        
        // Collect from current module
        val module = PsiTreeUtil.getParentOfType(position, MoveModule::class.java)
        module?.let {
            functions.addAll(PsiTreeUtil.getChildrenOfTypeAsList(it, MoveFunction::class.java))
        }
        
        // TODO: Collect from imported modules
        
        return functions
    }
    
    private fun collectVisibleStructs(position: PsiElement): List<MoveStruct> {
        val structs = mutableListOf<MoveStruct>()
        
        // Structs in current module
        val module = PsiTreeUtil.getParentOfType(position, MoveModule::class.java)
        if (module != null) {
            structs.addAll(module.structs)
        }
        
        // Imported structs
        val file = position.containingFile as? MoveFile
        file?.useStatements?.forEach { use ->
            val importedModule = resolveModule(use)
            if (importedModule != null) {
                structs.addAll(importedModule.structs)
            }
        }
        
        return structs
    }
    
    private fun collectVisibleConstants(position: PsiElement): List<MoveConstant> {
        val constants = mutableListOf<MoveConstant>()
        
        // Constants in current module
        val module = PsiTreeUtil.getParentOfType(position, MoveModule::class.java)
        if (module != null) {
            constants.addAll(module.constants)
        }
        
        // Imported constants
        val file = position.containingFile as? MoveFile
        file?.useStatements?.forEach { use ->
            val importedModule = resolveModule(use)
            if (importedModule != null) {
                constants.addAll(importedModule.constants.filter { it.isPublic })
            }
        }
        
        return constants
    }
    
    private fun collectLocalVariables(position: PsiElement): List<MoveNamedElement> {
        val variables = mutableListOf<MoveNamedElement>()
        
        // Walk up the tree collecting variables in scope
        var current: PsiElement? = position
        while (current != null) {
            when (current) {
                is MoveCodeBlock -> {
                    // Collect let statements before current position
                    current.children.forEach { child ->
                        if (child.textRange.endOffset < position.textRange.startOffset) {
                            if (child is MoveLetStatement) {
                                child.variables.forEach { variables.add(it) }
                            }
                        }
                    }
                }
                is MoveFunction -> {
                    // Add function parameters
                    current.parameters.forEach { param ->
                        variables.add(param)
                    }
                    break // Don't go beyond function scope
                }
            }
            current = current.parent
        }
        
        return variables
    }
    
    private fun resolveFunction(call: MoveCallExpression): MoveFunction? {
        val ref = call.reference
        return ref?.resolve() as? MoveFunction
    }
    
    private fun resolveModule(use: MoveUseStatement): MoveModule? {
        // TODO: Implement module resolution using stub index
        return null
    }
    
    private fun findStructByType(type: MoveNamedType, project: Project): MoveStruct? {
        // TODO: Implement struct lookup by type name
        return null
    }
    
    private fun addFrameworkCompletions(
        element: PsiElement,
        result: CompletionResultSet,
        typeEngine: MoveTypeInferenceEngine
    ) {
        val frameworkLibrary = SuiFrameworkLibrary.getInstance(element.project)
        
        // Add framework modules
        frameworkLibrary.getAllModules().forEach { frameworkModule ->
            frameworkModule.functions.forEach { frameworkFunction ->
                result.addElement(
                    LookupElementBuilder.create(frameworkFunction.name)
                        .withIcon(MoveIcons.FUNCTION)
                        .withTypeText(frameworkFunction.signature)
                        .withTailText(" from ${frameworkModule.name}", true)
                )
            }
        }
    }
    
    private fun getFunctionSignature(function: MoveFunction): String {
        val params = function.parameters.joinToString(", ") { param ->
            "${param.name}: ${param.type?.text ?: "?"}"
        }
        return "($params)"
    }
    
    private fun handleFunctionInsert(context: InsertionContext, function: MoveFunction) {
        val document = context.document
        val offset = context.tailOffset
        
        // Add parentheses if not already present
        if (document.charsSequence.getOrNull(offset) != '(') {
            document.insertString(offset, "()")
            if (function.parameters.isNotEmpty()) {
                // Move cursor inside parentheses
                context.editor.caretModel.moveToOffset(offset + 1)
            }
        }
    }
}

/**
 * Context information for code completion.
 */
data class CompletionContext(
    val type: CompletionContextType,
    val expectedType: MoveType?,
    val receiverType: MoveType? = null,
    val structLiteral: MoveStructLiteralExpression? = null,
    val callExpression: MoveCallExpression? = null,
    val position: PsiElement
)

/**
 * Type of completion context.
 */
enum class CompletionContextType {
    EXPRESSION,
    TYPE,
    STRUCT_FIELD,
    FUNCTION_CALL,
    DOT_ACCESS
}

/**
 * Additional PSI interfaces needed for completion.
 */
interface MoveBinaryExpression : MoveExpression {
    val left: MoveExpression
    val right: MoveExpression
    val operator: String
}

interface MoveReturnStatement : MoveStatement {
    val expression: MoveExpression?
}

interface MoveAssignment : MoveStatement {
    val lvalue: MoveExpression
    val rvalue: MoveExpression
}

interface MoveCodeBlock : PsiElement {
    val statements: List<MoveStatement>
}

val MoveFile.useStatements: List<MoveUseStatement>
    get() = PsiTreeUtil.findChildrenOfType(this, MoveUseStatement::class.java).toList()

val MoveLetStatement.variables: List<MoveVariable>
    get() = listOfNotNull(this.pattern as? MoveVariable)

val MoveFunction.isPublic: Boolean
    get() = this.text.contains("public")

val MoveConstant.isPublic: Boolean
    get() = this.text.contains("public")
