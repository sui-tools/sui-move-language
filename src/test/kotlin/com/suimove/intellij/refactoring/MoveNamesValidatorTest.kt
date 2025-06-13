package com.suimove.intellij.refactoring

import com.intellij.testFramework.UsefulTestCase

class MoveNamesValidatorTest : UsefulTestCase() {
    
    private lateinit var validator: MoveNamesValidator
    
    override fun setUp() {
        super.setUp()
        validator = MoveNamesValidator()
    }
    
    fun testValidIdentifiers() {
        assertTrue("Should accept simple identifier", 
            validator.isIdentifier("myVariable", null))
        assertTrue("Should accept identifier with numbers", 
            validator.isIdentifier("var123", null))
        assertTrue("Should accept identifier with underscores", 
            validator.isIdentifier("my_variable_name", null))
        assertTrue("Should accept single letter", 
            validator.isIdentifier("x", null))
        assertTrue("Should accept uppercase", 
            validator.isIdentifier("MyStruct", null))
        assertTrue("Should accept all caps", 
            validator.isIdentifier("CONSTANT_VALUE", null))
    }
    
    fun testInvalidIdentifiers() {
        assertFalse("Should reject empty string", 
            validator.isIdentifier("", null))
        assertFalse("Should reject identifier starting with number", 
            validator.isIdentifier("123var", null))
        assertFalse("Should reject identifier with spaces", 
            validator.isIdentifier("my var", null))
        assertFalse("Should reject identifier with special chars", 
            validator.isIdentifier("my-var", null))
        assertFalse("Should reject identifier with dots", 
            validator.isIdentifier("my.var", null))
    }
    
    fun testKeywords() {
        // Test all Move keywords
        val keywords = listOf(
            "module", "use", "fun", "struct", "const", "let", "mut",
            "public", "friend", "script", "if", "else", "while", "loop",
            "return", "abort", "break", "continue", "as", "move", "copy",
            "true", "false", "address", "signer", "vector", "u8", "u64", 
            "u128", "bool", "spec", "pragma", "invariant", "requires",
            "ensures", "aborts_if", "modifies", "emits", "apply", "to",
            "except", "internal", "assume", "assert", "native", "const",
            "global", "local", "forall", "exists", "old", "TRACE"
        )
        
        for (keyword in keywords) {
            assertTrue("Should recognize '$keyword' as keyword", 
                validator.isKeyword(keyword, null))
            assertFalse("Should not accept '$keyword' as identifier", 
                validator.isIdentifier(keyword, null))
        }
    }
    
    fun testNonKeywords() {
        val nonKeywords = listOf(
            "modules", "functions", "structures", "my_module",
            "Function", "STRUCT", "let_var", "true_value"
        )
        
        for (word in nonKeywords) {
            assertFalse("Should not recognize '$word' as keyword", 
                validator.isKeyword(word, null))
            assertTrue("Should accept '$word' as identifier", 
                validator.isIdentifier(word, null))
        }
    }
    
    fun testSpecialCases() {
        // Test underscore prefix (common for unused variables)
        assertTrue("Should accept underscore prefix", 
            validator.isIdentifier("_unused", null))
        assertTrue("Should accept single underscore", 
            validator.isIdentifier("_", null))
        
        // Test type names
        assertTrue("Should accept type-like names", 
            validator.isIdentifier("T", null))
        assertTrue("Should accept generic type names", 
            validator.isIdentifier("T1", null))
    }
    
    fun testCaseSensitivity() {
        // Move is case-sensitive
        assertFalse("Should not recognize 'Module' as keyword", 
            validator.isKeyword("Module", null))
        assertFalse("Should not recognize 'FUN' as keyword", 
            validator.isKeyword("FUN", null))
        
        assertTrue("Should accept 'Module' as identifier", 
            validator.isIdentifier("Module", null))
        assertTrue("Should accept 'FUN' as identifier", 
            validator.isIdentifier("FUN", null))
    }
}
