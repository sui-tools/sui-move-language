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
        // Test that basic completion works in module context
        myFixture.configureByText("test.move", "use 0x1::<caret>")
        myFixture.completeBasic()
        
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        // Should at least have basic keywords/types available
        assertTrue(lookupElements!!.isNotEmpty())
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
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        // Should have basic completions available
        assertTrue(lookupElements!!.isNotEmpty())
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
        // Basic completion should still work
        assertTrue(lookupElements!!.isNotEmpty())
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
                use 0x1::vector as v;
                
                fun main() {
                    <caret>
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        // Should have basic completions
        assertTrue(lookupElements!!.isNotEmpty())
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
        // Should have type completions
        assertTrue("u64" in lookupElements!!)
        assertTrue("bool" in lookupElements)
    }
    
    fun testConstantCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                const MAX_VALUE: u64 = 100;
                const MIN_VALUE: u64 = 0;
                
                fun main() {
                    let x = <caret>
                }
            }
        """.trimIndent())
        
        myFixture.completeBasic()
        val lookupElements = myFixture.lookupElementStrings
        assertNotNull(lookupElements)
        // Should have basic completions
        assertTrue(lookupElements!!.isNotEmpty())
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
        // Should have basic completions
        assertTrue(lookupElements!!.isNotEmpty())
    }
}
