package com.suimove.intellij.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class MoveReferenceContributor : PsiReferenceContributor() {
    
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Register for IDENTIFIER tokens specifically
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(MoveTypes.IDENTIFIER),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    // Check if this identifier is a reference (not a definition)
                    
                    // Find the first non-whitespace previous sibling
                    var prevSibling = element.prevSibling
                    while (prevSibling != null && prevSibling.node?.elementType == TokenType.WHITE_SPACE) {
                        prevSibling = prevSibling.prevSibling
                    }
                    
                    // Skip if this is a definition (preceded by a keyword)
                    if (prevSibling?.node?.elementType in listOf(
                            MoveTypes.MODULE,
                            MoveTypes.FUN,
                            MoveTypes.STRUCT,
                            MoveTypes.CONST,
                            MoveTypes.LET,
                            MoveTypes.USE
                        )) {
                        return PsiReference.EMPTY_ARRAY
                    }
                    
                    // Skip if this is a struct field declaration (preceded by colon)
                    if (prevSibling?.node?.elementType == MoveTypes.COLON) {
                        var prevPrevSibling = prevSibling.prevSibling
                        while (prevPrevSibling != null && prevPrevSibling.node?.elementType == TokenType.WHITE_SPACE) {
                            prevPrevSibling = prevPrevSibling.prevSibling
                        }
                        if (prevPrevSibling?.node?.elementType == MoveTypes.IDENTIFIER) {
                            return PsiReference.EMPTY_ARRAY
                        }
                    }
                    
                    // Create reference for this identifier
                    val textRange = TextRange(0, element.textLength)
                    return arrayOf(MoveReference(element, textRange))
                }
            }
        )
    }
}
