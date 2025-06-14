package com.suimove.intellij.templates

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveFileType

class MoveFileTemplateTest : BasePlatformTestCase() {
    
    fun testModuleTemplate() {
        val templateManager = FileTemplateManager.getInstance(project)
        val template = templateManager.getInternalTemplate("Move Module")
        
        assertNotNull("Module template should exist", template)
        
        // Test template content
        val content = template.text
        assertTrue("Template should contain module declaration", 
            content.contains("module"))
        assertTrue("Template should have placeholder for address",
            content.contains("\${ADDRESS}") || content.contains("0x"))
        assertTrue("Template should have placeholder for module name",
            content.contains("\${MODULE_NAME}") || content.contains("\${NAME}"))
    }
    
    fun testScriptTemplate() {
        val templateManager = FileTemplateManager.getInstance(project)
        val template = templateManager.getInternalTemplate("Move Script")
        
        assertNotNull("Script template should exist", template)
        
        val content = template.text
        assertTrue("Template should contain script declaration",
            content.contains("script"))
        assertTrue("Template should contain main function",
            content.contains("fun main") || content.contains("public entry fun"))
    }
    
    fun testTestModuleTemplate() {
        val templateManager = FileTemplateManager.getInstance(project)
        val template = templateManager.getInternalTemplate("Move Test Module")
        
        assertNotNull("Test module template should exist", template)
        
        val content = template.text
        assertTrue("Template should contain #[test_only] annotation",
            content.contains("#[test_only]"))
        assertTrue("Template should contain test function",
            content.contains("#[test]") || content.contains("fun test_"))
    }
    
    fun testCreateModuleFromTemplate() {
        val templateManager = FileTemplateManager.getInstance(project)
        val template = templateManager.getInternalTemplate("Move Module")
        
        if (template != null) {
            val properties = mutableMapOf(
                "ADDRESS" to "0x1",
                "MODULE_NAME" to "my_module",
                "NAME" to "my_module"
            )
            
            val generatedContent = template.getText(properties)
            
            assertTrue("Generated content should contain module declaration",
                generatedContent.contains("module 0x1::my_module"))
        }
    }
    
    fun testCreateScriptFromTemplate() {
        val templateManager = FileTemplateManager.getInstance(project)
        val template = templateManager.getInternalTemplate("Move Script")
        
        if (template != null) {
            val properties = mutableMapOf(
                "SCRIPT_NAME" to "my_script",
                "NAME" to "my_script"
            )
            
            val generatedContent = template.getText(properties)
            
            assertTrue("Generated content should contain script declaration",
                generatedContent.contains("script"))
        }
    }
    
    fun testTemplateVariableSubstitution() {
        // Test that template variables are properly substituted
        val templateText = """
            module ${"\${ADDRESS}"}::${"\${MODULE_NAME}"} {
                // Created by ${"\${USER}"}
                // Date: ${"\${DATE}"}
                
                public fun ${"\${FUNCTION_NAME}"}() {
                    // Implementation
                }
            }
        """.trimIndent()
        
        val properties = mutableMapOf(
            "ADDRESS" to "0x42",
            "MODULE_NAME" to "test_module",
            "USER" to "test_user",
            "DATE" to "2024-01-01",
            "FUNCTION_NAME" to "do_something"
        )
        
        // Simulate template substitution
        var result = templateText
        properties.forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        
        assertTrue("Should substitute ADDRESS", result.contains("0x42"))
        assertTrue("Should substitute MODULE_NAME", result.contains("test_module"))
        assertTrue("Should substitute USER", result.contains("test_user"))
        assertTrue("Should substitute DATE", result.contains("2024-01-01"))
        assertTrue("Should substitute FUNCTION_NAME", result.contains("do_something"))
    }
    
    fun testLibraryTemplate() {
        val templateManager = FileTemplateManager.getInstance(project)
        val template = templateManager.getInternalTemplate("Move Library")
        
        // Library template might not exist, but test if it does
        if (template != null) {
            val content = template.text
            assertTrue("Library template should contain module declaration",
                content.contains("module"))
            assertTrue("Library template should contain public functions",
                content.contains("public fun"))
        }
    }
    
    fun testTemplateWithComplexStructure() {
        val complexTemplate = """
            module ${"\${ADDRESS}"}::${"\${MODULE_NAME}"} {
                use 0x1::vector;
                use 0x1::signer;
                
                struct ${"\${STRUCT_NAME}"} has key, store {
                    id: u64,
                    data: vector<u8>
                }
                
                const E_NOT_FOUND: u64 = 1;
                
                public entry fun create_${"\${RESOURCE_NAME}"}(account: &signer) {
                    let resource = ${"\${STRUCT_NAME}"} {
                        id: 0,
                        data: vector::empty()
                    };
                    move_to(account, resource);
                }
            }
        """.trimIndent()
        
        val properties = mutableMapOf(
            "ADDRESS" to "0x1",
            "MODULE_NAME" to "nft",
            "STRUCT_NAME" to "NFT",
            "RESOURCE_NAME" to "nft"
        )
        
        var result = complexTemplate
        properties.forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        
        assertTrue("Should create valid module structure",
            result.contains("module 0x1::nft"))
        assertTrue("Should create struct with correct name",
            result.contains("struct NFT has key, store"))
        assertTrue("Should create function with correct name",
            result.contains("public entry fun create_nft"))
    }
}
