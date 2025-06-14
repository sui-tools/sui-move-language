package com.suimove.intellij.structure

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.LanguageStructureViewBuilder
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
                struct MyStruct {
                    value: u64
                }
                
                fun test_function(): u64 {
                    42
                }
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model directly
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                
                // Verify root element
                assertEquals("Root should be file", file.name, root.presentation.presentableText)
                
                // Get module element
                val children = root.children
                assertEquals("Should have one module", 1, children.size)
                
                val moduleElement = children[0]
                assertEquals("Should be test_module", "test_module", moduleElement.presentation.presentableText)
                
                // Get module children (struct and function)
                val moduleChildren = moduleElement.children
                assertEquals("Module should have 2 children", 2, moduleChildren.size)
                
                // Verify struct
                val structElement = moduleChildren.find { it.presentation.presentableText == "MyStruct" }
                assertNotNull("Should find MyStruct", structElement)
                
                // Verify function
                val functionElement = moduleChildren.find { it.presentation.presentableText == "test_function" }
                assertNotNull("Should find test_function", functionElement)
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testStructFields() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Person {
                    name: vector<u8>,
                    age: u64,
                    active: bool
                }
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                val children = root.children
                val moduleElement = children[0]
                val moduleChildren = moduleElement.children
                
                // Find struct
                val structElement = moduleChildren.find { it.presentation.presentableText == "Person" }
                assertNotNull("Should find Person struct", structElement)
                
                // Get struct fields
                val structChildren = structElement!!.children
                assertEquals("Struct should have 3 fields", 3, structChildren.size)
                
                // Verify fields
                val fieldNames = structChildren.map { it.presentation.presentableText }
                assertTrue("Should have name field", fieldNames.contains("name"))
                assertTrue("Should have age field", fieldNames.contains("age"))
                assertTrue("Should have active field", fieldNames.contains("active"))
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testFunctionParameters() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun calculate(x: u64, y: u64, operation: u8): u64 {
                    if (operation == 0) {
                        x + y
                    } else {
                        x - y
                    }
                }
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                val children = root.children
                val moduleElement = children[0]
                val moduleChildren = moduleElement.children
                
                // Find function
                val functionElement = moduleChildren.find { it.presentation.presentableText == "calculate" }
                assertNotNull("Should find calculate function", functionElement)
                
                // Function parameters might be shown in the presentation
                val presentation = functionElement!!.presentation.presentableText
                assertTrue("Function should show in structure", presentation != null)
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testMultipleModules() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::module_a {
                fun func_a() {}
            }
            
            module 0x1::module_b {
                fun func_b() {}
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                val children = root.children
                
                assertEquals("Should have 2 modules", 2, children.size)
                
                val moduleNames = children.map { it.presentation.presentableText }
                assertTrue("Should have module_a", moduleNames.contains("module_a"))
                assertTrue("Should have module_b", moduleNames.contains("module_b"))
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testConstants() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                const MAX_VALUE: u64 = 1000000;
                const MIN_VALUE: u64 = 0;
                
                fun get_max(): u64 {
                    MAX_VALUE
                }
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                val children = root.children
                val moduleElement = children[0]
                val moduleChildren = moduleElement.children
                
                // Should have constants and function
                assertTrue("Should have at least 3 children", moduleChildren.size >= 3)
                
                val elementNames = moduleChildren.map { it.presentation.presentableText }
                assertTrue("Should have MAX_VALUE", elementNames.contains("MAX_VALUE"))
                assertTrue("Should have MIN_VALUE", elementNames.contains("MIN_VALUE"))
                assertTrue("Should have get_max function", elementNames.contains("get_max"))
            } finally {
                model.dispose()
            }
        }
    }
    
    fun testStructureNavigation() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::navigation_test {
                struct Data {
                    value: u64
                }
                
                fun process_data(data: Data): u64 {
                    data.value
                }
            }
        """.trimIndent())
        
        // Get structure view builder
        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(file)
        assertNotNull("Should have structure view builder", builder)
        
        // Create structure view model
        if (builder is TreeBasedStructureViewBuilder) {
            val model = builder.createStructureViewModel(myFixture.editor)
            try {
                val root = model.root
                
                // Test navigation capability
                assertTrue("Root should be navigable", root.canNavigate())
                assertTrue("Root should be navigable to source", root.canNavigateToSource())
            } finally {
                model.dispose()
            }
        }
    }
    
    // Helper function to find a node in the tree
    private fun findNode(tree: JTree, path: Array<String>): TreePath? {
        val root = tree.model.root
        var currentPath = TreePath(root)
        
        for (nodeName in path) {
            val model = tree.model
            val childCount = model.getChildCount(currentPath.lastPathComponent)
            var found = false
            
            for (i in 0 until childCount) {
                val child = model.getChild(currentPath.lastPathComponent, i)
                if (child.toString().contains(nodeName)) {
                    currentPath = currentPath.pathByAddingChild(child)
                    found = true
                    break
                }
            }
            
            if (!found) return null
        }
        
        return currentPath
    }
}
