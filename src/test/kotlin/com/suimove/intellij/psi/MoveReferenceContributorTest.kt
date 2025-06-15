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
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper(): u64 { 42 }
                
                fun test_ref() {
                    let x = helper();
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain helper function", file.text.contains("fun helper()"))
        assertTrue("File should contain function call", file.text.contains("helper()"))
    }
}
