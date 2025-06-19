package com.suimove.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveIcons
import com.suimove.intellij.psi.MoveTypes

class MoveCompletionContributor : CompletionContributor() {
    init {
        // Built-in types and functions completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            MoveBuiltinCompletionProvider()
        )
        
        // Type-aware completion for expressions
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            MoveTypeAwareCompletionProvider()
        )
        
        // Keyword completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            KeywordCompletionProvider()
        )
        
        // Import completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .afterLeaf(PlatformPatterns.psiElement(MoveTypes.USE)),
            MoveImportCompletionProvider()
        )
        
        // Function parameter completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .afterLeaf(PlatformPatterns.psiElement(MoveTypes.LPAREN)),
            MoveFunctionParameterCompletionProvider()
        )
        
        // Struct field completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .afterLeaf(PlatformPatterns.psiElement(MoveTypes.DOT)),
            MoveStructFieldCompletionProvider()
        )
        
        // Ability completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .afterLeaf(PlatformPatterns.psiElement(MoveTypes.HAS)),
            AbilityCompletionProvider()
        )
    }
    
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
        
        // Add postfix templates
        if (parameters.completionType == CompletionType.BASIC) {
            addPostfixTemplates(parameters, result)
        }
    }
    
    private fun addPostfixTemplates(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val prevElement = position.prevSibling
        
        if (prevElement != null && prevElement.text == ".") {
            // Add postfix templates like .if, .let, .match
            result.addElement(
                LookupElementBuilder.create("if")
                    .withIcon(MoveIcons.KEYWORD)
                    .withTypeText("postfix")
                    .withInsertHandler { context, item ->
                        PostfixTemplateHandler.handleIfTemplate(context)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("let")
                    .withIcon(MoveIcons.KEYWORD)
                    .withTypeText("postfix")
                    .withInsertHandler { context, item ->
                        PostfixTemplateHandler.handleLetTemplate(context)
                    }
            )
        }
    }
}

/**
 * Provides keyword completions based on context.
 */
class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    private val keywords = listOf(
        "module", "script", "fun", "struct", "public", "entry", "native",
        "has", "copy", "drop", "store", "key", "const", "let", "mut",
        "return", "abort", "break", "continue", "if", "else", "while",
        "loop", "move", "use", "friend", "acquires", "as", "true", "false"
    )
    
    private val contextualKeywords = mapOf(
        "struct" to listOf("has"),
        "fun" to listOf("public", "entry", "native", "acquires"),
        "let" to listOf("mut"),
        "public" to listOf("fun", "struct", "use", "friend"),
        "has" to listOf("copy", "drop", "store", "key")
    )
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val prevKeyword = findPreviousKeyword(position)
        
        // Add contextual keywords
        if (prevKeyword != null && contextualKeywords.containsKey(prevKeyword)) {
            contextualKeywords[prevKeyword]?.forEach { keyword ->
                result.addElement(
                    LookupElementBuilder.create(keyword)
                        .withIcon(MoveIcons.KEYWORD)
                        .withBoldness(true)
                        .withTypeText("keyword")
                )
            }
        } else {
            // Add all keywords
            keywords.forEach { keyword ->
                result.addElement(
                    LookupElementBuilder.create(keyword)
                        .withIcon(MoveIcons.KEYWORD)
                        .withBoldness(true)
                        .withTypeText("keyword")
                )
            }
        }
    }
    
    private fun findPreviousKeyword(position: PsiElement): String? {
        var prev = position.prevSibling
        while (prev != null && prev.text.isBlank()) {
            prev = prev.prevSibling
        }
        return if (prev != null && prev.text in keywords) prev.text else null
    }
}

/**
 * Provides ability completions after 'has' keyword.
 */
class AbilityCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    private val abilities = listOf("copy", "drop", "store", "key")
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        abilities.forEach { ability ->
            result.addElement(
                LookupElementBuilder.create(ability)
                    .withIcon(MoveIcons.ABILITY)
                    .withBoldness(true)
                    .withTypeText("ability")
            )
        }
    }
}

/**
 * Handles postfix template insertions.
 */
object PostfixTemplateHandler {
    
    fun handleIfTemplate(context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val offset = editor.caretModel.offset
        
        // Remove the dot and "if"
        val lineStart = document.getLineStartOffset(document.getLineNumber(offset))
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, offset))
        val exprEnd = lineText.lastIndexOf('.')
        
        if (exprEnd >= 0) {
            val exprStart = findExpressionStart(lineText, exprEnd)
            val expression = lineText.substring(exprStart, exprEnd).trim()
            
            // Replace with if statement
            document.replaceString(
                lineStart + exprStart,
                offset,
                "if ($expression) {\n    \n}"
            )
            
            // Position cursor inside if body
            editor.caretModel.moveToOffset(lineStart + exprStart + "if ($expression) {\n    ".length)
        }
    }
    
    fun handleLetTemplate(context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val offset = editor.caretModel.offset
        
        // Similar to handleIfTemplate but creates a let binding
        val lineStart = document.getLineStartOffset(document.getLineNumber(offset))
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, offset))
        val exprEnd = lineText.lastIndexOf('.')
        
        if (exprEnd >= 0) {
            val exprStart = findExpressionStart(lineText, exprEnd)
            val expression = lineText.substring(exprStart, exprEnd).trim()
            
            // Replace with let statement
            document.replaceString(
                lineStart + exprStart,
                offset,
                "let value = $expression;"
            )
            
            // Position cursor at variable name
            editor.caretModel.moveToOffset(lineStart + exprStart + "let ".length)
            editor.selectionModel.setSelection(
                lineStart + exprStart + "let ".length,
                lineStart + exprStart + "let value".length
            )
        }
    }
    
    private fun findExpressionStart(text: String, dotIndex: Int): Int {
        // Simple heuristic to find expression start
        var i = dotIndex - 1
        var parenDepth = 0
        
        while (i >= 0) {
            when (text[i]) {
                ')' -> parenDepth++
                '(' -> {
                    parenDepth--
                    if (parenDepth < 0) return i + 1
                }
                ' ', '\t' -> {
                    if (parenDepth == 0 && i < dotIndex - 1) {
                        return i + 1
                    }
                }
                ';', '{', '}' -> return i + 1
            }
            i--
        }
        
        return 0
    }
}
