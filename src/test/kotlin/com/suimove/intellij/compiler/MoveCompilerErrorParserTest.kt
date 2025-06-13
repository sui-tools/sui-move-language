package com.suimove.intellij.compiler

import com.intellij.testFramework.UsefulTestCase

class MoveCompilerErrorParserTest : UsefulTestCase() {
    
    private lateinit var parser: MoveCompilerErrorParser
    
    override fun setUp() {
        super.setUp()
        parser = MoveCompilerErrorParser()
    }
    
    fun testParseSimpleError() {
        val output = """
            error[E01001]: type mismatch
              ┌─ sources/test.move:5:13
              │
            5 │     let x: u64 = true;
              │             ^^^   ---- expected 'u64', found 'bool'
        """.trimIndent()
        
        val errors = parser.parse(output)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error code", "E01001", error.code)
        assertEquals("Should parse error message", "type mismatch", error.message)
        assertEquals("Should parse file path", "sources/test.move", error.file)
        assertEquals("Should parse line number", 5, error.line)
        assertEquals("Should parse column number", 13, error.column)
        assertEquals("Should be error severity", MoveCompilerError.Severity.ERROR, error.severity)
    }
    
    fun testParseMultipleErrors() {
        val output = """
            error[E01001]: type mismatch
              ┌─ sources/test.move:5:13
              │
            5 │     let x: u64 = true;
              │             ^^^   ---- expected 'u64', found 'bool'
              
            error[E01002]: undefined function
              ┌─ sources/test.move:10:9
              │
           10 │         undefined_function();
              │         ^^^^^^^^^^^^^^^^^^ function not found
        """.trimIndent()
        
        val errors = parser.parse(output)
        
        assertEquals("Should parse 2 errors", 2, errors.size)
        
        assertEquals("First error message", "type mismatch", errors[0].message)
        assertEquals("Second error message", "undefined function", errors[1].message)
    }
    
    fun testParseWarning() {
        val output = """
            warning[W01001]: unused variable
              ┌─ sources/test.move:3:9
              │
            3 │     let unused_var = 42;
              │         ^^^^^^^^^^ variable is never used
              │
              = help: consider prefixing with an underscore: '_unused_var'
        """.trimIndent()
        
        val errors = parser.parse(output)
        
        assertEquals("Should parse 1 warning", 1, errors.size)
        
        val warning = errors[0]
        assertEquals("Should parse warning code", "W01001", warning.code)
        assertEquals("Should parse warning message", "unused variable", warning.message)
        assertEquals("Should be warning severity", MoveCompilerError.Severity.WARNING, warning.severity)
        assertTrue("Should include help text", warning.details.contains("consider prefixing"))
    }
    
    fun testParseErrorWithMultilineDetails() {
        val output = """
            error[E01003]: invalid struct field
              ┌─ sources/test.move:15:17
              │
           12 │     struct MyStruct {
           13 │         value: u64,
           14 │     }
           15 │     let s = MyStruct { undefined_field: 42 };
              │                        ^^^^^^^^^^^^^^^ field 'undefined_field' not found
              │
              = help: available fields are: 'value'
        """.trimIndent()
        
        val errors = parser.parse(output)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error message", "invalid struct field", error.message)
        assertTrue("Should include field error details", 
            error.details.contains("field 'undefined_field' not found"))
        assertTrue("Should include help text", 
            error.details.contains("available fields are: 'value'"))
    }
    
    fun testParseErrorWithoutLocation() {
        val output = """
            error: failed to resolve dependencies
              package 'SomePackage' not found in registry
        """.trimIndent()
        
        val errors = parser.parse(output)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error message", "failed to resolve dependencies", error.message)
        assertNull("Should have no file location", error.file)
        assertEquals("Should have line 0 for missing location", 0, error.line)
    }
    
    fun testParseMixedErrorsAndWarnings() {
        val output = """
            warning[W01001]: unused import
              ┌─ sources/test.move:1:5
              │
            1 │ use 0x1::unused_module;
              │     ^^^^^^^^^^^^^^^^^^ import is never used
              
            error[E01001]: type mismatch
              ┌─ sources/test.move:5:13
              │
            5 │     let x: u64 = true;
              │             ^^^   ---- expected 'u64', found 'bool'
              
            warning[W01002]: unused variable
              ┌─ sources/test.move:8:9
              │
            8 │     let unused = 42;
              │         ^^^^^^ variable is never used
        """.trimIndent()
        
        val errors = parser.parse(output)
        
        assertEquals("Should parse 3 items total", 3, errors.size)
        
        val warnings = errors.filter { it.severity == MoveCompilerError.Severity.WARNING }
        val actualErrors = errors.filter { it.severity == MoveCompilerError.Severity.ERROR }
        
        assertEquals("Should have 2 warnings", 2, warnings.size)
        assertEquals("Should have 1 error", 1, actualErrors.size)
    }
    
    fun testParseEmptyOutput() {
        val output = ""
        val errors = parser.parse(output)
        
        assertTrue("Should return empty list for empty output", errors.isEmpty())
    }
    
    fun testParseSuccessOutput() {
        val output = """
            BUILDING MyModule
            BUILT
            Success
        """.trimIndent()
        
        val errors = parser.parse(output)
        
        assertTrue("Should return empty list for success output", errors.isEmpty())
    }
}
