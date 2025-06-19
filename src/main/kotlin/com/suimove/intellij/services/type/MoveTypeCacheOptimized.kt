package com.suimove.intellij.services.type

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.ContainerUtil
import com.suimove.intellij.psi.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.Collections

/**
 * Optimized type cache with improved performance characteristics:
 * - Soft references for memory efficiency
 * - File-based invalidation for better granularity
 * - Statistics tracking for performance monitoring
 * - Batch operations support
 */
@Service(Service.Level.PROJECT)
class MoveTypeCacheOptimized(private val project: Project) : Disposable {
    
    companion object {
        private val LOG = Logger.getInstance(MoveTypeCacheOptimized::class.java)
        private val TYPE_KEY = Key.create<CachedValue<MoveType>>("MOVE_TYPE_CACHED")
        
        // Cache size limits
        private const val MAX_CACHE_SIZE = 10000
        private const val CLEANUP_THRESHOLD = 0.9 // Clean up when 90% full
    }
    
    // Performance statistics
    private val stats = CacheStatistics()
    
    // Main type cache with soft references
    private val typeCache = ContainerUtil.createConcurrentSoftValueMap<PsiElement, CachedValue<MoveType>>()
    
    // File-based cache for quick file invalidation
    private val fileCaches = ConcurrentHashMap<VirtualFile, MutableSet<PsiElement>>()
    
    // Type hierarchy index for quick lookups
    private val typeHierarchyIndex = ConcurrentHashMap<String, TypeHierarchyInfo>()
    
    // Generic type parameter cache
    private val genericTypeCache = ContainerUtil.createConcurrentSoftValueMap<String, ResolvedGenericType>()
    
    init {
        // Register PSI change listener for cache invalidation
        PsiManager.getInstance(project).addPsiTreeChangeListener(
            CacheInvalidationListener(),
            this
        )
    }
    
    /**
     * Get cached type with statistics tracking.
     */
    fun getCachedType(element: PsiElement): MoveType? {
        stats.totalRequests.incrementAndGet()
        
        // Try user data first (fastest)
        element.getUserData(TYPE_KEY)?.let { cached ->
            stats.hits.incrementAndGet()
            return cached.value
        }
        
        // Try main cache
        val cachedValue = typeCache[element]
        if (cachedValue != null) {
            stats.hits.incrementAndGet()
            // Store in user data for faster access
            element.putUserData(TYPE_KEY, cachedValue)
            return cachedValue.value
        }
        
        stats.misses.incrementAndGet()
        return null
    }
    
    /**
     * Cache type with automatic cleanup if needed.
     */
    fun cacheType(element: PsiElement, type: MoveType) {
        // Check cache size and cleanup if needed
        if (typeCache.size > MAX_CACHE_SIZE * CLEANUP_THRESHOLD) {
            cleanupCache()
        }
        
        val cachedValue = CachedValuesManager.getManager(project).createCachedValue(
            CachedValueProvider {
                CachedValueProvider.Result.create(
                    type,
                    element.containingFile ?: PsiModificationTracker.MODIFICATION_COUNT
                )
            },
            false
        )
        
        // Store in both caches
        typeCache[element] = cachedValue
        element.putUserData(TYPE_KEY, cachedValue)
        
        // Track in file cache
        element.containingFile?.virtualFile?.let { file ->
            val set = fileCaches.computeIfAbsent(file) { 
                Collections.newSetFromMap(ConcurrentHashMap())
            }
            set.add(element)
        }
        
        // Update hierarchy index if it's a struct type
        if (type is MoveNamedType) {
            updateTypeHierarchy(type)
        }
    }
    
    /**
     * Batch cache multiple types for better performance.
     */
    fun cacheTypes(types: Map<PsiElement, MoveType>) {
        types.forEach { (element, type) ->
            cacheType(element, type)
        }
    }
    
    /**
     * Get or compute type with caching.
     */
    fun getOrComputeType(element: PsiElement, computer: () -> MoveType?): MoveType? {
        getCachedType(element)?.let { return it }
        
        val computed = computer() ?: return null
        cacheType(element, computed)
        return computed
    }
    
    /**
     * Invalidate cache for a specific file.
     */
    fun invalidateFile(file: VirtualFile) {
        stats.invalidations.incrementAndGet()
        
        fileCaches.remove(file)?.forEach { element ->
            typeCache.remove(element)
            element.putUserData(TYPE_KEY, null)
        }
    }
    
    /**
     * Clear entire cache.
     */
    fun invalidate() {
        stats.invalidations.incrementAndGet()
        
        typeCache.clear()
        fileCaches.clear()
        typeHierarchyIndex.clear()
        genericTypeCache.clear()
    }
    
    /**
     * Get cache statistics for monitoring.
     */
    fun getStatistics(): CacheStatistics = stats.copy()
    
    /**
     * Reset statistics.
     */
    fun resetStatistics() {
        stats.reset()
    }
    
    /**
     * Get type hierarchy information.
     */
    fun getTypeHierarchy(typeName: String): TypeHierarchyInfo? {
        return typeHierarchyIndex[typeName]
    }
    
    /**
     * Cache resolved generic type.
     */
    fun cacheGenericType(signature: String, resolved: ResolvedGenericType) {
        genericTypeCache[signature] = resolved
    }
    
    /**
     * Get cached generic type resolution.
     */
    fun getCachedGenericType(signature: String): ResolvedGenericType? {
        return genericTypeCache[signature]
    }
    
    override fun dispose() {
        invalidate()
    }
    
    private fun updateTypeHierarchy(type: MoveNamedType) {
        val info = TypeHierarchyInfo(
            typeName = type.qualifiedName,
            supertypes = emptySet(),
            abilities = emptySet(),
            typeParameters = emptyList(),
            isStruct = true,
            isEnum = false,
            moduleAddress = type.moduleAddress,
            moduleName = type.moduleName
        )
        typeHierarchyIndex[info.typeName] = info
    }
    
    private fun cleanupCache() {
        LOG.debug("Cleaning up type cache, current size: ${typeCache.size}")
        
        // Remove entries for invalid PSI elements
        val toRemove = typeCache.keys.filter { !it.isValid }
        toRemove.forEach { element ->
            typeCache.remove(element)
            element.putUserData(TYPE_KEY, null)
        }
        
        // Clean up file caches
        fileCaches.entries.removeIf { (file, elements) ->
            elements.removeIf { !it.isValid }
            elements.isEmpty()
        }
        
        LOG.debug("Cache cleanup complete, removed ${toRemove.size} entries")
    }
    
    /**
     * PSI change listener for cache invalidation.
     */
    private inner class CacheInvalidationListener : PsiTreeChangeAdapter() {
        override fun childrenChanged(event: PsiTreeChangeEvent) {
            handlePsiChange(event)
        }
        
        override fun childAdded(event: PsiTreeChangeEvent) {
            handlePsiChange(event)
        }
        
        override fun childRemoved(event: PsiTreeChangeEvent) {
            handlePsiChange(event)
        }
        
        override fun childReplaced(event: PsiTreeChangeEvent) {
            handlePsiChange(event)
        }
        
        private fun handlePsiChange(event: PsiTreeChangeEvent) {
            val file = event.file?.virtualFile ?: return
            
            // Only invalidate if it's a Move file
            if (file.extension == "move") {
                invalidateFile(file)
            }
        }
    }
}

/**
 * Cache performance statistics.
 */
data class CacheStatistics(
    val totalRequests: AtomicLong = AtomicLong(0),
    val hits: AtomicLong = AtomicLong(0),
    val misses: AtomicLong = AtomicLong(0),
    val invalidations: AtomicLong = AtomicLong(0)
) {
    val hitRate: Double
        get() = if (totalRequests.get() > 0) {
            hits.get().toDouble() / totalRequests.get()
        } else 0.0
    
    fun reset() {
        totalRequests.set(0)
        hits.set(0)
        misses.set(0)
        invalidations.set(0)
    }
    
    fun copy() = CacheStatistics(
        AtomicLong(totalRequests.get()),
        AtomicLong(hits.get()),
        AtomicLong(misses.get()),
        AtomicLong(invalidations.get())
    )
}

/**
 * Resolved generic type information.
 */
data class ResolvedGenericType(
    val typeParameters: Map<String, MoveType>,
    val constraints: Map<String, List<MoveAbility>>,
    val resolvedType: MoveType
)
