package com.suimove.intellij.analysis

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.psi.MoveFile

class MoveSemanticAnalyzerTest : BasePlatformTestCase() {
    
    private lateinit var analyzer: MoveSemanticAnalyzer
    
    override fun setUp() {
        super.setUp()
        analyzer = MoveSemanticAnalyzer()
    }
    
    fun testValidFunctionCall() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun add(a: u64, b: u64): u64 {
                    a + b
                }
                
                fun test_add() {
                    let result = add(1, 2);
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertTrue("Should have no errors for valid function call", errors.isEmpty())
    }
    
    fun testInvalidFunctionCall() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun add(a: u64, b: u64): u64 {
                    a + b
                }
                
                fun test_add() {
                    let result = add(1); // Missing argument
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertFalse("Should have errors for invalid function call", errors.isEmpty())
        assertTrue("Should detect wrong argument count", 
            errors.any { it.message.contains("expects 2 arguments") })
    }
    
    fun testUndefinedFunction() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_undefined() {
                    let result = undefined_function(1, 2);
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertFalse("Should have errors for undefined function", errors.isEmpty())
        assertTrue("Should detect undefined function", 
            errors.any { it.message.contains("Undefined function") })
    }
    
    fun testTypeMismatch() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_types() {
                    let x: u64 = true; // Type mismatch
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertFalse("Should have errors for type mismatch", errors.isEmpty())
        assertTrue("Should detect type mismatch", 
            errors.any { it.message.contains("Type mismatch") })
    }
    
    fun testValidTypeAssignment() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_types() {
                    let x: u64 = 42;
                    let y: bool = true;
                    let z: address = @0x1;
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertTrue("Should have no errors for valid type assignments", errors.isEmpty())
    }
    
    fun testVectorTypeChecking() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_vectors() {
                    let v1: vector<u64> = vector[1, 2, 3];
                    let v2: vector<bool> = vector[true, false];
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertTrue("Should have no errors for valid vector types", errors.isEmpty())
    }
    
    fun testStructFieldAccess() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64,
                    flag: bool
                }
                
                fun test_struct(s: MyStruct) {
                    let v = s.value;
                    let f = s.flag;
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertTrue("Should have no errors for valid struct field access", errors.isEmpty())
    }
    
    fun testInvalidStructFieldAccess() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64
                }
                
                fun test_struct(s: MyStruct) {
                    let v = s.undefined_field;
                }
            }
        """.trimIndent()) as MoveFile
        
        val errors = analyzer.analyze(file)
        assertFalse("Should have errors for invalid field access", errors.isEmpty())
    }
}
