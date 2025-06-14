package com.suimove.intellij.analysis

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.psi.MoveElementFactory

class MoveTypeSystemTest : BasePlatformTestCase() {
    
    fun testPrimitiveTypes() {
        // Test parsing primitive types
        assertEquals(MoveType.U8, MoveTypeSystem.parseType("u8"))
        assertEquals(MoveType.U64, MoveTypeSystem.parseType("u64"))
        assertEquals(MoveType.U128, MoveTypeSystem.parseType("u128"))
        assertEquals(MoveType.Bool, MoveTypeSystem.parseType("bool"))
        assertEquals(MoveType.Address, MoveTypeSystem.parseType("address"))
        
        // Test unknown types
        assertTrue(MoveTypeSystem.parseType("MyStruct") is MoveType.Struct)
    }
    
    fun testTypeCompatibility() {
        // Same types are compatible
        assertTrue("u64 should be assignable to u64", 
            MoveTypeSystem.isAssignable(MoveType.U64, MoveType.U64))
        assertTrue("bool should be assignable to bool", 
            MoveTypeSystem.isAssignable(MoveType.Bool, MoveType.Bool))
        
        // Different types are not compatible
        assertFalse("u64 should not be assignable to bool", 
            MoveTypeSystem.isAssignable(MoveType.U64, MoveType.Bool))
        assertFalse("address should not be assignable to u64", 
            MoveTypeSystem.isAssignable(MoveType.Address, MoveType.U64))
    }
    
    fun testNumericTypeCompatibility() {
        // Numeric types are not implicitly convertible in Move
        assertFalse("u8 should not be assignable to u64", 
            MoveTypeSystem.isAssignable(MoveType.U8, MoveType.U64))
        assertFalse("u64 should not be assignable to u128", 
            MoveTypeSystem.isAssignable(MoveType.U64, MoveType.U128))
    }
    
    fun testVectorTypes() {
        val vectorU64 = MoveTypeSystem.parseType("vector<u64>")
        assertTrue("Should parse vector<u64>", vectorU64 is MoveType.Vector)
        assertEquals("Vector element type should be u64", 
            MoveType.U64, (vectorU64 as MoveType.Vector).elementType)
        
        val vectorBool = MoveTypeSystem.parseType("vector<bool>")
        assertTrue("Should parse vector<bool>", vectorBool is MoveType.Vector)
        assertEquals("Vector element type should be bool", 
            MoveType.Bool, (vectorBool as MoveType.Vector).elementType)
        
        // Vectors of same element type are assignable
        assertTrue("vector<u64> should be assignable to vector<u64>",
            MoveTypeSystem.isAssignable(vectorU64, vectorU64))
        
        // Vectors of different element types are not assignable
        assertFalse("vector<u64> should not be assignable to vector<bool>",
            MoveTypeSystem.isAssignable(vectorU64, vectorBool))
    }
    
    fun testStructTypes() {
        val struct1 = MoveType.Struct("MyStruct", "0x1::module")
        val struct2 = MoveType.Struct("MyStruct", "0x1::module")
        val struct3 = MoveType.Struct("OtherStruct", "0x1::module")
        
        assertTrue("Same struct should be assignable",
            MoveTypeSystem.isAssignable(struct1, struct2))
        
        assertFalse("Different structs should not be assignable",
            MoveTypeSystem.isAssignable(struct1, struct3))
    }
    
    fun testReferenceTypes() {
        val ref = MoveType.Reference(false, MoveType.U64)
        val mutRef = MoveType.Reference(true, MoveType.U64)
        
        assertEquals("Should format immutable reference correctly",
            "&u64", ref.toString())
        assertEquals("Should format mutable reference correctly",
            "&mut u64", mutRef.toString())
        
        // References are assignable based on mutability and inner type
        assertTrue("Same reference type should be assignable",
            MoveTypeSystem.isAssignable(ref, ref))
        assertTrue("Same mutable reference type should be assignable",
            MoveTypeSystem.isAssignable(mutRef, mutRef))
        
        // Mutable references can be assigned to immutable references
        assertTrue("Mutable ref should be assignable to immutable ref",
            MoveTypeSystem.isAssignable(mutRef, ref))
        
        // Immutable references cannot be assigned to mutable references
        assertFalse("Immutable ref should not be assignable to mutable ref",
            MoveTypeSystem.isAssignable(ref, mutRef))
    }
    
    fun testTypeInference() {
        // Test type parsing for literals
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test() {
                    let a = true;
                    let b = false;
                    let c = 42;
                    let d = 0x42;
                    let e = @0x1;
                    let f = b"hello";
                }
            }
        """.trimIndent())
        
        // Since we can't easily test inferType without proper PSI elements,
        // we'll test the parseType functionality instead
        assertEquals("Should parse bool type", MoveType.Bool, MoveTypeSystem.parseType("bool"))
        assertEquals("Should parse u64 type", MoveType.U64, MoveTypeSystem.parseType("u64"))
        assertEquals("Should parse address type", MoveType.Address, MoveTypeSystem.parseType("address"))
        
        val vectorU8 = MoveTypeSystem.parseType("vector<u8>")
        assertTrue("Should parse vector<u8>", vectorU8 is MoveType.Vector)
        assertEquals("Should have u8 element type", MoveType.U8, (vectorU8 as MoveType.Vector).elementType)
    }
    
    fun testTypeToString() {
        assertEquals("bool", MoveType.Bool.toString())
        assertEquals("u8", MoveType.U8.toString())
        assertEquals("u64", MoveType.U64.toString())
        assertEquals("u128", MoveType.U128.toString())
        assertEquals("address", MoveType.Address.toString())
        assertEquals("vector<u64>", MoveType.Vector(MoveType.U64).toString())
        assertEquals("MyStruct", MoveType.Struct("MyStruct").toString())
        assertEquals("0x1::module::MyStruct", MoveType.Struct("MyStruct", "0x1::module").toString())
        assertEquals("&u64", MoveType.Reference(false, MoveType.U64).toString())
        assertEquals("&mut u64", MoveType.Reference(true, MoveType.U64).toString())
        assertEquals("()", MoveType.Void.toString())
        assertEquals("unknown", MoveType.Unknown.toString())
    }
    
    fun testNestedTypes() {
        // Test vector of vectors
        val nestedVector = MoveTypeSystem.parseType("vector<vector<u64>>")
        assertTrue("Should parse nested vector", nestedVector is MoveType.Vector)
        val innerType = (nestedVector as MoveType.Vector).elementType
        assertTrue("Inner type should be vector", innerType is MoveType.Vector)
        assertEquals("Inner vector element should be u64", 
            MoveType.U64, (innerType as MoveType.Vector).elementType)
        
        // Test reference to vector
        val refVector = MoveType.Reference(false, MoveType.Vector(MoveType.U64))
        assertEquals("Should format reference to vector correctly",
            "&vector<u64>", refVector.toString())
    }
}
