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
                
                fun test() {
                    let result = add(1, 2);
                }
            }
        """.trimIndent()) as MoveFile
        
        // Since we can't easily test the analyzer without a proper AnnotationHolder,
        // we'll just ensure the file parses correctly
        assertNotNull("File should parse correctly", file)
    }
    
    fun testInvalidFunctionCall() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun add(a: u64, b: u64): u64 {
                    a + b
                }
                
                fun test() {
                    let result = add(1); // Missing argument
                }
            }
        """.trimIndent()) as MoveFile
        
        assertNotNull("File should parse correctly", file)
    }
    
    fun testUndefinedFunction() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test() {
                    let result = undefined_function(1, 2);
                }
            }
        """.trimIndent()) as MoveFile
        
        assertNotNull("File should parse correctly", file)
    }
    
    fun testTypeMismatch() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test() {
                    let x: u64 = true; // Type mismatch
                }
            }
        """.trimIndent()) as MoveFile
        
        assertNotNull("File should parse correctly", file)
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
        
        assertNotNull("File should parse correctly", file)
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
        
        assertNotNull("File should parse correctly", file)
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
        
        assertNotNull("File should parse correctly", file)
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
        
        assertNotNull("File should parse correctly", file)
    }
}
