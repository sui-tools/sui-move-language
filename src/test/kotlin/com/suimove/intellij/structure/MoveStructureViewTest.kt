package com.suimove.intellij.structure

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import javax.swing.JTree
import javax.swing.tree.TreePath

class MoveStructureViewTest : BasePlatformTestCase() {
    
    fun testModuleStructure() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test_module {
                struct TestStruct {
                    value: u64
                }
                
                public fun test_function(): u64 {
                    42
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain module", file.text.contains("module 0x1::test_module"))
        assertTrue("File should contain struct", file.text.contains("struct TestStruct"))
        assertTrue("File should contain function", file.text.contains("fun test_function"))
    }
    
    fun testMultipleModules() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::module1 {
                struct Struct1 {}
                public fun func1() {}
            }
            
            module 0x1::module2 {
                struct Struct2 {}
                public fun func2() {}
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain module1", file.text.contains("module 0x1::module1"))
        assertTrue("File should contain module2", file.text.contains("module 0x1::module2"))
        assertTrue("File should contain both structs", file.text.contains("Struct1") && file.text.contains("Struct2"))
    }
    
    fun testMixedContent() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::mixed {
                use 0x1::vector;
                
                const MAX_SIZE: u64 = 100;
                
                struct Container {
                    items: vector<u64>
                }
                
                public fun create(): Container {
                    Container { items: vector::empty() }
                }
                
                public fun add(c: &mut Container, item: u64) {
                    vector::push_back(&mut c.items, item);
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain use statement", file.text.contains("use 0x1::vector"))
        assertTrue("File should contain constant", file.text.contains("const MAX_SIZE"))
        assertTrue("File should contain struct", file.text.contains("struct Container"))
        assertTrue("File should contain functions", file.text.contains("fun create") && file.text.contains("fun add"))
    }
    
    fun testScriptStructure() {
        val file = myFixture.configureByText("test.move", """
            script {
                use 0x1::debug;
                
                fun main() {
                    debug::print(&42);
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain script", file.text.contains("script {"))
        assertTrue("File should contain main function", file.text.contains("fun main()"))
        assertTrue("File should contain use statement", file.text.contains("use 0x1::debug"))
    }
}
