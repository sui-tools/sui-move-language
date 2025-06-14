package com.suimove.intellij.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Paths

class MoveSettingsTest : BasePlatformTestCase() {
    
    private lateinit var settings: MoveSettings
    
    override fun setUp() {
        super.setUp()
        settings = MoveSettings.instance
    }
    
    fun testDefaultSettings() {
        // Test default values
        assertNotNull("Settings instance should exist", settings)
        assertNotNull("Default Sui CLI path should not be null", settings.suiCliPath)
        assertTrue("Default Sui CLI path should be empty", settings.suiCliPath.isEmpty())
    }
    
    fun testSettingSuiCliPath() {
        val testPath = "/usr/local/bin/sui"
        settings.suiCliPath = testPath
        
        assertEquals("Sui CLI path should be updated", testPath, settings.suiCliPath)
        
        // Reset to default
        settings.suiCliPath = ""
    }
    
    fun testSettingsState() {
        // Configure settings
        settings.suiCliPath = "/test/path"
        
        // Get state
        val state = settings.state
        
        // Verify state contains correct values
        assertNotNull("State should not be null", state)
        assertEquals("State should have correct Sui CLI path", "/test/path", state.suiCliPath)
        
        // Reset to defaults
        settings.suiCliPath = ""
    }
    
    fun testLoadState() {
        // Create a new state
        val newState = MoveSettings()
        newState.suiCliPath = "/new/path"
        
        // Load the state
        settings.loadState(newState)
        
        // Verify settings were updated
        assertEquals("Sui CLI path should be updated from state", "/new/path", settings.suiCliPath)
        
        // Reset to defaults
        settings.suiCliPath = ""
    }
    
    fun testSettingsConfigurable() {
        val configurable = MoveSettingsConfigurable()
        
        // Test display name
        assertNotNull("Configurable should have a display name", configurable.displayName)
        assertTrue("Display name should contain 'Move'", configurable.displayName.contains("Move"))
        
        // Test help topic
        assertNotNull("Configurable should have a help topic", configurable.helpTopic)
    }
    
    fun testCreateSettingsComponent() {
        val configurable = MoveSettingsConfigurable()
        val component = configurable.createComponent()
        
        assertNotNull("Settings component should be created", component)
    }
    
    fun testIsModified() {
        val configurable = MoveSettingsConfigurable()
        
        // Initially not modified
        assertFalse("Initially settings should not be modified", configurable.isModified)
        
        // Create UI and modify settings
        val component = configurable.createComponent()
        assertNotNull("Settings component should be created", component)
        
        // We can't directly test UI interaction in this test framework
        // This would require a UI test framework
    }
    
    fun testApply() {
        val configurable = MoveSettingsConfigurable()
        
        // Create UI
        val component = configurable.createComponent()
        assertNotNull("Settings component should be created", component)
        
        // We can't directly test UI interaction in this test framework
        // This would require a UI test framework
    }
    
    fun testReset() {
        val configurable = MoveSettingsConfigurable()
        
        // Create UI
        val component = configurable.createComponent()
        assertNotNull("Settings component should be created", component)
        
        // We can't directly test UI interaction in this test framework
        // This would require a UI test framework
    }
}
