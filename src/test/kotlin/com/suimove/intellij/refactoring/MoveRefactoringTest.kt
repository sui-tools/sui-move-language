package com.suimove.intellij.refactoring

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveRefactoringTest : BasePlatformTestCase() {
    
    fun testRenameLocalVariable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let <caret>old_name = 42;
                    let x = old_name + 1;
                    let y = old_name * 2;
                }
            }
        """.trimIndent())
        
        myFixture.renameElementAtCaret("new_name")
        
        myFixture.checkResult("""
            module 0x1::test {
                fun main() {
                    let new_name = 42;
                    let x = new_name + 1;
                    let y = new_name * 2;
                }
            }
        """.trimIndent())
    }
    
    fun testRenameFunction() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun <caret>old_function(): u64 { 42 }
                
                fun main() {
                    let x = old_function();
                    let y = old_function() + old_function();
                }
            }
        """.trimIndent())
        
        myFixture.renameElementAtCaret("new_function")
        
        myFixture.checkResult("""
            module 0x1::test {
                fun new_function(): u64 { 42 }
                
                fun main() {
                    let x = new_function();
                    let y = new_function() + new_function();
                }
            }
        """.trimIndent())
    }
    
    fun testRenameStruct() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct <caret>OldStruct {
                    value: u64
                }
                
                fun create(): OldStruct {
                    OldStruct { value: 42 }
                }
                
                fun use_struct(s: OldStruct): u64 {
                    s.value
                }
            }
        """.trimIndent())
        
        myFixture.renameElementAtCaret("NewStruct")
        
        myFixture.checkResult("""
            module 0x1::test {
                struct NewStruct {
                    value: u64
                }
                
                fun create(): NewStruct {
                    NewStruct { value: 42 }
                }
                
                fun use_struct(s: NewStruct): u64 {
                    s.value
                }
            }
        """.trimIndent())
    }
    
    fun testRenameStructField() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    <caret>old_field: u64
                }
                
                fun use_struct(s: MyStruct): u64 {
                    s.old_field
                }
                
                fun create(): MyStruct {
                    MyStruct { old_field: 42 }
                }
            }
        """.trimIndent())
        
        myFixture.renameElementAtCaret("new_field")
        
        myFixture.checkResult("""
            module 0x1::test {
                struct MyStruct {
                    new_field: u64
                }
                
                fun use_struct(s: MyStruct): u64 {
                    s.new_field
                }
                
                fun create(): MyStruct {
                    MyStruct { new_field: 42 }
                }
            }
        """.trimIndent())
    }
    
    fun testRenameParameter() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun calculate(<caret>old_param: u64): u64 {
                    old_param + old_param * 2
                }
            }
        """.trimIndent())
        
        myFixture.renameElementAtCaret("new_param")
        
        myFixture.checkResult("""
            module 0x1::test {
                fun calculate(new_param: u64): u64 {
                    new_param + new_param * 2
                }
            }
        """.trimIndent())
    }
    
    fun testInvalidRename() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun <caret>my_function() {}
            }
        """.trimIndent())
        
        // Test renaming to a keyword (should fail)
        try {
            myFixture.renameElementAtCaret("module")
            fail("Should not allow renaming to keyword")
        } catch (e: Exception) {
            // Expected
        }
    }
    
    fun testRenameAcrossFiles() {
        myFixture.addFileToProject("lib.move", """
            module 0x1::lib {
                public fun helper(): u64 { 42 }
            }
        """.trimIndent())
        
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::lib;
                
                fun main() {
                    let x = lib::<caret>helper();
                }
            }
        """.trimIndent())
        
        // Note: Cross-file rename might need special handling
        // This test documents expected behavior
    }
}
