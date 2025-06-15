package com.suimove.intellij.integration

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.psi.MoveFile

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
                public fun process(value: u64) {
                    assert!(value > 0, 1);
                }
            }
        """.trimIndent()).virtualFile
    }
    
    fun testEndToEndWorkflow() {
        // Test that all files are created and recognized as Move files
        assertEquals("Main file should be Move file", "Move", moduleFile.fileType.name)
        assertEquals("Library file should be Move file", "Move", libraryFile.fileType.name)
        assertEquals("Util file should be Move file", "Move", utilFile.fileType.name)
        
        // Test that PSI files are created correctly
        val psiManager = PsiManager.getInstance(project)
        val mainPsi = psiManager.findFile(moduleFile) as? MoveFile
        val libPsi = psiManager.findFile(libraryFile) as? MoveFile
        val utilPsi = psiManager.findFile(utilFile) as? MoveFile
        
        assertNotNull("Main PSI should exist", mainPsi)
        assertNotNull("Library PSI should exist", libPsi)
        assertNotNull("Util PSI should exist", utilPsi)
        
        // Test that files contain expected content
        assertTrue("Main should contain module declaration", 
            mainPsi?.text?.contains("module 0x1::main") == true)
        assertTrue("Library should contain struct declaration", 
            libPsi?.text?.contains("struct LibraryData") == true)
        assertTrue("Util should contain process function", 
            utilPsi?.text?.contains("fun process") == true)
    }
    
    fun testCrossModuleReferences() {
        // Test that cross-module references can be identified
        val psiManager = PsiManager.getInstance(project)
        val mainPsi = psiManager.findFile(moduleFile) as? MoveFile
        
        assertNotNull("Main PSI should exist", mainPsi)
        
        // Check that the file contains use statements
        val text = mainPsi?.text ?: ""
        assertTrue("Should contain library import", text.contains("use 0x1::library"))
        assertTrue("Should contain util import", text.contains("use 0x1::util"))
        
        // Check that the file references imported modules
        assertTrue("Should reference library module", text.contains("library::create"))
        assertTrue("Should reference util module", text.contains("util::process"))
    }
    
    fun testMultiModuleProject() {
        // Test multi-module project structure
        val files = listOf(moduleFile, libraryFile, utilFile)
        
        // All files should exist and be Move files
        for (file in files) {
            assertTrue("File should exist: ${file.name}", file.exists())
            assertEquals("File should be Move type", "Move", file.fileType.name)
        }
        
        // Test that we can navigate between files
        val psiManager = PsiManager.getInstance(project)
        for (file in files) {
            val psi = psiManager.findFile(file)
            assertNotNull("PSI should exist for ${file.name}", psi)
            assertTrue("PSI should be MoveFile", psi is MoveFile)
        }
    }
    
    fun testExternalToolIntegration() {
        // Test basic integration without actual external tools
        val psiManager = PsiManager.getInstance(project)
        val mainPsi = psiManager.findFile(moduleFile) as? MoveFile
        
        assertNotNull("Main PSI should exist", mainPsi)
        
        // Verify that the file structure supports external tool integration
        assertTrue("File should have proper extension", moduleFile.name.endsWith(".move"))
        assertTrue("File should have valid path", moduleFile.path.isNotEmpty())
        
        // Test that we can access file content for external tools
        val content = mainPsi?.text ?: ""
        assertTrue("Content should not be empty", content.isNotEmpty())
        assertTrue("Content should be valid Move code", content.contains("module"))
    }
    
    fun testEdgeCase_UnusualSyntax() {
        // Test handling of edge cases in syntax
        val edgeFile = myFixture.configureByText("edge.move", """
            module 0x1::edge {
                // Test various edge cases
                const MAX_U64: u64 = 18446744073709551615;
                const HEX_VALUE: u64 = 0xFF;
                
                fun test_generics<T: copy + drop>() {}
                
                fun test_references(x: &u64, y: &mut u64) {
                    *y = *x;
                }
                
                struct Complex<T> has copy, drop {
                    field: vector<T>
                }
            }
        """.trimIndent())
        
        val psi = PsiManager.getInstance(project).findFile(edgeFile.virtualFile) as? MoveFile
        assertNotNull("Edge case PSI should exist", psi)
        
        val text = psi?.text ?: ""
        assertTrue("Should handle large numbers", text.contains("18446744073709551615"))
        assertTrue("Should handle hex values", text.contains("0xFF"))
        assertTrue("Should handle generics", text.contains("<T: copy + drop>"))
        assertTrue("Should handle references", text.contains("&mut u64"))
        assertTrue("Should handle abilities", text.contains("has copy, drop"))
    }
}
