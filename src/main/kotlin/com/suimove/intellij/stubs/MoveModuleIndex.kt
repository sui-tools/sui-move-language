package com.suimove.intellij.stubs

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.suimove.intellij.psi.MoveModule

/**
 * Index for Move modules by name.
 */
class MoveModuleIndex : StringStubIndexExtension<MoveModule>() {
    
    override fun getKey(): StubIndexKey<String, MoveModule> = KEY
    
    companion object {
        val KEY: StubIndexKey<String, MoveModule> = StubIndexKey.createIndexKey("move.module.name")
        
        val INSTANCE = MoveModuleIndex()
    }
}
