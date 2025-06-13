package com.suimove.intellij.integration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.psi.MoveFile
import org.mockito.Mockito.*

class MoveIntegrationTest : BasePlatformTestCase() {
    
    // Test files for multi-module project
    private lateinit var moduleFile: VirtualFile
    private lateinit var libraryFile: VirtualFile
    private lateinit var utilFile: VirtualFile
    
    override fun setUp() {
        super.setUp()
        
        // Create a multi-module project structure
        moduleFile = myFixture.configureByText("main.move", """
            module 0x1::main {
                use 0x1::library;
                use 0x1::util;
                
                fun main() {
                    let lib = library::create();
                    let value = library::get_value(&lib);
                    util::process(value);
                }
            }
        """.trimIndent()).virtualFile
        
        libraryFile = myFixture.addFileToProject("library.move", """
            module 0x1::library {
                struct LibraryData {
                    value: u64
                }
                
                public fun create(): LibraryData {
                    LibraryData { value: 42 }
                }
                
                public fun get_value(data: &LibraryData): u64 {
                    data.value
                }
            }
        """.trimIndent()).virtualFile
        
        utilFile = myFixture.addFileToProject("util.move", """
            module 0x1::util {
                public fun process(value: u64): u64 {
                    value * 2
                }
            }
        """.trimIndent()).virtualFile
    }
    
    fun testCrossModuleReferences() {
        // Open the main file
        myFixture.openFileInEditor(moduleFile)
        
        // Perform highlighting to trigger reference resolution
        val highlightInfos = myFixture.doHighlighting()
        
        // Verify no errors
        val errors = highlightInfos.filter { it.severity.name == "ERROR" }
        assertEquals("Should have no errors in cross-module references", 0, errors.size)
        
        // Test navigation to library module
        val mainFile = PsiManager.getInstance(project).findFile(moduleFile) as MoveFile
        val libraryReference = mainFile.findElementAt(myFixture.editor.document.text.indexOf("library::create"))
        
        assertNotNull("Should find library reference", libraryReference)
        
        // Test find usages across modules
        myFixture.openFileInEditor(libraryFile)
        val libraryFile = PsiManager.getInstance(project).findFile(libraryFile) as MoveFile
        val createFunction = libraryFile.findElementAt(libraryFile.text.indexOf("create(): LibraryData"))
        
        assertNotNull("Should find create function", createFunction)
        
        // Find usages should include the reference in main.move
        val usages = myFixture.findUsages(createFunction!!)
        assertTrue("Should find usage in main module", usages.isNotEmpty())
    }
    
    fun testEndToEndWorkflow() {
        // 1. Create a new file
        val newFile = myFixture.addFileToProject("new_module.move", """
            module 0x1::new_module {
                fun test_function() {
                    let x = 42;
                }
            }
        """.trimIndent()).virtualFile
        
        // 2. Open the file
        myFixture.openFileInEditor(newFile)
        
        // 3. Make changes to the file
        myFixture.type("\n    fun another_function() {}\n")
        
        // 4. Verify changes
        assertTrue("File should contain the new function", 
            myFixture.editor.document.text.contains("another_function"))
        
        // 5. Perform code completion
        myFixture.editor.caretModel.moveToOffset(myFixture.editor.document.text.indexOf("another_function") + 20)
        myFixture.type("\n        let y = ")
        val completions = myFixture.completeBasic()
        assertNotNull("Should have completions available", completions)
        
        // 6. Apply intention action
        myFixture.configureByText("intention_test.move", """
            module 0x1::intention_test {
                fun test() {
                    let <caret>z = 42;
                }
            }
        """.trimIndent())
        
        val intention = myFixture.findSingleIntention("Add type annotation")
        myFixture.launchAction(intention)
        
        assertTrue("Should apply intention action", 
            myFixture.editor.document.text.contains("z: u64"))
    }
    
    fun testMultiModuleProject() {
        // Create a more complex multi-module project
        for (i in 1..5) {
            myFixture.addFileToProject("module$i.move", """
                module 0x1::module$i {
                    struct Data$i {
                        value: u64
                    }
                    
                    public fun create$i(): Data$i {
                        Data$i { value: $i }
                    }
                    
                    public fun process$i(data: &Data$i): u64 {
                        data.value * $i
                    }
                }
            """.trimIndent())
        }
        
        // Create an integration module that uses all the others
        val integrationFile = myFixture.addFileToProject("integration.move", """
            module 0x1::integration {
                ${(1..5).joinToString("\n                ") { "use 0x1::module$it;" }}
                
                fun integrate() {
                    ${(1..5).joinToString("\n                    ") { 
                        "let data$it = module$it::create$it();" 
                    }}
                    
                    ${(1..5).joinToString("\n                    ") { 
                        "let result$it = module$it::process$it(&data$it);" 
                    }}
                    
                    let total = ${(1..5).joinToString(" + ") { "result$it" }};
                }
            }
        """.trimIndent()).virtualFile
        
        // Open the integration file
        myFixture.openFileInEditor(integrationFile)
        
        // Perform highlighting to trigger reference resolution
        val highlightInfos = myFixture.doHighlighting()
        
        // Verify no errors
        val errors = highlightInfos.filter { it.severity.name == "ERROR" }
        assertEquals("Should have no errors in multi-module project", 0, errors.size)
        
        // Test refactoring across modules
        myFixture.openFileInEditor(myFixture.findFileInTempDir("module1.move"))
        
        // Rename a function and check if references are updated
        myFixture.renameElementAtCaret("create1_renamed")
        
        // Open the integration file again to check if the reference was updated
        myFixture.openFileInEditor(integrationFile)
        assertTrue("Reference should be updated after rename", 
            myFixture.editor.document.text.contains("module1::create1_renamed"))
    }
    
    fun testExternalToolIntegration() {
        // Mock compiler service
        val compilerService = mock(com.suimove.intellij.compiler.MoveCompilerService::class.java)
        
        // Create a test file
        val testFile = myFixture.configureByText("compiler_test.move", """
            module 0x1::compiler_test {
                fun test() {
                    let x = 42;
                }
            }
        """.trimIndent()).virtualFile
        
        // Mock successful compilation
        `when`(compilerService.buildProject(any())).thenReturn(
            com.suimove.intellij.compiler.MoveCompilerResult(true, "Build successful", emptyList())
        )
        
        // Mock error compilation
        `when`(compilerService.buildProject(eq("/error/path"))).thenReturn(
            com.suimove.intellij.compiler.MoveCompilerResult(
                false, 
                "Build failed", 
                listOf(com.suimove.intellij.compiler.MoveCompilerError(
                    "compiler_test.move", 
                    3, 
                    "Type error", 
                    "ERROR"
                ))
            )
        )
        
        // Test successful build
        val successResult = compilerService.buildProject(testFile.path)
        assertTrue("Build should succeed", successResult.success)
        
        // Test failed build
        val errorResult = compilerService.buildProject("/error/path")
        assertFalse("Build should fail", errorResult.success)
        assertEquals("Should have one error", 1, errorResult.errors.size)
        assertEquals("Should have correct error message", "Type error", errorResult.errors[0].message)
    }
    
    fun testEdgeCase_UnusualSyntax() {
        // Test file with unusual syntax combinations
        val unusualFile = myFixture.configureByText("unusual.move", """
            module 0x1::unusual {
                // Nested generic types
                struct Complex<T> {
                    data: vector<vector<vector<T>>>
                }
                
                // Function with many generic parameters
                fun generic_madness<T1, T2, T3, T4, T5>() {}
                
                // Deeply nested blocks
                fun nested_blocks() {
                    if (true) {
                        if (true) {
                            if (true) {
                                if (true) {
                                    if (true) {
                                        // Deep nesting
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Unusual but valid identifiers
                fun _unusual_name_with_underscores_123() {}
            }
        """.trimIndent())
        
        // Perform highlighting
        val highlightInfos = myFixture.doHighlighting()
        
        // Verify no errors
        val errors = highlightInfos.filter { it.severity.name == "ERROR" }
        assertEquals("Should handle unusual syntax without errors", 0, errors.size)
    }
}
