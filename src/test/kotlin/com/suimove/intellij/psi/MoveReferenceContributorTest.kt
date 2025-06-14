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
        
        // Force reference service to process the element
        val refService = PsiReferenceService.getService()
        val refs = refService.getReferences(element!!, PsiReferenceService.Hints.NO_HINTS)
        
        println("References found: ${refs.size}")
        refs.forEach { ref ->
            println("Reference: ${ref.javaClass.name} -> ${ref.canonicalText}")
        }
        
        assertTrue("Should find at least one reference", refs.isNotEmpty())
    }
}
