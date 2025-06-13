package com.suimove.intellij.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveCompletionTest : BasePlatformTestCase() {
    
    fun testKeywordCompletion() {
        myFixture.configureByText("test.move", "mo<caret>")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("module" in lookupElements!!)
        assertTrue("move" in lookupElements)
    }
    
    fun testTypeCompletion() {
        myFixture.configureByText("test.move", "let x: u<caret>")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("u8" in lookupElements!!)
        assertTrue("u64" in lookupElements)
        assertTrue("u128" in lookupElements)
    }
    
    fun testBuiltinFunctionCompletion() {
        myFixture.configureByText("test.move", "module 0x1::test { fun f() { bor<caret> } }")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("borrow_global" in lookupElements!!)
        assertTrue("borrow_global_mut" in lookupElements)
    }
}
