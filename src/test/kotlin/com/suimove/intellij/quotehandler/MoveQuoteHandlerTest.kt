package com.suimove.intellij.quotehandler

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveQuoteHandler

/**
 * Tests for Move quote handler
 */
class MoveQuoteHandlerTest : BasePlatformTestCase() {
    
    fun testQuoteHandlerCanBeCreated() {
        val handler = MoveQuoteHandler()
        assertNotNull(handler)
    }
    
    fun testQuoteHandlerIsSimpleTokenSetQuoteHandler() {
        val handler = MoveQuoteHandler()
        assertTrue(handler is com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler)
    }
    
    fun testQuoteHandlerClassName() {
        val handler = MoveQuoteHandler()
        assertEquals("MoveQuoteHandler", handler.javaClass.simpleName)
    }
}
