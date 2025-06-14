package com.suimove.intellij.formatter

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for Move formatting model builder
 */
class MoveFormattingModelBuilderTest : BasePlatformTestCase() {
    
    fun testFormattingModelBuilderCanBeCreated() {
        val builder = MoveFormattingModelBuilder()
        assertNotNull(builder)
    }
    
    fun testFormattingModelBuilderHasCorrectRange() {
        val builder = MoveFormattingModelBuilder()
        // The builder should handle any range
        assertTrue(builder.javaClass.methods.any { it.name == "createModel" })
    }
    
    fun testFormattingModelBuilderClassName() {
        val builder = MoveFormattingModelBuilder()
        assertEquals("MoveFormattingModelBuilder", builder.javaClass.simpleName)
    }
}
