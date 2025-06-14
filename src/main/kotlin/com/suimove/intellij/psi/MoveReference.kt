package com.suimove.intellij.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class MoveReference(
    element: PsiElement,
    textRange: TextRange
) : PsiReferenceBase<PsiElement>(element, textRange) {
    
    override fun resolve(): PsiElement? {
        val name = element.text
        val file = element.containingFile as? MoveFile ?: return null
        
        // Since we have a flat PSI structure, we need to search through all identifiers
        // and check if they are definitions based on their preceding tokens
        
        val allIdentifiers = PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.IDENTIFIER }
        
        for (identifier in allIdentifiers) {
            // Skip if it's the same element
            if (identifier == element) continue
            
            // Check if this identifier has the same name
            if (identifier.text != name) continue
            
            // Check if this identifier is a definition
            var prevSibling = identifier.prevSibling
            while (prevSibling != null && prevSibling.node?.elementType == TokenType.WHITE_SPACE) {
                prevSibling = prevSibling.prevSibling
            }
            
            // Check if preceded by definition keywords
            val isDefinition = prevSibling?.node?.elementType in listOf(
                MoveTypes.MODULE,
                MoveTypes.FUN,
                MoveTypes.STRUCT,
                MoveTypes.CONST,
                MoveTypes.LET,
                MoveTypes.USE
            )
            
            if (isDefinition) {
                return identifier
            }
            
            // Check for function parameters (identifier followed by colon in function signature)
            var nextSibling = identifier.nextSibling
            while (nextSibling != null && nextSibling.node?.elementType == TokenType.WHITE_SPACE) {
                nextSibling = nextSibling.nextSibling
            }
            
            if (nextSibling?.node?.elementType == MoveTypes.COLON) {
                // Check if we're in a function parameter list by looking for parentheses
                var searchBack: PsiElement? = identifier
                var foundOpenParen = false
                var parenBalance = 0
                
                while (searchBack != null && !foundOpenParen) {
                    searchBack = searchBack.prevSibling
                    when (searchBack?.node?.elementType) {
                        MoveTypes.RPAREN -> parenBalance++
                        MoveTypes.LPAREN -> {
                            parenBalance--
                            if (parenBalance < 0) {
                                // Found the opening parenthesis of our context
                                // Now check if there's a 'fun' keyword before it
                                var funCheck = searchBack.prevSibling
                                while (funCheck != null) {
                                    if (funCheck.node?.elementType == MoveTypes.FUN) {
                                        return identifier
                                    }
                                    if (funCheck.node?.elementType != TokenType.WHITE_SPACE &&
                                        funCheck.node?.elementType != MoveTypes.IDENTIFIER) {
                                        break
                                    }
                                    funCheck = funCheck.prevSibling
                                }
                                break
                            }
                        }
                        MoveTypes.LBRACE -> break // Stop if we hit a brace
                    }
                }
            }
            
            // Check for struct fields (identifier followed by colon in struct body)
            if (nextSibling?.node?.elementType == MoveTypes.COLON) {
                // Look backwards for struct keyword
                var searchElement: PsiElement? = identifier
                var foundStruct = false
                var braceCount = 0
                
                while (searchElement != null && searchElement.textOffset > 0) {
                    searchElement = searchElement.prevSibling ?: searchElement.parent
                    
                    when (searchElement?.node?.elementType) {
                        MoveTypes.RBRACE -> braceCount++
                        MoveTypes.LBRACE -> {
                            braceCount--
                            if (braceCount < 0) {
                                // We've found the opening brace of our context
                                var structCheck = searchElement.prevSibling
                                while (structCheck != null) {
                                    if (structCheck.node?.elementType == MoveTypes.STRUCT) {
                                        foundStruct = true
                                        break
                                    }
                                    if (structCheck.node?.elementType != TokenType.WHITE_SPACE &&
                                        structCheck.node?.elementType != MoveTypes.IDENTIFIER) {
                                        break
                                    }
                                    structCheck = structCheck.prevSibling
                                }
                                break
                            }
                        }
                    }
                }
                
                if (foundStruct) {
                    return identifier
                }
            }
        }
        
        return null
    }
    
    override fun getVariants(): Array<Any> {
        val variants = mutableListOf<String>()
        val file = element.containingFile as? MoveFile ?: return emptyArray()
        
        // Collect all available symbols
        var current: PsiElement? = element
        while (current != null && current.node?.elementType != MoveTypes.MODULE_DEFINITION) {
            current = current.parent
        }
        val module = current
        
        if (module != null) {
            // Add functions
            PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
                .filter { it.node?.elementType == MoveTypes.FUNCTION_DEFINITION }
                .forEach { function ->
                    function.node?.findChildByType(MoveTypes.IDENTIFIER)?.text?.let { 
                        variants.add(it) 
                    }
                }
            
            // Add structs
            PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
                .filter { it.node?.elementType == MoveTypes.STRUCT_DEFINITION }
                .forEach { struct ->
                    struct.node?.findChildByType(MoveTypes.IDENTIFIER)?.text?.let { 
                        variants.add(it) 
                    }
                }
            
            // Add constants
            PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
                .filter { it.node?.elementType == MoveTypes.CONST_DEFINITION }
                .forEach { constant ->
                    constant.node?.findChildByType(MoveTypes.IDENTIFIER)?.text?.let { 
                        variants.add(it) 
                    }
                }
        }
        
        return variants.toTypedArray()
    }
}
