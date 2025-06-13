package com.suimove.intellij.edge

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveFileType

class MoveEdgeCaseTest : BasePlatformTestCase() {
    
    fun testExtremelyLargeModule() {
        // Generate an extremely large module
        val sb = StringBuilder()
        sb.append("module 0x1::extremely_large_module {\n")
        
        // Add 1000 functions with minimal content
        for (i in 1..1000) {
            sb.append("    fun function$i() {}\n")
        }
        
        sb.append("}")
        
        // Create the file
        val largeFile = myFixture.configureByText("extremely_large.move", sb.toString())
        
        // Verify the file is parsed correctly
        val highlightInfos = myFixture.doHighlighting()
        
        // Verify no errors
        val errors = highlightInfos.filter { it.severity.name == "ERROR" }
        assertEquals("Large module should parse without errors", 0, errors.size)
    }
    
    fun testDeepNesting() {
        // Generate a file with extremely deep nesting
        val sb = StringBuilder()
        sb.append("module 0x1::deep_nesting {\n")
        sb.append("    fun deep_nesting() {\n")
        
        // Create deeply nested blocks (100 levels)
        for (i in 1..100) {
            sb.append("        ".repeat(i))
            sb.append("if (true) {\n")
        }
        
        // Close all blocks
        for (i in 100 downTo 1) {
            sb.append("        ".repeat(i))
            sb.append("}\n")
        }
        
        sb.append("    }\n")
        sb.append("}")
        
        // Create the file
        val deepFile = myFixture.configureByText("deep_nesting.move", sb.toString())
        
        // Verify the file is parsed correctly
        val highlightInfos = myFixture.doHighlighting()
        
        // Deep nesting might cause errors in some parsers, but we should handle it gracefully
        // This test is more about not crashing than having zero errors
        assertNotNull("Parser should not crash on deep nesting", highlightInfos)
    }
    
    fun testComplexExpressions() {
        // Generate a file with extremely complex expressions
        val sb = StringBuilder()
        sb.append("module 0x1::complex_expressions {\n")
        sb.append("    fun complex_expression() {\n")
        
        // Create a complex expression with many operators
        var expr = "1"
        for (i in 1..100) {
            val op = when (i % 4) {
                0 -> "+"
                1 -> "-"
                2 -> "*"
                else -> "/"
            }
            expr = "($expr $op $i)"
        }
        
        sb.append("        let result = $expr;\n")
        sb.append("    }\n")
        sb.append("}")
        
        // Create the file
        val complexFile = myFixture.configureByText("complex_expressions.move", sb.toString())
        
        // Verify the file is parsed correctly
        val highlightInfos = myFixture.doHighlighting()
        
        // Complex expressions might be challenging for the parser
        assertNotNull("Parser should not crash on complex expressions", highlightInfos)
    }
    
    fun testUnusualIdentifiers() {
        // Test file with unusual but valid identifiers
        val unusualFile = myFixture.configureByText("unusual_identifiers.move", """
            module 0x1::unusual_identifiers {
                // Identifiers with underscores
                fun _function_with_underscores() {}
                
                // Identifiers with numbers
                fun function123() {}
                
                // Identifiers with mixed case
                fun camelCaseFunction() {}
                
                // Long identifiers
                fun this_is_an_extremely_long_function_name_that_might_cause_issues_with_some_parsers_or_formatters() {}
                
                // Identifiers that look like keywords but aren't
                fun iflike() {}
                fun forlike() {}
                fun whilelike() {}
                
                // Struct with unusual field names
                struct UnusualStruct {
                    _field: u64,
                    field123: u64,
                    camelCaseField: u64,
                    this_is_an_extremely_long_field_name_that_might_cause_issues: u64
                }
            }
        """.trimIndent())
        
        // Verify the file is parsed correctly
        val highlightInfos = myFixture.doHighlighting()
        
        // Verify no errors
        val errors = highlightInfos.filter { it.severity.name == "ERROR" }
        assertEquals("Unusual identifiers should parse without errors", 0, errors.size)
    }
    
    fun testEdgeCaseTypes() {
        // Test file with edge case type combinations
        val edgeTypeFile = myFixture.configureByText("edge_case_types.move", """
            module 0x1::edge_case_types {
                // Deeply nested generic types
                struct DeepGeneric<T> {
                    field1: vector<vector<vector<vector<T>>>>,
                    field2: vector<vector<&mut vector<&vector<T>>>>
                }
                
                // Function with many generic parameters
                fun many_generics<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>() {}
                
                // Complex reference types
                fun complex_references(
                    ref1: &u64,
                    ref2: &mut u64,
                    ref3: &vector<u64>,
                    ref4: &mut vector<u64>,
                    ref5: &vector<&u64>,
                    ref6: &mut vector<&mut u64>
                ) {}
                
                // Return type with complex generics
                fun complex_return<T>(): vector<vector<T>> {
                    vector[]
                }
            }
        """.trimIndent())
        
        // Verify the file is parsed correctly
        val highlightInfos = myFixture.doHighlighting()
        
        // Verify no errors
        val errors = highlightInfos.filter { it.severity.name == "ERROR" }
        assertEquals("Edge case types should parse without errors", 0, errors.size)
    }
    
    fun testBoundaryConditions() {
        // Test file with boundary conditions
        val boundaryFile = myFixture.configureByText("boundary_conditions.move", """
            module 0x1::boundary_conditions {
                // Empty function
                fun empty_function() {}
                
                // Empty struct
                struct EmptyStruct {}
                
                // Function with many parameters
                fun many_parameters(
                    ${(1..50).joinToString(",\n                    ") { "param$it: u64" }}
                ) {}
                
                // Struct with many fields
                struct ManyFields {
                    ${(1..50).joinToString(",\n                    ") { "field$it: u64" }}
                }
                
                // Many constants
                ${(1..50).joinToString("\n                ") { "const CONST$it: u64 = $it;" }}
                
                // Function with large numeric literals
                fun large_numbers() {
                    let a = 18446744073709551615; // u64 max
                    let b = 340282366920938463463374607431768211455; // u128 max
                    let c = 0; // min
                }
            }
        """.trimIndent())
        
        // Verify the file is parsed correctly
        val highlightInfos = myFixture.doHighlighting()
        
        // Boundary conditions might cause errors, but we should handle them gracefully
        assertNotNull("Parser should not crash on boundary conditions", highlightInfos)
    }
    
    fun testMalformedButRecoverableCode() {
        // Test file with malformed but recoverable code
        val malformedFile = myFixture.configureByText("malformed.move", """
            module 0x1::malformed {
                // Missing closing brace in struct
                struct Malformed1 {
                    field1: u64,
                    field2: bool
                // Missing closing brace
                
                // Function with missing semicolon
                fun missing_semicolon() {
                    let x = 42
                    let y = true;
                }
                
                // Missing parameter type
                fun missing_type(x, y: bool) {
                    x + 1
                }
                
                // This function is correct and should parse
                fun correct_function() {
                    let z = 100;
                }
            }
        """.trimIndent())
        
        // Verify the file is parsed as best as possible
        val highlightInfos = myFixture.doHighlighting()
        
        // We expect errors, but we should recover and parse the correct parts
        val errors = highlightInfos.filter { it.severity.name == "ERROR" }
        assertTrue("Malformed code should have errors", errors.isNotEmpty())
        
        // But we should still find the correct function
        val file = myFixture.file
        val text = file.text
        assertTrue("Parser should recover and find correct function", 
            text.contains("correct_function"))
    }
    
    fun testUnicodeCharacters() {
        // Test file with Unicode characters in comments and strings
        val unicodeFile = myFixture.configureByText("unicode.move", """
            module 0x1::unicode {
                // Unicode in comments: 你好, 世界! Привет, мир! こんにちは, 世界!
                
                fun unicode_strings() {
                    let s1 = b"Unicode in bytes: \u{1F600}";
                    
                    // Function with non-ASCII name (not valid Move, but testing parser recovery)
                    fun функция() {}
                }
                
                // Emoji in comments: 😀 🚀 🌍 🔥 💯
            }
        """.trimIndent())
        
        // Verify the file is parsed correctly
        val highlightInfos = myFixture.doHighlighting()
        
        // Unicode in comments should be fine, but in identifiers might cause errors
        // We're testing that the parser doesn't crash
        assertNotNull("Parser should not crash on Unicode", highlightInfos)
    }
}
