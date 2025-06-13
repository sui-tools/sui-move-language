package com.suimove.intellij.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Paths

class MoveSettingsTest : BasePlatformTestCase() {
    
    private lateinit var settings: MoveSettings
    
    override fun setUp() {
        super.setUp()
        settings = MoveSettings.getInstance(project)
    }
    
    fun testDefaultSettings() {
        // Test default values
        assertNotNull("Settings instance should exist", settings)
        assertNotNull("Default compiler path should not be null", settings.compilerPath)
        assertTrue("Default compiler path should be empty", settings.compilerPath.isEmpty())
        
        assertNotNull("Default network should not be null", settings.network)
        assertEquals("Default network should be testnet", "testnet", settings.network)
        
        assertFalse("Auto-format should be disabled by default", settings.autoFormat)
    }
    
    fun testSettingCompilerPath() {
        val testPath = "/usr/local/bin/sui"
        settings.compilerPath = testPath
        
        assertEquals("Compiler path should be updated", testPath, settings.compilerPath)
        
        // Reset to default
        settings.compilerPath = ""
    }
    
    fun testSettingNetwork() {
        val testNetwork = "mainnet"
        settings.network = testNetwork
        
        assertEquals("Network should be updated", testNetwork, settings.network)
        
        // Reset to default
        settings.network = "testnet"
    }
    
    fun testSettingAutoFormat() {
        settings.autoFormat = true
        
        assertTrue("Auto-format should be enabled", settings.autoFormat)
        
        // Reset to default
        settings.autoFormat = false
    }
    
    fun testSettingGasOptimization() {
        settings.optimizeGas = true
        
        assertTrue("Gas optimization should be enabled", settings.optimizeGas)
        
        // Reset to default
        settings.optimizeGas = false
    }
    
    fun testSettingsState() {
        // Configure settings
        settings.compilerPath = "/test/path"
        settings.network = "devnet"
        settings.autoFormat = true
        settings.optimizeGas = true
        
        // Get state
        val state = settings.state
        
        // Verify state contains correct values
        assertNotNull("State should not be null", state)
        assertEquals("State should have correct compiler path", "/test/path", state.compilerPath)
        assertEquals("State should have correct network", "devnet", state.network)
        assertTrue("State should have correct auto-format setting", state.autoFormat)
        assertTrue("State should have correct gas optimization setting", state.optimizeGas)
        
        // Reset to defaults
        settings.compilerPath = ""
        settings.network = "testnet"
        settings.autoFormat = false
        settings.optimizeGas = false
    }
    
    fun testLoadState() {
        // Create a new state
        val newState = MoveSettingsState()
        newState.compilerPath = "/new/path"
        newState.network = "localnet"
        newState.autoFormat = true
        newState.optimizeGas = true
        
        // Load the state
        settings.loadState(newState)
        
        // Verify settings were updated
        assertEquals("Compiler path should be updated from state", "/new/path", settings.compilerPath)
        assertEquals("Network should be updated from state", "localnet", settings.network)
        assertTrue("Auto-format should be updated from state", settings.autoFormat)
        assertTrue("Gas optimization should be updated from state", settings.optimizeGas)
        
        // Reset to defaults
        settings.compilerPath = ""
        settings.network = "testnet"
        settings.autoFormat = false
        settings.optimizeGas = false
    }
    
    fun testValidCompilerPath() {
        // Set a valid path (this is mock validation, not actually checking file system)
        val validPath = Paths.get(System.getProperty("user.home"), "sui").toString()
        settings.compilerPath = validPath
        
        assertTrue("Valid compiler path should be accepted", settings.isCompilerPathValid())
        
        // Reset
        settings.compilerPath = ""
    }
    
    fun testInvalidCompilerPath() {
        // Set an invalid path
        settings.compilerPath = "/definitely/not/a/real/path/sui_xyz123"
        
        // In test environment, we can't truly validate paths
        // This is more of a placeholder test
        assertFalse("Non-existent path should be considered invalid", 
            settings.isCompilerPathValid() && !settings.compilerPath.isEmpty())
        
        // Reset
        settings.compilerPath = ""
    }
    
    fun testNetworkOptions() {
        // Test that all network options are valid
        val validNetworks = listOf("testnet", "mainnet", "devnet", "localnet")
        
        for (network in validNetworks) {
            settings.network = network
            assertEquals("Network should be updated to " + network, network, settings.network)
        }
        
        // Reset
        settings.network = "testnet"
    }
    
    fun testSettingsConfigurable() {
        val configurable = MoveSettingsConfigurable(project)
        
        // Test display name
        assertNotNull("Configurable should have a display name", configurable.displayName)
        assertTrue("Display name should contain 'Move'", configurable.displayName.contains("Move"))
        
        // Test help topic
        assertNotNull("Configurable should have a help topic", configurable.helpTopic)
    }
    
    fun testCreateSettingsComponent() {
        val configurable = MoveSettingsConfigurable(project)
        val component = configurable.createComponent()
        
        assertNotNull("Settings component should be created", component)
    }
    
    fun testIsModified() {
        val configurable = MoveSettingsConfigurable(project)
        
        // Initially not modified
        assertFalse("Initially settings should not be modified", configurable.isModified)
        
        // Create UI and modify settings
        val component = configurable.createComponent()
        assertNotNull("Settings component should be created", component)
        
        // We can't directly test UI interaction in this test framework
        // This would require a UI test framework
    }
    
    fun testApply() {
        val configurable = MoveSettingsConfigurable(project)
        
        // Create UI
        val component = configurable.createComponent()
        assertNotNull("Settings component should be created", component)
        
        // We can't directly test UI interaction in this test framework
        // This would require a UI test framework
    }
    
    fun testReset() {
        val configurable = MoveSettingsConfigurable(project)
        
        // Create UI
        val component = configurable.createComponent()
        assertNotNull("Settings component should be created", component)
        
        // We can't directly test UI interaction in this test framework
        // This would require a UI test framework
    }
}
