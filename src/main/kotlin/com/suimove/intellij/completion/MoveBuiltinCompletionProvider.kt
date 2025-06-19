package com.suimove.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveIcons

/**
 * Provides completion for Move built-in types and functions.
 */
class MoveBuiltinCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    companion object {
        private val BUILTIN_TYPES = listOf(
            "bool", "u8", "u16", "u32", "u64", "u128", "u256", 
            "address", "signer", "vector"
        )
        
        private val BUILTIN_FUNCTIONS = listOf(
            "move_from", "move_to", "borrow_global", "borrow_global_mut",
            "exists", "freeze", "assert!", "abort"
        )
    }
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val prefix = result.prefixMatcher.prefix.lowercase()
        
        // Add built-in types if we're in a type context or after ":"
        if (isTypeContext(position)) {
            BUILTIN_TYPES.filter { it.startsWith(prefix) }.forEach { type ->
                result.addElement(
                    LookupElementBuilder.create(type)
                        .withIcon(MoveIcons.TYPE)
                        .withTypeText("built-in type")
                        .withBoldness(true)
                )
            }
        }
        
        // Add built-in functions if we're in an expression context
        if (isExpressionContext(position)) {
            BUILTIN_FUNCTIONS.filter { it.startsWith(prefix) }.forEach { function ->
                result.addElement(
                    LookupElementBuilder.create(function)
                        .withIcon(MoveIcons.FUNCTION)
                        .withTypeText("built-in function")
                        .withBoldness(true)
                )
            }
        }
    }
    
    private fun isTypeContext(position: PsiElement): Boolean {
        val text = position.containingFile.text
        val offset = position.textOffset
        
        // Check if we're after a colon (type annotation)
        if (offset > 0 && text[offset - 1] == ':') {
            return true
        }
        
        // Check if we're in a function return type position
        val beforeText = text.substring(0.coerceAtLeast(offset - 10), offset)
        if (beforeText.contains("): ") || beforeText.contains("):")) {
            return true
        }
        
        // Check if we're after "let x: "
        if (beforeText.matches(Regex(".*\\s*:\\s*$"))) {
            return true
        }
        
        return false
    }
    
    private fun isExpressionContext(position: PsiElement): Boolean {
        val text = position.containingFile.text
        val offset = position.textOffset
        
        // Simple heuristic: we're in expression context if we're inside a function body
        val beforeText = text.substring(0, offset)
        val afterText = text.substring(offset)
        
        // Check if we're inside curly braces (likely a function body)
        val openBraces = beforeText.count { it == '{' }
        val closeBraces = beforeText.count { it == '}' }
        
        return openBraces > closeBraces
    }
}
