package com.suimove.intellij.parser

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveFileType
import junit.framework.TestCase

class MoveErrorRecoveryTest : BasePlatformTestCase() {
    
    fun testRecoverFromMissingClosingBrace() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun missing_brace() {
                    let x = 42;
                    // Missing closing brace
                
                fun next_function() {
                    // Parser should recover and parse this function
                }
            }
        """.trimIndent())
        
        // Check that parser recovered and parsed content
        assertTrue("Should contain first function", file.text.contains("fun missing_brace()"))
        assertTrue("Should contain second function", file.text.contains("fun next_function()"))
        
        // Verify the file was parsed (even if with errors)
        assertNotNull("File should be parsed", file.firstChild)
    }
    
    fun testRecoverFromInvalidFunctionSyntax() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun 123() {} // Invalid function name
                
                fun valid_function() {
                    let x = 1;
                }
                
                struct ValidStruct {
                    field: u64
                }
            }
        """.trimIndent())
        
        // Check recovery - valid elements should still be parsed
        assertTrue("Should contain valid function", file.text.contains("fun valid_function()"))
        assertTrue("Should contain valid struct", file.text.contains("struct ValidStruct"))
    }
    
    fun testRecoverFromMalformedStruct() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                struct MissingBrace {
                    field: u64
                    // Missing closing brace
                
                struct NextStruct {
                    value: bool
                }
                
                fun some_function() {
                    // Should still parse
                }
            }
        """.trimIndent())
        
        // Check that subsequent elements are still recognized
        assertTrue("Should contain NextStruct", file.text.contains("struct NextStruct"))
        assertTrue("Should contain function", file.text.contains("fun some_function()"))
    }
    
    fun testRecoverFromIncompleteUseStatement() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                use 0x1::
                // Incomplete use statement
                
                use 0x2::valid_module;
                
                fun test() {
                    // Function should still be parsed
                }
            }
        """.trimIndent())
        
        // Check recovery
        assertTrue("Should contain valid use statement", file.text.contains("use 0x2::valid_module"))
        assertTrue("Should contain function", file.text.contains("fun test()"))
    }
    
    fun testRecoverFromInvalidTypeAnnotations() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun test() {
                    let x: = 42; // Missing type
                    let y: u64 = 100; // Valid
                    let z: vector<> = vector[]; // Invalid generic
                    let valid: bool = true;
                }
            }
        """.trimIndent())
        
        // Check that valid declarations are still present
        assertTrue("Should contain valid type annotation", file.text.contains("let y: u64 = 100"))
        assertTrue("Should contain valid bool declaration", file.text.contains("let valid: bool = true"))
    }
    
    fun testRecoverFromExpressionErrors() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun expressions() {
                    let x = 1 + ; // Incomplete expression
                    let y = 2 * 3; // Valid expression
                    let z = if (true) 1; // Missing else
                    let valid = 4 + 5; // Valid
                }
            }
        """.trimIndent())
        
        // Check recovery
        assertTrue("Should contain valid multiplication", file.text.contains("let y = 2 * 3"))
        assertTrue("Should contain valid addition", file.text.contains("let valid = 4 + 5"))
    }
    
    fun testRecoverFromAbilityErrors() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                struct Invalid has copy, , drop {} // Extra comma
                
                struct Valid has copy, drop {
                    field: u64
                }
                
                struct Another has store {
                    value: bool
                }
            }
        """.trimIndent())
        
        // Check recovery
        assertTrue("Should contain Valid struct", file.text.contains("struct Valid has copy, drop"))
        assertTrue("Should contain Another struct", file.text.contains("struct Another has store"))
    }
    
    fun testRecoverFromGenericTypeErrors() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                struct Generic<> {} // Empty generic
                struct ValidGeneric<T> { value: T }
                struct Complex<T: copy + drop, U> { a: T, b: U }
                fun generic_fun<>() {} // Empty generic
                fun valid_generic<T>() {}
            }
        """.trimIndent())
        
        // Check recovery
        assertTrue("Should contain ValidGeneric", file.text.contains("struct ValidGeneric<T>"))
        assertTrue("Should contain Complex struct", file.text.contains("struct Complex<T: copy + drop, U>"))
        assertTrue("Should contain valid_generic function", file.text.contains("fun valid_generic<T>()"))
    }
    
    fun testRecoverFromNestedErrors() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun nested() {
                    let result = {
                        let x = 1 + ; // Error in nested block
                        let y = 2;
                        x + y // Missing semicolon
                    };
                    
                    let valid = 42;
                }
                
                fun another_function() {
                    // Should still be parsed
                }
            }
        """.trimIndent())
        
        // Check recovery
        assertTrue("Should contain valid assignment", file.text.contains("let valid = 42"))
        assertTrue("Should contain another_function", file.text.contains("fun another_function()"))
    }
    
    fun testRecoverFromMixedErrors() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                use 0x1::; // Incomplete
                
                struct S has copy, {} // Invalid abilities
                
                fun f<>() { // Empty generics
                    let x: = 1; // Missing type
                    let y = 1 + ; // Incomplete expr
                }
                
                // Valid elements after errors
                struct ValidStruct { field: u64 }
                fun valid_function() { let x = 1; }
            }
        """.trimIndent())
        
        // Check that valid elements are still present
        assertTrue("Should contain ValidStruct", file.text.contains("struct ValidStruct"))
        assertTrue("Should contain valid_function", file.text.contains("fun valid_function()"))
    }
}
