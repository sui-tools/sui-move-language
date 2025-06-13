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
        
        // Search in current module
        val module = PsiTreeUtil.getParentOfType(element, PsiElement::class.java) { 
            it.node?.elementType == MoveTypes.MODULE_DEFINITION 
        }
        
        if (module != null) {
            // Look for functions
            val functions = PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
                .filter { it.node?.elementType == MoveTypes.FUNCTION_DEFINITION }
            
            for (function in functions) {
                val functionName = function.node?.findChildByType(MoveTypes.IDENTIFIER)?.text
                if (functionName == name) {
                    return function
                }
            }
            
            // Look for structs
            val structs = PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
                .filter { it.node?.elementType == MoveTypes.STRUCT_DEFINITION }
            
            for (struct in structs) {
                val structName = struct.node?.findChildByType(MoveTypes.IDENTIFIER)?.text
                if (structName == name) {
                    return struct
                }
            }
            
            // Look for constants
            val constants = PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
                .filter { it.node?.elementType == MoveTypes.CONST_DEFINITION }
            
            for (constant in constants) {
                val constantName = constant.node?.findChildByType(MoveTypes.IDENTIFIER)?.text
                if (constantName == name) {
                    return constant
                }
            }
        }
        
        // Search in use statements
        val useStatements = PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.USE_DECL }
        
        for (use in useStatements) {
            val alias = use.node?.findChildByType(MoveTypes.IDENTIFIER)?.text
            if (alias == name) {
                return use
            }
        }
        
        return null
    }
    
    override fun getVariants(): Array<Any> {
        val variants = mutableListOf<String>()
        val file = element.containingFile as? MoveFile ?: return emptyArray()
        
        // Collect all available symbols
        val module = PsiTreeUtil.getParentOfType(element, PsiElement::class.java) { 
            it.node?.elementType == MoveTypes.MODULE_DEFINITION 
        }
        
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
