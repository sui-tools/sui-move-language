package com.suimove.intellij.services.type

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for caching resolved types to improve performance.
 * Automatically invalidates cache when PSI tree changes.
 */
@Service(Service.Level.PROJECT)
class MoveTypeCache(private val project: Project) {
    
    private val typeCache = ContainerUtil.createConcurrentWeakMap<PsiElement, CachedValue<MoveType>>()
    private val typeHierarchyIndex = ConcurrentHashMap<String, TypeHierarchyInfo>()
    
    /**
     * Get cached type for an element, or null if not cached.
     */
    fun getCachedType(element: PsiElement): MoveType? {
        val cachedValue = typeCache[element] ?: return null
        return cachedValue.value
    }
    
    /**
     * Cache a resolved type for an element.
     */
    fun cacheType(element: PsiElement, type: MoveType) {
        val cachedValue = CachedValuesManager.getManager(project).createCachedValue(
            {
                CachedValueProvider.Result.create(
                    type,
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            },
            false
        )
        typeCache[element] = cachedValue
    }
    
    /**
     * Clear cache for a specific element.
     */
    fun invalidate(element: PsiElement) {
        typeCache.remove(element)
    }
    
    /**
     * Clear entire cache.
     */
    fun invalidateAll() {
        typeCache.clear()
        typeHierarchyIndex.clear()
    }
    
    /**
     * Index a type in the hierarchy for fast lookup.
     */
    fun indexType(typeName: String, info: TypeHierarchyInfo) {
        typeHierarchyIndex[typeName] = info
    }
    
    /**
     * Get type hierarchy information.
     */
    fun getTypeHierarchy(typeName: String): TypeHierarchyInfo? {
        return typeHierarchyIndex[typeName]
    }
    
    /**
     * Find all subtypes of a given type.
     */
    fun findSubtypes(typeName: String): Set<String> {
        return typeHierarchyIndex.entries
            .filter { it.value.supertypes.contains(typeName) }
            .map { it.key }
            .toSet()
    }
    
    /**
     * Find all supertypes of a given type.
     */
    fun findSupertypes(typeName: String): Set<String> {
        val info = typeHierarchyIndex[typeName] ?: return emptySet()
        val result = mutableSetOf<String>()
        val queue = ArrayDeque(info.supertypes)
        
        while (queue.isNotEmpty()) {
            val supertype = queue.removeFirst()
            if (result.add(supertype)) {
                typeHierarchyIndex[supertype]?.supertypes?.let { queue.addAll(it) }
            }
        }
        
        return result
    }
    
    /**
     * Check if type1 is a subtype of type2.
     */
    fun isSubtypeOf(type1: String, type2: String): Boolean {
        if (type1 == type2) return true
        return findSupertypes(type1).contains(type2)
    }
    
    companion object {
        fun getInstance(project: Project): MoveTypeCache {
            return project.getService(MoveTypeCache::class.java)
        }
    }
}

/**
 * Information about a type in the hierarchy.
 */
data class TypeHierarchyInfo(
    val typeName: String,
    val supertypes: Set<String> = emptySet(),
    val abilities: Set<MoveAbility> = emptySet(),
    val typeParameters: List<MoveTypeParameter> = emptyList(),
    val isStruct: Boolean = false,
    val isEnum: Boolean = false,
    val moduleAddress: String? = null,
    val moduleName: String? = null
) {
    val qualifiedName: String
        get() = if (moduleAddress != null && moduleName != null) {
            "$moduleAddress::$moduleName::$typeName"
        } else {
            typeName
        }
}
