package com.suimove.intellij.navigation

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveNavigationTest : BasePlatformTestCase() {
    
    fun testGotoFunctionDeclaration() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun helper(): u64 { 42 }
                
                fun main() {
                    let x = hel<caret>per();
                }
            }
        """.trimIndent())
        
        val handler = MoveGotoDeclarationHandler()
        val targets = handler.getGotoDeclarationTargets(
            myFixture.file.findElementAt(myFixture.caretOffset), 
            myFixture.caretOffset, 
            myFixture.editor
        )
        
        assertNotNull("Should find declaration targets", targets)
        assertEquals("Should find one target", 1, targets?.size)
        
        val target = targets?.get(0)
        assertTrue("Should navigate to function declaration", 
            target?.text?.contains("fun helper()") == true)
    }
    
    fun testGotoStructDeclaration() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64
                }
                
                fun main() {
                    let s = My<caret>Struct { value: 42 };
                }
            }
        """.trimIndent())
        
        val handler = MoveGotoDeclarationHandler()
        val targets = handler.getGotoDeclarationTargets(
            myFixture.file.findElementAt(myFixture.caretOffset), 
            myFixture.caretOffset, 
            myFixture.editor
        )
        
        assertNotNull("Should find struct declaration", targets)
        assertTrue("Should navigate to struct declaration",
            targets?.any { it.text.contains("struct MyStruct") } == true)
    }
    
    fun testGotoVariableDeclaration() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let my_var = 42;
                    let x = my_<caret>var + 1;
                }
            }
        """.trimIndent())
        
        val handler = MoveGotoDeclarationHandler()
        val targets = handler.getGotoDeclarationTargets(
            myFixture.file.findElementAt(myFixture.caretOffset), 
            myFixture.caretOffset, 
            myFixture.editor
        )
        
        assertNotNull("Should find variable declaration", targets)
        assertTrue("Should navigate to variable declaration",
            targets?.any { it.text.contains("let my_var") } == true)
    }
    
    fun testGotoImportedModule() {
        val otherModule = myFixture.addFileToProject("other.move", """
            module 0x1::other {
                public fun helper(): u64 { 42 }
            }
        """.trimIndent())
        
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::other;
                
                fun main() {
                    let x = ot<caret>her::helper();
                }
            }
        """.trimIndent())
        
        val handler = MoveGotoDeclarationHandler()
        val targets = handler.getGotoDeclarationTargets(
            myFixture.file.findElementAt(myFixture.caretOffset), 
            myFixture.caretOffset, 
            myFixture.editor
        )
        
        assertNotNull("Should find module declaration", targets)
    }
    
    fun testFindUsagesOfFunction() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun <caret>helper(): u64 { 42 }
                
                fun main() {
                    let x = helper();
                    let y = helper() + helper();
                }
                
                fun another() {
                    helper();
                }
            }
        """.trimIndent())
        
        val element = file.findElementAt(myFixture.caretOffset)?.parent
        val usages = myFixture.findUsages(element!!)
        
        assertEquals("Should find 4 usages of helper function", 4, usages.size)
    }
    
    fun testFindUsagesOfStruct() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct <caret>MyStruct {
                    value: u64
                }
                
                fun create(): MyStruct {
                    MyStruct { value: 42 }
                }
                
                fun use_struct(s: MyStruct): u64 {
                    s.value
                }
            }
        """.trimIndent())
        
        val element = file.findElementAt(myFixture.caretOffset)?.parent
        val usages = myFixture.findUsages(element!!)
        
        assertTrue("Should find usages in function return type", 
            usages.any { it.element?.text?.contains("MyStruct") == true })
        assertTrue("Should find usages in struct construction",
            usages.any { it.element?.parent?.text?.contains("MyStruct { value: 42 }") == true })
    }
    
    fun testFindUsagesOfVariable() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>my_var = 42;
                    let x = my_var + 1;
                    let y = my_var * 2;
                    if (my_var > 0) {
                        my_var
                    } else {
                        0
                    }
                }
            }
        """.trimIndent())
        
        val element = file.findElementAt(myFixture.caretOffset)?.parent
        val usages = myFixture.findUsages(element!!)
        
        assertEquals("Should find 4 usages of variable", 4, usages.size)
    }
    
    fun testFindUsagesAcrossModules() {
        myFixture.addFileToProject("other.move", """
            module 0x1::other {
                use 0x1::test::MyStruct;
                
                fun use_struct(): MyStruct {
                    MyStruct { value: 100 }
                }
            }
        """.trimIndent())
        
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                public struct <caret>MyStruct {
                    value: u64
                }
            }
        """.trimIndent())
        
        val element = file.findElementAt(myFixture.caretOffset)?.parent
        val usages = myFixture.findUsages(element!!)
        
        assertTrue("Should find usages across modules", usages.size >= 2)
    }
}
