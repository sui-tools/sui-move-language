package com.suimove.intellij.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveFileType

class MoveTemplateContextTypeTest : BasePlatformTestCase() {
    
    private lateinit var contextType: MoveTemplateContextType
    
    override fun setUp() {
        super.setUp()
        contextType = MoveTemplateContextType()
    }
    
    fun testIsInContext() {
        // Create a Move file
        val moveFile = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {
                    <caret>
                }
            }
        """.trimIndent())
        
        val context = createContext(moveFile)
        assertTrue("Should be in Move context", contextType.isInContext(context))
    }
    
    fun testIsNotInContextForNonMoveFile() {
        // Create a non-Move file
        val nonMoveFile = myFixture.configureByText("test.txt", """
            This is not a Move file.
            <caret>
        """.trimIndent())
        
        val context = createContext(nonMoveFile)
        assertFalse("Should not be in Move context for non-Move file", contextType.isInContext(context))
    }
    
    fun testIsInContextForModuleBody() {
        val moveFile = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                <caret>
            }
        """.trimIndent())
        
        val context = createContext(moveFile)
        assertTrue("Should be in Move context for module body", contextType.isInContext(context))
    }
    
    fun testIsInContextForFunctionBody() {
        val moveFile = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun test_function() {
                    <caret>
                }
            }
        """.trimIndent())
        
        val context = createContext(moveFile)
        assertTrue("Should be in Move context for function body", contextType.isInContext(context))
    }
    
    fun testIsInContextForEmptyFile() {
        val moveFile = myFixture.configureByText(MoveFileType, "<caret>")
        
        val context = createContext(moveFile)
        assertTrue("Should be in Move context for empty file", contextType.isInContext(context))
    }
    
    fun testIsInContextForComment() {
        val moveFile = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                // <caret> This is a comment
            }
        """.trimIndent())
        
        val context = createContext(moveFile)
        assertTrue("Should be in Move context for comment", contextType.isInContext(context))
    }
    
    private fun createContext(file: com.intellij.psi.PsiFile): TemplateActionContext {
        val offset = myFixture.editor.caretModel.offset
        return TemplateActionContext.create(file, null, offset, offset, false)
    }
}
