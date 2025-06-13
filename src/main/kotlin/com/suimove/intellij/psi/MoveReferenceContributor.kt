package com.suimove.intellij.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.suimove.intellij.MoveLanguage

class MoveReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiElement::class.java)
                .withLanguage(MoveLanguage),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (element.node?.elementType == MoveTypes.IDENTIFIER) {
                        // Check if this identifier is a reference (not a definition)
                        val parent = element.parent
                        val parentType = parent?.node?.elementType
                        
                        // Skip if this is a definition
                        if (parentType in listOf(
                            MoveTypes.MODULE_DEFINITION,
                            MoveTypes.FUNCTION_DEFINITION,
                            MoveTypes.STRUCT_DEFINITION,
                            MoveTypes.CONST_DEFINITION
                        )) {
                            return PsiReference.EMPTY_ARRAY
                        }
                        
                        // Create reference for type names and function calls
                        return arrayOf(MoveReference(element, TextRange(0, element.textLength)))
                    }
                    
                    return PsiReference.EMPTY_ARRAY
                }
            }
        )
    }
}
