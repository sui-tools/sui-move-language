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
        
        // Get structure view builder
        val builder = MoveStructureViewFactory().getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                
                // Verify root element
                assertEquals("Root should be file", file.name, root.presentation.presentableText)
                
                // Since the current implementation only shows direct file children,
                // we should have at least one child (the module)
                val children = root.children
                assertTrue("Should have at least one child", children.isNotEmpty())
                
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testEmptyFile() {
        val file = myFixture.configureByText("empty.move", "")
        
        // Get structure view builder
        val builder = MoveStructureViewFactory().getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                assertEquals("Root should be file", file.name, root.presentation.presentableText)
                
                // Empty file should have no children
                val children = root.children
                assertEquals("Should have no children", 0, children.size)
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testMultipleModules() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::first {
                struct FirstStruct {}
            }
            
            module 0x1::second {
                struct SecondStruct {}
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = MoveStructureViewFactory().getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                val children = root.children
                
                // Should have at least 2 children (the two modules)
                assertTrue("Should have at least 2 children", children.size >= 2)
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testScriptStructure() {
        val file = myFixture.configureByText("test.move", """
            script {
                use 0x1::Signer;
                
                public entry fun main(account: &signer) {
                    // Script body
                }
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = MoveStructureViewFactory().getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                val children = root.children
                
                // Should have at least one child (the script)
                assertTrue("Should have at least one child", children.isNotEmpty())
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testMixedContent() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::my_module {
                struct MyStruct {}
            }
            
            script {
                fun main() {}
            }
            
            // Some comments
        """.trimIndent())
        
        // Get structure view builder
        val builder = MoveStructureViewFactory().getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                val children = root.children
                
                // Should have at least 2 children (module and script)
                assertTrue("Should have at least 2 children", children.size >= 2)
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testStructureViewModel() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                const MAX: u64 = 100;
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = MoveStructureViewFactory().getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                // Test basic model properties
                assertNotNull("Should have root", model.root)
                
                // Test filters and sorters (should be empty for now)
                val filters = model.filters
                val sorters = model.sorters
                assertNotNull("Should have filters array", filters)
                assertNotNull("Should have sorters array", sorters)
            } finally {
                model.dispose()
            }
        }
    }
}
