package com.suimove.intellij.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.TokenType
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.MoveLanguage
import com.suimove.intellij.psi.MoveTypes
import com.suimove.intellij.psi.MoveReference

class MoveFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, MoveLanguage) {
    override fun getFileType(): FileType = MoveFileType

    override fun toString(): String = "Move File"
    
    override fun findReferenceAt(offset: Int): PsiReference? {
        val element = findElementAt(offset)
        if (element != null && element.node?.elementType == MoveTypes.IDENTIFIER) {
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
                return null
            }
            
            // Skip if this is a struct field declaration (preceded by colon)
            if (prevSibling?.node?.elementType == MoveTypes.COLON) {
                var prevPrevSibling = prevSibling.prevSibling
                while (prevPrevSibling != null && prevPrevSibling.node?.elementType == TokenType.WHITE_SPACE) {
                    prevPrevSibling = prevPrevSibling.prevSibling
                }
                if (prevPrevSibling?.node?.elementType == MoveTypes.IDENTIFIER) {
                    return null
                }
            }
            
            // Create reference for this identifier
            val reference = MoveReference(element, element.textRange.shiftLeft(element.textRange.startOffset))
            return reference
        }
        
        return super.findReferenceAt(offset)
    }
}
