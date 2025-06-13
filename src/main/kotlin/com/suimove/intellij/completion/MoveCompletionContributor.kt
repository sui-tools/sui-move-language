package com.suimove.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveLanguage
import com.suimove.intellij.psi.MoveTypes

class MoveCompletionContributor : CompletionContributor() {
    init {
        // Basic keyword completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(MoveLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    // Keywords
                    KEYWORDS.forEach { keyword ->
                        result.addElement(
                            LookupElementBuilder.create(keyword)
                                .bold()
                                .withTypeText("keyword")
                        )
                    }
                    
                    // Types
                    TYPES.forEach { type ->
                        result.addElement(
                            LookupElementBuilder.create(type)
                                .withIcon(null)
                                .withTypeText("type")
                        )
                    }
                    
                    // Built-in functions
                    BUILTIN_FUNCTIONS.forEach { function ->
                        result.addElement(
                            LookupElementBuilder.create(function)
                                .withIcon(null)
                                .withTypeText("function")
                                .withTailText("()")
                        )
                    }
                }
            }
        )
    }
    
    companion object {
        private val KEYWORDS = listOf(
            "module", "script", "fun", "public", "entry", "native",
            "struct", "has", "copy", "drop", "store", "key",
            "const", "let", "mut", "move", "return", "abort",
            "break", "continue", "if", "else", "while", "loop",
            "spec", "pragma", "invariant", "assume", "assert",
            "requires", "ensures", "use", "friend", "acquires",
            "as", "true", "false"
        )
        
        private val TYPES = listOf(
            "u8", "u16", "u32", "u64", "u128", "u256",
            "bool", "address", "signer", "vector"
        )
        
        private val BUILTIN_FUNCTIONS = listOf(
            "assert!", "move_to", "move_from", "borrow_global",
            "borrow_global_mut", "exists", "freeze"
        )
    }
}
