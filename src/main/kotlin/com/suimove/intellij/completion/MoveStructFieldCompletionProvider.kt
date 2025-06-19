package com.suimove.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveIcons
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.type.*

/**
 * Provides struct field completions after dot operator.
 */
class MoveStructFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val typeEngine = MoveTypeInferenceEngine.getInstance(position.project)
        
        // Find the expression before the dot
        val dotExpr = PsiTreeUtil.getParentOfType(position, MoveDotExpression::class.java)
        val receiver = dotExpr?.receiver ?: findReceiverExpression(position) ?: return
        
        // Infer the type of the receiver
        val receiverType = typeEngine.inferType(receiver) ?: return
        
        when (receiverType) {
            is MoveNamedType -> {
                addStructFieldCompletions(receiverType, result, typeEngine)
            }
            is MoveReferenceType -> {
                if (receiverType.innerType is MoveNamedType) {
                    addStructFieldCompletions(receiverType.innerType, result, typeEngine)
                }
            }
            is MoveGenericType -> {
                addGenericTypeCompletions(receiverType, result)
            }
            else -> {
                // No completions for other types
            }
        }
    }
    
    private fun findReceiverExpression(position: PsiElement): MoveExpression? {
        // Walk back to find the expression before the dot
        var current = position.prevSibling
        while (current != null && (current.text.isBlank() || current.text == ".")) {
            current = current.prevSibling
        }
        
        return current as? MoveExpression
    }
    
    private fun addStructFieldCompletions(
        structType: MoveNamedType,
        result: CompletionResultSet,
        typeEngine: MoveTypeInferenceEngine
    ) {
        val struct = findStructByType(structType) ?: return
        
        struct.fields.forEach { field ->
            val fieldType = field.type?.let { typeEngine.inferType(it) }
            result.addElement(
                LookupElementBuilder.create(field.name ?: "")
                    .withIcon(MoveIcons.FIELD)
                    .withTypeText(fieldType?.displayName() ?: "")
                    .withTailText(
                        if (field.visibility != MoveVisibility.PUBLIC) " (private)" else "",
                        true
                    )
            )
        }
        
        // Add methods if struct has any (future feature)
        addStructMethods(struct, result)
    }
    
    private fun addGenericTypeCompletions(
        genericType: MoveGenericType,
        result: CompletionResultSet
    ) {
        when (genericType.baseName) {
            "vector" -> addVectorMethods(result)
            "Option" -> addOptionMethods(result)
            "Table" -> addTableMethods(result)
            else -> {
                // Try to resolve as a struct
                val structType = MoveNamedType(genericType.baseName)
                val struct = findStructByType(structType)
                if (struct != null) {
                    addStructFieldCompletions(structType, result, MoveTypeInferenceEngine.getInstance(struct.project))
                }
            }
        }
    }
    
    private fun addVectorMethods(result: CompletionResultSet) {
        val methods = listOf(
            MethodInfo("length", "(): u64", "Returns the length of the vector"),
            MethodInfo("is_empty", "(): bool", "Checks if the vector is empty"),
            MethodInfo("push_back", "(element: T)", "Adds an element to the end"),
            MethodInfo("pop_back", "(): T", "Removes and returns the last element"),
            MethodInfo("borrow", "(index: u64): &T", "Borrows an element at index"),
            MethodInfo("borrow_mut", "(index: u64): &mut T", "Mutably borrows an element"),
            MethodInfo("swap", "(i: u64, j: u64)", "Swaps two elements"),
            MethodInfo("reverse", "()", "Reverses the vector in place"),
            MethodInfo("append", "(other: vector<T>)", "Appends another vector"),
            MethodInfo("contains", "(element: &T): bool", "Checks if element exists"),
            MethodInfo("index_of", "(element: &T): (bool, u64)", "Finds element index"),
            MethodInfo("remove", "(index: u64): T", "Removes element at index")
        )
        
        methods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withIcon(MoveIcons.FUNCTION)
                    .withTypeText(method.signature)
                    .withTailText(" - ${method.description}", true)
                    .withInsertHandler(MethodInsertHandler)
            )
        }
    }
    
    private fun addOptionMethods(result: CompletionResultSet) {
        val methods = listOf(
            MethodInfo("is_some", "(): bool", "Checks if Option contains a value"),
            MethodInfo("is_none", "(): bool", "Checks if Option is empty"),
            MethodInfo("unwrap", "(): T", "Unwraps the value, aborts if none"),
            MethodInfo("unwrap_or", "(default: T): T", "Unwraps or returns default"),
            MethodInfo("map", "<U>(f: |T| -> U): Option<U>", "Maps the value"),
            MethodInfo("filter", "(predicate: |&T| -> bool): Option<T>", "Filters the value"),
            MethodInfo("and_then", "<U>(f: |T| -> Option<U>): Option<U>", "Chains options")
        )
        
        methods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withIcon(MoveIcons.FUNCTION)
                    .withTypeText(method.signature)
                    .withTailText(" - ${method.description}", true)
                    .withInsertHandler(MethodInsertHandler)
            )
        }
    }
    
    private fun addTableMethods(result: CompletionResultSet) {
        val methods = listOf(
            MethodInfo("add", "(key: K, value: V)", "Adds a key-value pair"),
            MethodInfo("remove", "(key: K): V", "Removes and returns value"),
            MethodInfo("borrow", "(key: K): &V", "Borrows a value"),
            MethodInfo("borrow_mut", "(key: K): &mut V", "Mutably borrows a value"),
            MethodInfo("contains", "(key: K): bool", "Checks if key exists"),
            MethodInfo("length", "(): u64", "Returns number of entries"),
            MethodInfo("is_empty", "(): bool", "Checks if table is empty"),
            MethodInfo("destroy_empty", "()", "Destroys an empty table")
        )
        
        methods.forEach { method ->
            result.addElement(
                LookupElementBuilder.create(method.name)
                    .withIcon(MoveIcons.FUNCTION)
                    .withTypeText(method.signature)
                    .withTailText(" - ${method.description}", true)
                    .withInsertHandler(MethodInsertHandler)
            )
        }
    }
    
    private fun addStructMethods(struct: MoveStruct, result: CompletionResultSet) {
        // In Move, structs don't have methods, but we can suggest related functions
        // from the same module that take this struct as first parameter
        val module = PsiTreeUtil.getParentOfType(struct, MoveModule::class.java) ?: return
        
        module.functions.filter { function ->
            function.visibility == MoveVisibility.PUBLIC || function.visibility == MoveVisibility.PUBLIC_FRIEND &&
            function.parameters.firstOrNull()?.type?.text?.contains(struct.name ?: "") == true
        }.forEach { function ->
            val params = function.parameters.drop(1).joinToString(", ") { param ->
                "${param.name}: ${param.type?.text ?: "?"}"
            }
            
            result.addElement(
                LookupElementBuilder.create(function.name ?: "")
                    .withIcon(MoveIcons.FUNCTION)
                    .withTypeText(function.returnType?.displayName() ?: "()")
                    .withTailText(if (params.isNotEmpty()) "($params)" else "()", true)
                    .withInsertHandler(MethodInsertHandler)
            )
        }
    }
    
    private fun findStructByType(type: MoveNamedType): MoveStruct? {
        // TODO: Implement struct lookup using index
        return null
    }
    
    private data class MethodInfo(
        val name: String,
        val signature: String,
        val description: String
    )
    
    /**
     * Insert handler for method calls.
     */
    object MethodInsertHandler : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            val editor = context.editor
            val offset = editor.caretModel.offset
            
            // Insert parentheses
            editor.document.insertString(offset, "()")
            
            // Position cursor inside parentheses if method has parameters
            val methodName = item.lookupString
            if (hasParameters(methodName)) {
                editor.caretModel.moveToOffset(offset + 1)
            }
        }
        
        private fun hasParameters(methodName: String): Boolean {
            // Methods that don't take parameters
            val noParamMethods = setOf("length", "is_empty", "is_some", "is_none", "unwrap")
            return methodName !in noParamMethods
        }
    }
}

/**
 * Dot expression interface.
 */
interface MoveDotExpression : MoveExpression {
    val receiver: MoveExpression?
}

/**
 * Extension for field visibility.
 */
val MoveStructField.visibility: MoveVisibility
    get() = when {
        this.text.contains("public") -> MoveVisibility.PUBLIC
        this.text.contains("public(friend)") -> MoveVisibility.PUBLIC_FRIEND
        else -> MoveVisibility.PRIVATE
    }

private fun getDefaultValueForType(type: MoveType?): String {
    return when (type) {
        is MoveBuiltinType -> when (type) {
            MoveBuiltinType.BOOL -> "false"
            MoveBuiltinType.U8, MoveBuiltinType.U16, MoveBuiltinType.U32,
            MoveBuiltinType.U64, MoveBuiltinType.U128, MoveBuiltinType.U256 -> "0"
            MoveBuiltinType.ADDRESS -> "@0x0"
            MoveBuiltinType.STRING -> "\"\""
            else -> "/* value */"
        }
        is MoveGenericType -> when (type.baseName) {
            "vector" -> "vector[]"
            else -> "/* value */"
        }
        is MoveNamedType -> "${type.name} { /* fields */ }"
        else -> "/* value */"
    }
}
