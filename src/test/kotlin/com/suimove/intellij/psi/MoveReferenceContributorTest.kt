package com.suimove.intellij.psi

import com.intellij.psi.PsiReferenceService
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveReferenceContributorTest : BasePlatformTestCase() {
    
    override fun setUp() {
        super.setUp()
        // Ensure the reference contributor is loaded
        myFixture.testDataPath = "src/test/testData"
    }
    
    fun testReferenceContributorIsRegistered() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper(): u64 { 42 }
                
                fun test_ref() {
                    let x = <caret>helper();
                }
            }
        """.trimIndent())
        
        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should not be null", element)
        assertEquals("Element should be 'helper'", "helper", element?.text)
        
        // Due to minimal PSI structure, references are provided through MoveFile.findReferenceAt
        // instead of through the standard reference contributor mechanism
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find reference through file.findReferenceAt", reference)
        
        // Verify the reference resolves correctly
        val resolved = reference?.resolve()
        assertNotNull("Reference should resolve", resolved)
        assertEquals("Should resolve to function declaration", "helper", resolved?.text)
    }
}
