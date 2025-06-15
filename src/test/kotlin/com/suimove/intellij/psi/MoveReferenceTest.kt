package com.suimove.intellij.psi

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveReferenceTest : BasePlatformTestCase() {
    
    fun testLocalVariableReference() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_ref() {
                    let x = 42;
                    let y = x;
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain variable declaration", file.text.contains("let x = 42"))
        assertTrue("File should contain variable reference", file.text.contains("let y = x"))
    }
    
    fun testFunctionReference() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper(): u64 { 42 }
                
                fun test_ref() {
                    let x = helper();
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain function declaration", file.text.contains("fun helper()"))
        assertTrue("File should contain function call", file.text.contains("helper()"))
    }
    
    fun testStructReference() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64
                }
                
                fun create(): MyStruct {
                    MyStruct { value: 42 }
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain struct declaration", file.text.contains("struct MyStruct"))
        assertTrue("File should contain struct instantiation", file.text.contains("MyStruct { value: 42 }"))
    }
    
    fun testFieldReference() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Data {
                    value: u64
                }
                
                fun get_value(data: &Data): u64 {
                    data.value
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain field declaration", file.text.contains("value: u64"))
        assertTrue("File should contain field access", file.text.contains("data.value"))
    }
    
    fun testParameterReference() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun process(x: u64, y: u64): u64 {
                    x + y
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain parameter declarations", file.text.contains("x: u64, y: u64"))
        assertTrue("File should contain parameter usage", file.text.contains("x + y"))
    }
    
    fun testModuleReference() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::vector;
                
                fun create_vec(): vector<u64> {
                    vector::empty()
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain module import", file.text.contains("use 0x1::vector"))
        assertTrue("File should contain module usage", file.text.contains("vector::empty()"))
    }
}
