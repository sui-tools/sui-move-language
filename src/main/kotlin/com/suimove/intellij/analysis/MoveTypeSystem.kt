package com.suimove.intellij.analysis

import com.intellij.psi.PsiElement
import com.suimove.intellij.psi.MoveTypes

sealed class MoveType {
    object Bool : MoveType()
    object U8 : MoveType()
    object U16 : MoveType()
    object U32 : MoveType()
    object U64 : MoveType()
    object U128 : MoveType()
    object U256 : MoveType()
    object Address : MoveType()
    data class Vector(val elementType: MoveType) : MoveType() {
        override fun toString(): String = "vector<$elementType>"
    }
    data class Struct(val name: String, val module: String? = null) : MoveType() {
        override fun toString(): String = if (module != null) "$module::$name" else name
    }
    data class Reference(val mutable: Boolean, val innerType: MoveType) : MoveType() {
        override fun toString(): String = if (mutable) "&mut $innerType" else "&$innerType"
    }
    object Unknown : MoveType()
    object Void : MoveType()
    
    override fun toString(): String = when (this) {
        is Bool -> "bool"
        is U8 -> "u8"
        is U16 -> "u16"
        is U32 -> "u32"
        is U64 -> "u64"
        is U128 -> "u128"
        is U256 -> "u256"
        is Address -> "address"
        is Vector -> "vector<$elementType>"
        is Struct -> if (module != null) "$module::$name" else name
        is Reference -> if (mutable) "&mut $innerType" else "&$innerType"
        is Unknown -> "unknown"
        is Void -> "()"
    }
}

object MoveTypeSystem {
    fun inferType(element: PsiElement): MoveType {
        return when (element.node?.elementType) {
            MoveTypes.TRUE, MoveTypes.FALSE -> MoveType.Bool
            MoveTypes.INTEGER_LITERAL -> inferIntegerType(element.text)
            MoveTypes.HEX_LITERAL -> MoveType.U64 // Default for hex
            MoveTypes.ADDRESS_LITERAL -> MoveType.Address
            MoveTypes.BYTE_STRING_LITERAL -> MoveType.Vector(MoveType.U8)
            else -> inferFromContext(element)
        }
    }
    
    private fun inferIntegerType(text: String): MoveType {
        return when {
            text.endsWith("u8") -> MoveType.U8
            text.endsWith("u16") -> MoveType.U16
            text.endsWith("u32") -> MoveType.U32
            text.endsWith("u64") -> MoveType.U64
            text.endsWith("u128") -> MoveType.U128
            text.endsWith("u256") -> MoveType.U256
            else -> MoveType.U64 // Default
        }
    }
    
    private fun inferFromContext(element: PsiElement): MoveType {
        // Look for type annotations
        val parent = element.parent
        if (parent?.node?.elementType == MoveTypes.TYPE_ANNOTATION) {
            return parseTypeAnnotation(parent)
        }
        
        // Check if it's a known type name
        return when (element.text) {
            "bool" -> MoveType.Bool
            "u8" -> MoveType.U8
            "u16" -> MoveType.U16
            "u32" -> MoveType.U32
            "u64" -> MoveType.U64
            "u128" -> MoveType.U128
            "u256" -> MoveType.U256
            "address" -> MoveType.Address
            "vector" -> MoveType.Vector(MoveType.Unknown)
            else -> MoveType.Unknown
        }
    }
    
    private fun parseTypeAnnotation(element: PsiElement): MoveType {
        val typeText = element.text.trim().removePrefix(":").trim()
        return parseType(typeText)
    }
    
    fun parseType(typeText: String): MoveType {
        return when {
            typeText == "bool" -> MoveType.Bool
            typeText == "u8" -> MoveType.U8
            typeText == "u16" -> MoveType.U16
            typeText == "u32" -> MoveType.U32
            typeText == "u64" -> MoveType.U64
            typeText == "u128" -> MoveType.U128
            typeText == "u256" -> MoveType.U256
            typeText == "address" -> MoveType.Address
            typeText.startsWith("vector<") && typeText.endsWith(">") -> {
                val inner = typeText.substring(7, typeText.length - 1)
                MoveType.Vector(parseType(inner))
            }
            typeText.startsWith("&mut ") -> {
                MoveType.Reference(true, parseType(typeText.substring(5)))
            }
            typeText.startsWith("&") -> {
                MoveType.Reference(false, parseType(typeText.substring(1)))
            }
            typeText.contains("::") -> {
                val parts = typeText.split("::")
                if (parts.size == 2) {
                    MoveType.Struct(parts[1], parts[0])
                } else {
                    MoveType.Struct(typeText)
                }
            }
            else -> MoveType.Struct(typeText)
        }
    }
    
    fun isAssignable(from: MoveType, to: MoveType): Boolean {
        return when {
            from == to -> true
            from is MoveType.Unknown || to is MoveType.Unknown -> true
            from is MoveType.Reference && to is MoveType.Reference -> {
                // A mutable reference can be assigned to an immutable reference
                // but not vice versa
                (from.mutable || !to.mutable) && isAssignable(from.innerType, to.innerType)
            }
            else -> false
        }
    }
}
