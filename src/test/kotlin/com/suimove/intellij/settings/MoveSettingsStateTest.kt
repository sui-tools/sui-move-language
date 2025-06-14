package com.suimove.intellij.settings

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for Move settings state
 */
class MoveSettingsStateTest {
    
    @Test
    fun `test settings can be created with defaults`() {
        val settings = MoveSettings()
        assertNotNull(settings, "Settings should be instantiated")
        assertEquals("", settings.suiCliPath, "Default sui CLI path should be empty")
    }
    
    @Test
    fun `test settings can be modified`() {
        val settings = MoveSettings()
        val newPath = "/usr/local/bin/sui"
        settings.suiCliPath = newPath
        assertEquals(newPath, settings.suiCliPath, "Settings should be modifiable")
    }
    
    @Test
    fun `test settings state can be loaded`() {
        val settings = MoveSettings()
        val state = MoveSettings()
        state.suiCliPath = "/custom/path/sui"
        
        settings.loadState(state)
        assertEquals("/custom/path/sui", settings.suiCliPath, "Settings should load from state")
    }
    
    @Test
    fun `test settings state can be saved`() {
        val settings = MoveSettings()
        settings.suiCliPath = "/another/path/sui"
        
        val state = settings
        assertNotNull(state, "State should not be null")
        assertEquals("/another/path/sui", state.suiCliPath, "State should contain current settings")
    }
    
    @Test
    fun `test settings copy state`() {
        val original = MoveSettings()
        original.suiCliPath = "/original/path"
        
        val copy = MoveSettings()
        copy.loadState(original)
        
        assertEquals(original.suiCliPath, copy.suiCliPath, "Copied settings should match original")
        
        // Modify copy shouldn't affect original
        copy.suiCliPath = "/modified/path"
        assertNotEquals(original.suiCliPath, copy.suiCliPath, "Original should not be affected by copy modification")
    }
    
    @Test
    fun `test settings default state`() {
        val settings = MoveSettings()
        val defaultState = settings
        
        assertNotNull(defaultState, "Default state should not be null")
        assertEquals("", defaultState.suiCliPath, "Default state should have default values")
    }
}
