package com.suimove.intellij.parser

import com.intellij.testFramework.ParsingTestCase
import com.suimove.intellij.MoveParserDefinition

class MoveParserTest : ParsingTestCase("", "move", MoveParserDefinition()) {
    
    fun testEmptyModule() {
        doTest(true)
    }
    
    fun testSimpleFunction() {
        doTest(true)
    }
    
    fun testStructDefinition() {
        doTest(true)
    }
    
    fun testUseStatement() {
        doTest(true)
    }
    
    override fun getTestDataPath(): String = "src/test/testData/parser"
    
    override fun skipSpaces(): Boolean = false
    
    override fun includeRanges(): Boolean = true
}
