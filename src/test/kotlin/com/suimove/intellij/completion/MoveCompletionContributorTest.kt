package com.suimove.intellij.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveCompletionContributorTest : BasePlatformTestCase() {
    
    override fun setUp() {
        super.setUp()
    }
    
    fun testKeywordCompletion() {
        myFixture.configureByText("test.move", """
            mo<caret>
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val keywords = lookupElements.filter { it.lookupString == "module" }
        assertTrue("Should suggest 'module' keyword", keywords.isNotEmpty())
    }
    
    fun testFunctionKeywordCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fu<caret>
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val keywords = lookupElements.filter { it.lookupString == "fun" }
        assertTrue("Should suggest 'fun' keyword", keywords.isNotEmpty())
    }
    
    fun testStructKeywordCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                st<caret>
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val keywords = lookupElements.filter { it.lookupString == "struct" }
        assertTrue("Should suggest 'struct' keyword", keywords.isNotEmpty())
    }
    
    fun testPrimitiveTypeCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test(): <caret> {
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val types = lookupElements.filter { it.lookupString == "u64" || it.lookupString == "bool" }
        assertTrue("Should suggest primitive types", types.isNotEmpty())
    }
    
    fun testLocalVariableCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test() {
                    let variable = 42;
                    var<caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val variables = lookupElements.filter { it.lookupString == "variable" }
        assertTrue("Should suggest local variable", variables.isNotEmpty())
    }
    
    fun testFunctionCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper() {}
                
                fun main() {
                    hel<caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val functions = lookupElements.filter { it.lookupString == "helper" }
        assertTrue("Should suggest function name", functions.isNotEmpty())
    }
    
    fun testStructCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {}
                
                fun main() {
                    MyS<caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val structs = lookupElements.filter { it.lookupString == "MyStruct" }
        assertTrue("Should suggest struct name", structs.isNotEmpty())
    }
    
    fun testStructFieldCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    field1: u64,
                    field2: bool
                }
                
                fun main() {
                    let s = MyStruct { field1: 42, field2: true };
                    s.fi<caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val fields = lookupElements.filter { it.lookupString == "field1" || it.lookupString == "field2" }
        assertTrue("Should suggest struct fields", fields.isNotEmpty())
        assertEquals("Should suggest both fields", 2, fields.size)
    }
    
    fun testModuleImportCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::ve<caret>
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val modules = lookupElements.filter { it.lookupString == "vector" }
        assertTrue("Should suggest standard modules", modules.isNotEmpty())
    }
    
    fun testImportedModuleFunctionCompletion() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::vector;
                
                fun main() {
                    vector::<caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val functions = lookupElements.filter { it.lookupString == "empty" || it.lookupString == "push_back" }
        assertTrue("Should suggest vector functions", functions.isNotEmpty())
    }
    
    fun testCompletionInComment() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                // This is a co<caret>
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        // Completion should not be triggered in comments
        assertTrue("Should not provide completions in comments", 
            lookupElements == null || lookupElements.isEmpty())
    }
    
    fun testCompletionAfterDot() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let x = 0;
                    x.<caret>
                }
            }
        """.trimIndent())
        
        // No methods for primitive types
        val lookupElements = myFixture.completeBasic()
        assertTrue("Should not provide completions for primitives", 
            lookupElements == null || lookupElements.isEmpty())
    }
    
    fun testCompletionWithGenericType() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun generic<T>(x: T): T {
                    <caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val typeParams = lookupElements.filter { it.lookupString == "T" }
        assertTrue("Should suggest generic type parameter", typeParams.isNotEmpty())
    }
    
    fun testCompletionForConstant() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                const MAX_VALUE: u64 = 100;
                
                fun main() {
                    MAX<caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val constants = lookupElements.filter { it.lookupString == "MAX_VALUE" }
        assertTrue("Should suggest constant", constants.isNotEmpty())
    }
    
    fun testCompletionForFunctionParameter() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test(param1: u64, param2: bool) {
                    pa<caret>
                }
            }
        """.trimIndent())
        
        val lookupElements = myFixture.completeBasic()
        assertNotNull("Should have completion results", lookupElements)
        
        val params = lookupElements.filter { it.lookupString == "param1" || it.lookupString == "param2" }
        assertEquals("Should suggest both parameters", 2, params.size)
    }
}
