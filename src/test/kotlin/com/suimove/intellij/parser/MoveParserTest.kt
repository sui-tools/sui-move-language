package com.suimove.intellij.parser

import com.intellij.testFramework.ParsingTestCase
import com.suimove.intellij.parser.MoveParserDefinition

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
    
    fun testComplexModule() {
        doTest(true)
    }
    
    fun testGenericTypes() {
        doTest(true)
    }
    
    fun testAbilities() {
        doTest(true)
    }
    
    fun testConstants() {
        doTest(true)
    }
    
    fun testErrorRecovery() {
        doTest(true)
    }
    
    fun testNestedStructs() {
        doTest(true)
    }
    
    fun testFunctionWithGenerics() {
        doTest(true)
    }
    
    fun testMultipleImports() {
        doTest(true)
    }
    
    fun testScriptWithMainFunction() {
        doTest(true)
    }
    
    fun testAcquiresClause() {
        doTest(true)
    }
    
    fun testPhantomTypeParameters() {
        doTest(true)
    }
    
    fun testFriendDeclaration() {
        doTest(true)
    }
    
    fun testSpecBlock() {
        doTest(true)
    }
    
    fun testVectorLiterals() {
        doTest(true)
    }
    
    fun testAddressBlock() {
        doTest(true)
    }
    
    fun testMalformedSyntax() {
        // Test error recovery
        doTest(true)
    }
    
    override fun getTestDataPath(): String = "src/test/testData/parser"
    
    override fun skipSpaces(): Boolean = false
    
    override fun includeRanges(): Boolean = true
}
