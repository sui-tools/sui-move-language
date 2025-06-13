package com.suimove.intellij.analysis

import com.intellij.testFramework.UsefulTestCase

class MoveTypeSystemTest : UsefulTestCase() {
    
    private lateinit var typeSystem: MoveTypeSystem
    
    override fun setUp() {
        super.setUp()
        typeSystem = MoveTypeSystem()
    }
    
    fun testPrimitiveTypes() {
        assertTrue("Should recognize u8 as primitive", typeSystem.isPrimitiveType("u8"))
        assertTrue("Should recognize u64 as primitive", typeSystem.isPrimitiveType("u64"))
        assertTrue("Should recognize u128 as primitive", typeSystem.isPrimitiveType("u128"))
        assertTrue("Should recognize bool as primitive", typeSystem.isPrimitiveType("bool"))
        assertTrue("Should recognize address as primitive", typeSystem.isPrimitiveType("address"))
        assertTrue("Should recognize signer as primitive", typeSystem.isPrimitiveType("signer"))
        
        assertFalse("Should not recognize vector as primitive", typeSystem.isPrimitiveType("vector"))
        assertFalse("Should not recognize custom type as primitive", typeSystem.isPrimitiveType("MyStruct"))
    }
    
    fun testTypeCompatibility() {
        // Same types are compatible
        assertTrue("u64 should be compatible with u64", 
            typeSystem.isCompatible("u64", "u64"))
        assertTrue("bool should be compatible with bool", 
            typeSystem.isCompatible("bool", "bool"))
        
        // Different types are not compatible
        assertFalse("u64 should not be compatible with bool", 
            typeSystem.isCompatible("u64", "bool"))
        assertFalse("address should not be compatible with u64", 
            typeSystem.isCompatible("address", "u64"))
    }
    
    fun testNumericTypeCompatibility() {
        // Numeric types are not implicitly convertible in Move
        assertFalse("u8 should not be compatible with u64", 
            typeSystem.isCompatible("u8", "u64"))
        assertFalse("u64 should not be compatible with u128", 
            typeSystem.isCompatible("u64", "u128"))
    }
    
    fun testVectorTypes() {
        assertTrue("Should recognize vector<u64> as vector type", 
            typeSystem.isVectorType("vector<u64>"))
        assertTrue("Should recognize vector<bool> as vector type", 
            typeSystem.isVectorType("vector<bool>"))
        assertTrue("Should recognize nested vector", 
            typeSystem.isVectorType("vector<vector<u8>>"))
        
        assertFalse("Should not recognize non-vector as vector", 
            typeSystem.isVectorType("u64"))
    }
    
    fun testVectorElementType() {
        assertEquals("Should extract u64 from vector<u64>", 
            "u64", typeSystem.getVectorElementType("vector<u64>"))
        assertEquals("Should extract bool from vector<bool>", 
            "bool", typeSystem.getVectorElementType("vector<bool>"))
        assertEquals("Should extract nested vector type", 
            "vector<u8>", typeSystem.getVectorElementType("vector<vector<u8>>"))
        
        assertNull("Should return null for non-vector", 
            typeSystem.getVectorElementType("u64"))
    }
    
    fun testVectorTypeCompatibility() {
        assertTrue("vector<u64> should be compatible with vector<u64>", 
            typeSystem.isCompatible("vector<u64>", "vector<u64>"))
        
        assertFalse("vector<u64> should not be compatible with vector<bool>", 
            typeSystem.isCompatible("vector<u64>", "vector<bool>"))
        assertFalse("vector<u64> should not be compatible with u64", 
            typeSystem.isCompatible("vector<u64>", "u64"))
    }
    
    fun testGenericTypes() {
        assertTrue("Should recognize T as generic", 
            typeSystem.isGenericType("T"))
        assertTrue("Should recognize T1 as generic", 
            typeSystem.isGenericType("T1"))
        assertTrue("Should recognize Type as generic", 
            typeSystem.isGenericType("Type"))
        
        assertFalse("Should not recognize u64 as generic", 
            typeSystem.isGenericType("u64"))
        assertFalse("Should not recognize vector as generic", 
            typeSystem.isGenericType("vector"))
    }
    
    fun testStructTypes() {
        // Register some struct types
        typeSystem.registerStruct("MyStruct", listOf("value" to "u64", "flag" to "bool"))
        typeSystem.registerStruct("Point", listOf("x" to "u64", "y" to "u64"))
        
        assertTrue("Should recognize registered struct", 
            typeSystem.isStructType("MyStruct"))
        assertTrue("Should recognize Point as struct", 
            typeSystem.isStructType("Point"))
        
        assertFalse("Should not recognize unregistered type as struct", 
            typeSystem.isStructType("Unknown"))
        assertFalse("Should not recognize primitive as struct", 
            typeSystem.isStructType("u64"))
    }
    
    fun testStructFieldTypes() {
        typeSystem.registerStruct("MyStruct", listOf("value" to "u64", "flag" to "bool"))
        
        assertEquals("Should get correct field type", 
            "u64", typeSystem.getStructFieldType("MyStruct", "value"))
        assertEquals("Should get correct field type", 
            "bool", typeSystem.getStructFieldType("MyStruct", "flag"))
        
        assertNull("Should return null for unknown field", 
            typeSystem.getStructFieldType("MyStruct", "unknown"))
        assertNull("Should return null for unknown struct", 
            typeSystem.getStructFieldType("Unknown", "value"))
    }
    
    fun testReferenceTypes() {
        assertTrue("Should recognize &u64 as reference", 
            typeSystem.isReferenceType("&u64"))
        assertTrue("Should recognize &mut u64 as mutable reference", 
            typeSystem.isReferenceType("&mut u64"))
        assertTrue("Should recognize &MyStruct as reference", 
            typeSystem.isReferenceType("&MyStruct"))
        
        assertFalse("Should not recognize u64 as reference", 
            typeSystem.isReferenceType("u64"))
    }
    
    fun testDereferenceType() {
        assertEquals("Should get u64 from &u64", 
            "u64", typeSystem.dereferenceType("&u64"))
        assertEquals("Should get u64 from &mut u64", 
            "u64", typeSystem.dereferenceType("&mut u64"))
        assertEquals("Should get MyStruct from &MyStruct", 
            "MyStruct", typeSystem.dereferenceType("&MyStruct"))
        
        assertEquals("Should return same type for non-reference", 
            "u64", typeSystem.dereferenceType("u64"))
    }
    
    fun testTypeInference() {
        // Literal type inference
        assertEquals("Should infer u64 for numeric literal", 
            "u64", typeSystem.inferLiteralType("42"))
        assertEquals("Should infer u64 for hex literal", 
            "u64", typeSystem.inferLiteralType("0xFF"))
        assertEquals("Should infer bool for true", 
            "bool", typeSystem.inferLiteralType("true"))
        assertEquals("Should infer bool for false", 
            "bool", typeSystem.inferLiteralType("false"))
        assertEquals("Should infer address for address literal", 
            "address", typeSystem.inferLiteralType("@0x1"))
        assertEquals("Should infer vector<u8> for byte string", 
            "vector<u8>", typeSystem.inferLiteralType("b\"hello\""))
    }
}
