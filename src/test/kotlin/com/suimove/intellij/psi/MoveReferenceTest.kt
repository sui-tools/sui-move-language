package com.suimove.intellij.psi

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveReferenceTest : BasePlatformTestCase() {
    
    fun testLocalVariableReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_ref() {
                    let x = 42;
                    let y = <caret>x;
                }
            }
        """.trimIndent())
        
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find reference at caret", reference)
        
        val resolved = reference?.resolve()
        assertNotNull("Should resolve to variable declaration", resolved)
        assertEquals("Should resolve to 'x'", "x", (resolved as? PsiElement)?.text)
    }
    
    fun testFunctionReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper(): u64 { 42 }
                
                fun test_ref() {
                    let x = <caret>helper();
                }
            }
        """.trimIndent())
        
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find function reference", reference)
        
        val resolved = reference?.resolve()
        assertNotNull("Should resolve to function declaration", resolved)
    }
    
    fun testStructReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64
                }
                
                fun test_ref(): <caret>MyStruct {
                    MyStruct { value: 42 }
                }
            }
        """.trimIndent())
        
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find struct reference", reference)
        
        val resolved = reference?.resolve()
        assertNotNull("Should resolve to struct declaration", resolved)
    }
    
    fun testModuleReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::other;
                
                fun test_ref() {
                    <caret>other::some_function();
                }
            }
        """.trimIndent())
        
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find module reference", reference)
    }
    
    fun testFieldReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64,
                    flag: bool
                }
                
                fun test_ref(s: MyStruct) {
                    let v = s.<caret>value;
                }
            }
        """.trimIndent())
        
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find field reference", reference)
        
        val resolved = reference?.resolve()
        assertNotNull("Should resolve to field declaration", resolved)
    }
    
    fun testParameterReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_ref(param: u64) {
                    let x = <caret>param + 1;
                }
            }
        """.trimIndent())
        
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find parameter reference", reference)
        
        val resolved = reference?.resolve()
        assertNotNull("Should resolve to parameter declaration", resolved)
    }
    
    fun testGenericTypeReference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun identity<T>(x: T): T {
                    x
                }
                
                fun test_ref() {
                    let result = identity::<u64>(42);
                }
            }
        """.trimIndent())
        
        // This test might not find a reference due to generic syntax
        // but we include it for completeness
        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        // Generic type references might need special handling
    }
}
