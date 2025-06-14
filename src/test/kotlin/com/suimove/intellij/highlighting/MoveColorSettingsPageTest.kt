package com.suimove.intellij.highlighting

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.suimove.intellij.MoveIcons

/**
 * Tests for Move color settings page
 */
class MoveColorSettingsPageTest {
    
    @Test
    fun `test color settings page can be created`() {
        val page = MoveColorSettingsPage()
        assertNotNull(page, "Color settings page should be instantiated")
    }
    
    @Test
    fun `test display name`() {
        val page = MoveColorSettingsPage()
        assertEquals("Move", page.displayName, "Display name should be 'Move'")
    }
    
    @Test
    fun `test icon`() {
        val page = MoveColorSettingsPage()
        assertEquals(MoveIcons.FILE, page.icon, "Icon should be Move file icon")
    }
    
    @Test
    fun `test attribute descriptors`() {
        val page = MoveColorSettingsPage()
        val descriptors = page.attributeDescriptors
        assertNotNull(descriptors, "Attribute descriptors should not be null")
        assertTrue(descriptors.isNotEmpty(), "Should have attribute descriptors")
        
        // Check for some expected descriptors
        val descriptorNames = descriptors.map { it.displayName }
        assertTrue(descriptorNames.contains("Keyword"), "Should have Keyword descriptor")
        assertTrue(descriptorNames.contains("String"), "Should have String descriptor")
        assertTrue(descriptorNames.contains("Number"), "Should have Number descriptor")
        assertTrue(descriptorNames.contains("Comment"), "Should have Comment descriptor")
    }
    
    @Test
    fun `test color descriptor map`() {
        val page = MoveColorSettingsPage()
        val colorMap = page.additionalHighlightingTagToDescriptorMap
        
        if (colorMap != null) {
            // Check for expected mappings
            assertTrue(colorMap.containsKey("keyword"), "Should have keyword mapping")
            assertTrue(colorMap.containsKey("string"), "Should have string mapping")
            assertTrue(colorMap.containsKey("number"), "Should have number mapping")
        }
    }
    
    @Test
    fun `test demo text`() {
        val page = MoveColorSettingsPage()
        val demoText = page.demoText
        assertNotNull(demoText, "Demo text should not be null")
        assertTrue(demoText.isNotEmpty(), "Demo text should not be empty")
        
        // Check that demo text contains Move code
        assertTrue(demoText.contains("module"), "Demo text should contain 'module' keyword")
        assertTrue(demoText.contains("fun"), "Demo text should contain 'fun' keyword")
        assertTrue(demoText.contains("struct"), "Demo text should contain 'struct' keyword")
    }
    
    @Test
    fun `test highlighter`() {
        val page = MoveColorSettingsPage()
        val highlighter = page.highlighter
        assertNotNull(highlighter, "Highlighter should not be null")
        assertTrue(highlighter is MoveSyntaxHighlighter, "Should be Move syntax highlighter")
    }
}
