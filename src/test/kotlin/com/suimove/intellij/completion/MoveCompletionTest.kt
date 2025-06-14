package com.suimove.intellij.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveCompletionTest : BasePlatformTestCase() {
    
    fun testKeywordCompletion() {
        myFixture.configureByText("test.move", "mo<caret>")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("module" in lookupElements!!)
        assertTrue("move" in lookupElements)
    }
    
    fun testTypeCompletion() {
        myFixture.configureByText("test.move", "let x: u<caret>")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("u8" in lookupElements!!)
        assertTrue("u64" in lookupElements)
        assertTrue("u128" in lookupElements)
    }
    
    fun testBuiltinFunctionCompletion() {
        myFixture.configureByText("test.move", "module 0x1::test { fun f() { bor<caret> } }")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("borrow_global" in lookupElements!!)
        assertTrue("borrow_global_mut" in lookupElements)
    }
    
    fun testModuleCompletion() {
        // First create a module that can be imported
        myFixture.addFileToProject("std.move", """
            module 0x1::std {
                public fun helper() {}
            }
        """.trimIndent())
        
        myFixture.configureByText("test.move", "use 0x1::<caret>")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("std" in lookupElements!!)
    }
    
    fun testFunctionParameterCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper(value: u64, flag: bool) {}
                
                fun main() {
                    helper(<caret>)
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        // Should suggest parameter types or names
    }
    
    fun testStructFieldCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    field1: u64,
                    field2: bool
                }
                
                fun main() {
                    let s = MyStruct { <caret> };
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("field1" in lookupElements!!)
        assertTrue("field2" in lookupElements)
    }
    
    fun testAbilityCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct has <caret>
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("copy" in lookupElements!!)
        assertTrue("drop" in lookupElements)
        assertTrue("store" in lookupElements)
        assertTrue("key" in lookupElements)
    }
    
    fun testImportAliasCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::std::{Self, helper as h};
                
                fun main() {
                    h<caret>
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("h" in lookupElements!!)
    }
    
    fun testGenericTypeParameterCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun generic_function<T: copy + drop>() {
                    let x: <caret>
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("T" in lookupElements!!)
    }
    
    fun testConstantCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                const MAX_VALUE: u64 = 100;
                const MIN_VALUE: u64 = 0;
                
                fun main() {
                    let x = MA<caret>
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("MAX_VALUE" in lookupElements!!)
    }
    
    fun testVectorMethodCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::vector;
                
                fun main() {
                    let v = vector::empty<u64>();
                    vector::<caret>
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        assertTrue("push_back" in lookupElements!!)
        assertTrue("length" in lookupElements)
        assertTrue("is_empty" in lookupElements)
    }
}
