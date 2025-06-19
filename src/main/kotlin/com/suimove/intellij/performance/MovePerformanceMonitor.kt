package com.suimove.intellij.performance

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

/**
 * Performance monitoring service for the Move plugin.
 * Tracks execution times, memory usage, and provides performance insights.
 */
@Service(Service.Level.PROJECT)
class MovePerformanceMonitor(private val project: Project) : Disposable {
    
    companion object {
        private val LOG = Logger.getInstance(MovePerformanceMonitor::class.java)
        private const val SLOW_OPERATION_THRESHOLD_MS = 100L
        private const val MEMORY_CHECK_INTERVAL_MINUTES = 5L
        
        fun getInstance(project: Project): MovePerformanceMonitor {
            return project.getService(MovePerformanceMonitor::class.java)
        }
    }
    
    private val operationMetrics = ConcurrentHashMap<String, OperationMetrics>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()
    private val executor = AppExecutorUtil.createBoundedScheduledExecutorService("MovePerformanceMonitor", 1)
    
    init {
        // Schedule periodic memory monitoring
        executor.scheduleWithFixedDelay(
            { captureMemorySnapshot() },
            0,
            MEMORY_CHECK_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
        
        Disposer.register(project, this)
    }
    
    /**
     * Measure and record operation performance.
     */
    fun <T> measureOperation(operationName: String, operation: () -> T): T {
        val startMemory = Runtime.getRuntime().freeMemory()
        val result: T
        
        val duration = measureTimeMillis {
            result = operation()
        }
        
        val memoryUsed = startMemory - Runtime.getRuntime().freeMemory()
        
        recordOperation(operationName, duration, memoryUsed)
        
        if (duration > SLOW_OPERATION_THRESHOLD_MS) {
            LOG.warn("Slow operation detected: $operationName took ${duration}ms")
        }
        
        return result
    }
    
    /**
     * Record an operation's performance metrics.
     */
    fun recordOperation(operationName: String, durationMs: Long, memoryBytes: Long = 0) {
        val metrics = operationMetrics.computeIfAbsent(operationName) { OperationMetrics(operationName) }
        metrics.record(durationMs, memoryBytes)
    }
    
    /**
     * Get performance report for all operations.
     */
    fun getPerformanceReport(): PerformanceReport {
        val operations = operationMetrics.values.map { it.getSnapshot() }
        val currentMemory = captureMemorySnapshot()
        
        return PerformanceReport(
            operations = operations,
            memorySnapshots = memorySnapshots.toList(),
            currentMemory = currentMemory,
            recommendations = generateRecommendations(operations)
        )
    }
    
    /**
     * Get metrics for a specific operation.
     */
    fun getOperationMetrics(operationName: String): OperationSnapshot? {
        return operationMetrics[operationName]?.getSnapshot()
    }
    
    /**
     * Clear all collected metrics.
     */
    fun clearMetrics() {
        operationMetrics.clear()
        memorySnapshots.clear()
    }
    
    /**
     * Export performance data as JSON.
     */
    fun exportPerformanceData(): String {
        val report = getPerformanceReport()
        return buildString {
            appendLine("{")
            appendLine("  \"operations\": [")
            report.operations.forEachIndexed { index, op ->
                append("    ")
                append(op.toJson())
                if (index < report.operations.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ],")
            appendLine("  \"memory\": {")
            appendLine("    \"current\": ${report.currentMemory.toJson()},")
            appendLine("    \"history\": [")
            report.memorySnapshots.forEachIndexed { index, snapshot ->
                append("      ")
                append(snapshot.toJson())
                if (index < report.memorySnapshots.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("    ]")
            appendLine("  },")
            appendLine("  \"recommendations\": [")
            report.recommendations.forEachIndexed { index, rec ->
                append("    \"$rec\"")
                if (index < report.recommendations.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ]")
            appendLine("}")
        }
    }
    
    override fun dispose() {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }
    }
    
    private fun captureMemorySnapshot(): MemorySnapshot {
        val runtime = Runtime.getRuntime()
        val snapshot = MemorySnapshot(
            timestamp = System.currentTimeMillis(),
            totalMemory = runtime.totalMemory(),
            freeMemory = runtime.freeMemory(),
            maxMemory = runtime.maxMemory()
        )
        
        memorySnapshots.add(snapshot)
        
        // Keep only last 100 snapshots
        if (memorySnapshots.size > 100) {
            memorySnapshots.removeAt(0)
        }
        
        return snapshot
    }
    
    private fun generateRecommendations(operations: List<OperationSnapshot>): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Check for slow operations
        operations.filter { it.averageDuration > SLOW_OPERATION_THRESHOLD_MS }
            .forEach { op ->
                recommendations.add(
                    "Operation '${op.name}' is slow (avg: ${op.averageDuration}ms). " +
                    "Consider optimizing or caching results."
                )
            }
        
        // Check for high memory usage
        val highMemoryOps = operations.filter { it.averageMemory > 10 * 1024 * 1024 } // 10MB
        if (highMemoryOps.isNotEmpty()) {
            recommendations.add(
                "Operations with high memory usage detected: ${highMemoryOps.joinToString { it.name }}. " +
                "Consider streaming or chunking data."
            )
        }
        
        // Check cache performance
        val cacheOp = operations.find { it.name.contains("cache", ignoreCase = true) }
        if (cacheOp != null && cacheOp.count > 1000) {
            val hitRate = calculateCacheHitRate()
            if (hitRate < 0.8) {
                recommendations.add(
                    "Cache hit rate is low (${(hitRate * 100).toInt()}%). " +
                    "Consider adjusting cache size or invalidation strategy."
                )
            }
        }
        
        // Check memory pressure
        val currentMemory = memorySnapshots.lastOrNull()
        if (currentMemory != null) {
            val usedPercent = currentMemory.usedMemory.toDouble() / currentMemory.totalMemory
            if (usedPercent > 0.9) {
                recommendations.add(
                    "High memory usage detected (${(usedPercent * 100).toInt()}%). " +
                    "Consider increasing heap size or optimizing memory usage."
                )
            }
        }
        
        return recommendations
    }
    
    private fun calculateCacheHitRate(): Double {
        // This would integrate with the type cache statistics
        return 0.85 // Placeholder
    }
}

/**
 * Metrics for a single operation type.
 */
class OperationMetrics(val name: String) {
    private val count = AtomicLong(0)
    private val totalDuration = AtomicLong(0)
    private val maxDuration = AtomicLong(0)
    private val minDuration = AtomicLong(Long.MAX_VALUE)
    private val totalMemory = AtomicLong(0)
    
    fun record(durationMs: Long, memoryBytes: Long) {
        count.incrementAndGet()
        totalDuration.addAndGet(durationMs)
        totalMemory.addAndGet(memoryBytes)
        
        // Update max
        var currentMax = maxDuration.get()
        while (durationMs > currentMax && !maxDuration.compareAndSet(currentMax, durationMs)) {
            currentMax = maxDuration.get()
        }
        
        // Update min
        var currentMin = minDuration.get()
        while (durationMs < currentMin && !minDuration.compareAndSet(currentMin, durationMs)) {
            currentMin = minDuration.get()
        }
    }
    
    fun getSnapshot(): OperationSnapshot {
        val currentCount = count.get()
        return OperationSnapshot(
            name = name,
            count = currentCount,
            totalDuration = totalDuration.get(),
            averageDuration = if (currentCount > 0) totalDuration.get() / currentCount else 0,
            maxDuration = maxDuration.get(),
            minDuration = if (minDuration.get() == Long.MAX_VALUE) 0 else minDuration.get(),
            totalMemory = totalMemory.get(),
            averageMemory = if (currentCount > 0) totalMemory.get() / currentCount else 0
        )
    }
}

/**
 * Snapshot of operation metrics.
 */
data class OperationSnapshot(
    val name: String,
    val count: Long,
    val totalDuration: Long,
    val averageDuration: Long,
    val maxDuration: Long,
    val minDuration: Long,
    val totalMemory: Long,
    val averageMemory: Long
) {
    fun toJson(): String {
        return """{
            |"name": "$name",
            |"count": $count,
            |"duration": {
            |  "total": $totalDuration,
            |  "average": $averageDuration,
            |  "max": $maxDuration,
            |  "min": $minDuration
            |},
            |"memory": {
            |  "total": $totalMemory,
            |  "average": $averageMemory
            |}
        |}""".trimMargin().replace("\n", " ")
    }
}

/**
 * Memory usage snapshot.
 */
data class MemorySnapshot(
    val timestamp: Long,
    val totalMemory: Long,
    val freeMemory: Long,
    val maxMemory: Long
) {
    val usedMemory: Long get() = totalMemory - freeMemory
    
    fun toJson(): String {
        return """{
            |"timestamp": $timestamp,
            |"total": $totalMemory,
            |"free": $freeMemory,
            |"used": $usedMemory,
            |"max": $maxMemory
        |}""".trimMargin().replace("\n", " ")
    }
}

/**
 * Complete performance report.
 */
data class PerformanceReport(
    val operations: List<OperationSnapshot>,
    val memorySnapshots: List<MemorySnapshot>,
    val currentMemory: MemorySnapshot,
    val recommendations: List<String>
)
