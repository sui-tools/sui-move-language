package com.suimove.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveIcons
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.type.*

/**
 * Provides parameter name and type completions in function signatures.
 */
class MoveFunctionParameterCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val function = PsiTreeUtil.getParentOfType(position, MoveFunction::class.java) ?: return
        
        // Check if we're in parameter list
        val parameterList = PsiTreeUtil.getParentOfType(position, MoveFunctionParameterList::class.java)
        if (parameterList != null) {
            addParameterCompletions(position, function, result)
        }
    }
    
    private fun addParameterCompletions(
        position: PsiElement,
        function: MoveFunction,
        result: CompletionResultSet
    ) {
        // Suggest common parameter names based on type
        val typeEngine = MoveTypeInferenceEngine.getInstance(position.project)
        
        // Check if we're after a colon (completing type)
        if (position.prevSibling?.text == ":") {
            addTypeCompletions(result)
        } else {
            // Completing parameter name
            addParameterNameSuggestions(function, result)
        }
    }
    
    private fun addTypeCompletions(result: CompletionResultSet) {
        // Add common parameter types
        val commonTypes = listOf(
            "u8", "u16", "u32", "u64", "u128", "u256",
            "bool", "address", "signer",
            "&signer", "&mut signer",
            "vector<u8>", "vector<address>",
            "String", "Option<T>", "TxContext"
        )
        
        commonTypes.forEach { type ->
            result.addElement(
                LookupElementBuilder.create(type)
                    .withIcon(MoveIcons.TYPE)
                    .withTypeText("type")
                    .withBoldness(type in listOf("signer", "&signer", "TxContext"))
            )
        }
    }
    
    private fun addParameterNameSuggestions(function: MoveFunction, result: CompletionResultSet) {
        // Common parameter name patterns
        val suggestions = mutableListOf<ParameterSuggestion>()
        
        // Check function name for context
        val functionName = function.name?.lowercase() ?: ""
        
        when {
            functionName.contains("transfer") -> {
                suggestions.add(ParameterSuggestion("recipient", "address"))
                suggestions.add(ParameterSuggestion("object", "T"))
                suggestions.add(ParameterSuggestion("ctx", "&mut TxContext"))
            }
            functionName.contains("create") || functionName.contains("new") -> {
                suggestions.add(ParameterSuggestion("ctx", "&mut TxContext"))
                suggestions.add(ParameterSuggestion("value", "u64"))
                suggestions.add(ParameterSuggestion("owner", "address"))
            }
            functionName.contains("update") || functionName.contains("set") -> {
                suggestions.add(ParameterSuggestion("object", "&mut T"))
                suggestions.add(ParameterSuggestion("new_value", "V"))
                suggestions.add(ParameterSuggestion("ctx", "&mut TxContext"))
            }
            functionName.contains("get") || functionName.contains("borrow") -> {
                suggestions.add(ParameterSuggestion("object", "&T"))
                suggestions.add(ParameterSuggestion("id", "ID"))
            }
            functionName.contains("delete") || functionName.contains("destroy") -> {
                suggestions.add(ParameterSuggestion("object", "T"))
                suggestions.add(ParameterSuggestion("ctx", "&mut TxContext"))
            }
        }
        
        // Add entry function specific parameters
        if (function.isEntry) {
            if (!suggestions.any { it.name == "ctx" }) {
                suggestions.add(ParameterSuggestion("ctx", "&mut TxContext"))
            }
        }
        
        // Generic parameter suggestions
        suggestions.addAll(listOf(
            ParameterSuggestion("sender", "&signer"),
            ParameterSuggestion("amount", "u64"),
            ParameterSuggestion("data", "vector<u8>"),
            ParameterSuggestion("index", "u64"),
            ParameterSuggestion("key", "K"),
            ParameterSuggestion("value", "V")
        ))
        
        // Filter out already used parameter names
        val existingParams = function.parameters.mapNotNull { it.name }.toSet()
        
        suggestions.filter { it.name !in existingParams }.forEach { suggestion ->
            result.addElement(
                LookupElementBuilder.create(suggestion.name)
                    .withIcon(MoveIcons.PARAMETER)
                    .withTypeText(suggestion.type)
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val offset = editor.caretModel.offset
                        editor.document.insertString(offset, ": ${suggestion.type}")
                        editor.caretModel.moveToOffset(offset + 2)
                    }
            )
        }
    }
    
    private data class ParameterSuggestion(val name: String, val type: String)
}

/**
 * Interface for function parameter list.
 */
interface MoveFunctionParameterList : PsiElement

/**
 * Extension to check if function is entry.
 */
val MoveFunction.isEntry: Boolean
    get() = this.text.contains("entry") // Simplified check
