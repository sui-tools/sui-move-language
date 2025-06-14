package com.suimove.intellij.compiler

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveCompilerErrorParserTest : BasePlatformTestCase() {
    
    fun testParseSimpleError() {
        val output = """
            error[E01001]: type mismatch
              ┌─ sources/test.move:5:13
              │
            5 │     let x: u64 = true;
              │             ^^^   ---- expected 'u64', found 'bool'
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error message", "type mismatch", error.message)
        assertEquals("Should parse file path", "sources/test.move", error.file)
        assertEquals("Should parse line number", 5, error.line)
        assertEquals("Should parse column number", 13, error.column)
        assertEquals("Should be error severity", ErrorSeverity.ERROR, error.severity)
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
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 2 errors", 2, errors.size)
        
        val error1 = errors[0]
        assertEquals("Should parse first error message", "type mismatch", error1.message)
        assertEquals("Should parse first error line", 5, error1.line)
        
        val error2 = errors[1]
        assertEquals("Should parse second error message", "undefined function", error2.message)
        assertEquals("Should parse second error line", 10, error2.line)
    }
    
    fun testParseWarning() {
        val output = """
            warning[W01001]: unused variable
              ┌─ sources/test.move:3:9
              │
            3 │     let unused = 42;
              │         ^^^^^^ variable is never used
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 1 warning", 1, errors.size)
        
        val warning = errors[0]
        assertEquals("Should parse warning message", "unused variable", warning.message)
        assertEquals("Should be warning severity", ErrorSeverity.WARNING, warning.severity)
    }
    
    fun testParseSimpleFormat() {
        val output = """
            sources/test.move:10:5: error: undefined function 'foo'
            sources/test.move:15:10: warning: unused variable 'x'
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 2 errors", 2, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error file", "sources/test.move", error.file)
        assertEquals("Should parse error line", 10, error.line)
        assertEquals("Should parse error column", 5, error.column)
        assertEquals("Should parse error message", "undefined function 'foo'", error.message)
        assertEquals("Should be error severity", ErrorSeverity.ERROR, error.severity)
        
        val warning = errors[1]
        assertEquals("Should parse warning file", "sources/test.move", warning.file)
        assertEquals("Should parse warning line", 15, warning.line)
        assertEquals("Should parse warning column", 10, warning.column)
        assertEquals("Should parse warning message", "unused variable 'x'", warning.message)
        assertEquals("Should be warning severity", ErrorSeverity.WARNING, warning.severity)
    }
    
    fun testParseMixedOutput() {
        val output = """
            Compiling module 0x1::test...
            error[E01001]: type mismatch
              ┌─ sources/test.move:5:13
              │
            5 │     let x: u64 = true;
              │             ^^^   ---- expected 'u64', found 'bool'
            
            Compilation failed with 1 error
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 1 error from mixed output", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error message", "type mismatch", error.message)
    }
    
    fun testParseEmptyOutput() {
        val output = ""
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 0 errors from empty output", 0, errors.size)
    }
    
    fun testParseComplexError() {
        val output = """
            error[E01003]: duplicate definition
              ┌─ sources/module1.move:10:5
              │
           10 │     struct MyStruct {
              │     ^^^^^^^^^^^^^^^ duplicate definition
              │
              ┌─ sources/module2.move:5:5
              │
            5 │     struct MyStruct {
              │     --------------- previously defined here
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error message", "duplicate definition", error.message)
        assertEquals("Should parse primary location", "sources/module1.move", error.file)
        assertEquals("Should parse primary line", 10, error.line)
        assertEquals("Should parse primary column", 5, error.column)
        assertEquals("Should be error severity", ErrorSeverity.ERROR, error.severity)
    }
    
    fun testParseNoLocationError() {
        val output = """
            error: failed to resolve dependencies
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error message", "failed to resolve dependencies", error.message)
        assertNull("Should have no file location", error.file)
        assertEquals("Should have line 0 for missing location", 0, error.line)
        assertEquals("Should be error severity", ErrorSeverity.ERROR, error.severity)
    }
    
    fun testParseMultilineMessage() {
        val output = """
            sources/test.move:20:1: error: function body too large
            The function exceeds the maximum allowed size.
            Consider breaking it into smaller functions.
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse only first line of message", "function body too large", error.message)
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
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 1 error", 1, errors.size)
        
        val error = errors[0]
        assertEquals("Should parse error message", "invalid struct field", error.message)
    }
    
    fun testParseErrorWithoutLocation() {
        val output = """
            error: failed to resolve dependencies
              package 'SomePackage' not found in registry
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
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
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertEquals("Should parse 3 items total", 3, errors.size)
        
        val warnings = errors.filter { it.severity == ErrorSeverity.WARNING }
        val actualErrors = errors.filter { it.severity == ErrorSeverity.ERROR }
        
        assertEquals("Should have 2 warnings", 2, warnings.size)
        assertEquals("Should have 1 error", 1, actualErrors.size)
    }
    
    fun testParseSuccessOutput() {
        val output = """
            BUILDING MyModule
            BUILT
            Success
        """.trimIndent()
        
        val errors = MoveCompilerErrorParser.parseCompilerOutput(output, project)
        
        assertTrue("Should return empty list for success output", errors.isEmpty())
    }
}
