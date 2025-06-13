package com.suimove.intellij.findusages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import com.suimove.intellij.lexer.MoveLexerAdapter
import com.suimove.intellij.psi.MoveTypes

class MoveFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            MoveLexerAdapter(),
            TokenSet.create(MoveTypes.IDENTIFIER),
            TokenSet.create(MoveTypes.LINE_COMMENT, MoveTypes.BLOCK_COMMENT),
            TokenSet.create(MoveTypes.INTEGER_LITERAL, MoveTypes.HEX_LITERAL, MoveTypes.BYTE_STRING_LITERAL)
        )
    }
    
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is PsiNamedElement
    }
    
    override fun getHelpId(psiElement: PsiElement): String? = null
    
    override fun getType(element: PsiElement): String {
        return when (element.parent?.node?.elementType) {
            MoveTypes.MODULE_DEFINITION -> "module"
            MoveTypes.FUNCTION_DEFINITION -> "function"
            MoveTypes.STRUCT_DEFINITION -> "struct"
            MoveTypes.CONST_DEFINITION -> "constant"
            else -> "symbol"
        }
    }
    
    override fun getDescriptiveName(element: PsiElement): String {
        return if (element is PsiNamedElement) {
            element.name ?: "<unnamed>"
        } else {
            element.text
        }
    }
    
    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return if (element is PsiNamedElement) {
            element.name ?: element.text
        } else {
            element.text
        }
    }
}
