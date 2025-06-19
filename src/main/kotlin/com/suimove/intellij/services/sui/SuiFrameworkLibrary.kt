package com.suimove.intellij.services.sui

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.suimove.intellij.psi.MoveFile
import com.suimove.intellij.psi.MoveModule
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

/**
 * Service that provides access to Sui framework modules and documentation.
 */
@Service(Service.Level.PROJECT)
class SuiFrameworkLibrary(private val project: Project) {
    
    private val moduleCache = ConcurrentHashMap<String, FrameworkModule>()
    private val documentationCache = ConcurrentHashMap<String, String>()
    
    init {
        loadFrameworkModules()
    }
    
    /**
     * Get a framework module by its fully qualified name.
     */
    fun getModule(qualifiedName: String): FrameworkModule? {
        return moduleCache[qualifiedName]
    }
    
    /**
     * Get all framework modules.
     */
    fun getAllModules(): Collection<FrameworkModule> {
        return moduleCache.values
    }
    
    /**
     * Get modules by address.
     */
    fun getModulesByAddress(address: String): List<FrameworkModule> {
        return moduleCache.values.filter { it.address == address }
    }
    
    /**
     * Get documentation for a framework item.
     */
    fun getDocumentation(qualifiedName: String): String? {
        return documentationCache[qualifiedName] ?: loadDocumentation(qualifiedName)
    }
    
    /**
     * Search for framework items by name.
     */
    fun searchByName(query: String): List<FrameworkItem> {
        val results = mutableListOf<FrameworkItem>()
        
        moduleCache.values.forEach { module ->
            // Search module name
            if (module.name.contains(query, ignoreCase = true)) {
                results.add(module)
            }
            
            // Search functions
            module.functions.filter { it.name.contains(query, ignoreCase = true) }
                .forEach { results.add(it) }
            
            // Search structs
            module.structs.filter { it.name.contains(query, ignoreCase = true) }
                .forEach { results.add(it) }
            
            // Search constants
            module.constants.filter { it.name.contains(query, ignoreCase = true) }
                .forEach { results.add(it) }
        }
        
        return results
    }
    
    /**
     * Check if a type is from the Sui framework.
     */
    fun isFrameworkType(typeName: String): Boolean {
        return FRAMEWORK_TYPES.contains(typeName) || 
               moduleCache.values.any { module ->
                   module.structs.any { it.name == typeName }
               }
    }
    
    /**
     * Get common Sui object types.
     */
    fun getSuiObjectTypes(): List<FrameworkStruct> {
        return listOf(
            getModule("0x2::object")?.structs?.find { it.name == "UID" },
            getModule("0x2::object")?.structs?.find { it.name == "ID" },
            getModule("0x2::tx_context")?.structs?.find { it.name == "TxContext" },
            getModule("0x2::coin")?.structs?.find { it.name == "Coin" },
            getModule("0x2::balance")?.structs?.find { it.name == "Balance" }
        ).filterNotNull()
    }
    
    private fun loadFrameworkModules() {
        // Load core Sui framework modules
        loadModule("0x1", "option", "std/option.move")
        loadModule("0x1", "vector", "std/vector.move")
        loadModule("0x1", "string", "std/string.move")
        
        loadModule("0x2", "object", "sui/object.move")
        loadModule("0x2", "tx_context", "sui/tx_context.move")
        loadModule("0x2", "transfer", "sui/transfer.move")
        loadModule("0x2", "coin", "sui/coin.move")
        loadModule("0x2", "balance", "sui/balance.move")
        loadModule("0x2", "event", "sui/event.move")
        loadModule("0x2", "package", "sui/package.move")
        loadModule("0x2", "dynamic_field", "sui/dynamic_field.move")
        loadModule("0x2", "dynamic_object_field", "sui/dynamic_object_field.move")
        loadModule("0x2", "table", "sui/table.move")
        loadModule("0x2", "bag", "sui/bag.move")
        loadModule("0x2", "object_bag", "sui/object_bag.move")
        loadModule("0x2", "object_table", "sui/object_table.move")
        loadModule("0x2", "linked_table", "sui/linked_table.move")
        loadModule("0x2", "priority_queue", "sui/priority_queue.move")
        loadModule("0x2", "vec_map", "sui/vec_map.move")
        loadModule("0x2", "vec_set", "sui/vec_set.move")
        
        // Load from bundled resources or downloaded framework
        // This is a simplified version - in production, we'd load actual Move files
        createMockFrameworkModules()
    }
    
    private fun loadModule(address: String, moduleName: String, resourcePath: String) {
        // In a real implementation, we'd load from bundled resources
        // For now, we'll create mock data
    }
    
    private fun createMockFrameworkModules() {
        // Object module
        val objectModule = FrameworkModule(
            address = "0x2",
            name = "object",
            qualifiedName = "0x2::object",
            functions = listOf(
                FrameworkFunction(
                    name = "new",
                    signature = "fun new(ctx: &mut TxContext): UID",
                    visibility = "public",
                    documentation = "Creates a new object UID"
                ),
                FrameworkFunction(
                    name = "delete",
                    signature = "fun delete(id: UID)",
                    visibility = "public",
                    documentation = "Deletes an object UID"
                ),
                FrameworkFunction(
                    name = "id",
                    signature = "fun id<T: key>(obj: &T): ID",
                    visibility = "public",
                    documentation = "Gets the ID of an object"
                ),
                FrameworkFunction(
                    name = "id_bytes",
                    signature = "fun id_bytes<T: key>(obj: &T): vector<u8>",
                    visibility = "public",
                    documentation = "Gets the ID bytes of an object"
                )
            ),
            structs = listOf(
                FrameworkStruct(
                    name = "UID",
                    abilities = setOf("store"),
                    fields = listOf(
                        FrameworkField("id", "ID", "The unique identifier")
                    ),
                    documentation = "Unique identifier for Sui objects"
                ),
                FrameworkStruct(
                    name = "ID",
                    abilities = setOf("copy", "drop", "store"),
                    fields = listOf(
                        FrameworkField("bytes", "address", "The ID bytes")
                    ),
                    documentation = "Object ID type"
                )
            ),
            constants = emptyList()
        )
        moduleCache["0x2::object"] = objectModule
        
        // TxContext module
        val txContextModule = FrameworkModule(
            address = "0x2",
            name = "tx_context",
            qualifiedName = "0x2::tx_context",
            functions = listOf(
                FrameworkFunction(
                    name = "sender",
                    signature = "fun sender(ctx: &TxContext): address",
                    visibility = "public",
                    documentation = "Returns the address of the transaction sender"
                ),
                FrameworkFunction(
                    name = "epoch",
                    signature = "fun epoch(ctx: &TxContext): u64",
                    visibility = "public",
                    documentation = "Returns the current epoch number"
                ),
                FrameworkFunction(
                    name = "epoch_timestamp_ms",
                    signature = "fun epoch_timestamp_ms(ctx: &TxContext): u64",
                    visibility = "public",
                    documentation = "Returns the current epoch timestamp in milliseconds"
                ),
                FrameworkFunction(
                    name = "fresh_object_address",
                    signature = "fun fresh_object_address(ctx: &mut TxContext): address",
                    visibility = "public",
                    documentation = "Generates a fresh object address"
                )
            ),
            structs = listOf(
                FrameworkStruct(
                    name = "TxContext",
                    abilities = setOf("drop"),
                    fields = emptyList(), // Internal fields not exposed
                    documentation = "Transaction context provided by the Sui runtime"
                )
            ),
            constants = emptyList()
        )
        moduleCache["0x2::tx_context"] = txContextModule
        
        // Transfer module
        val transferModule = FrameworkModule(
            address = "0x2",
            name = "transfer",
            qualifiedName = "0x2::transfer",
            functions = listOf(
                FrameworkFunction(
                    name = "public_transfer",
                    signature = "fun public_transfer<T: key + store>(obj: T, recipient: address)",
                    visibility = "public",
                    documentation = "Transfer an object to a recipient"
                ),
                FrameworkFunction(
                    name = "public_freeze_object",
                    signature = "fun public_freeze_object<T: key + store>(obj: T)",
                    visibility = "public",
                    documentation = "Freeze an object, making it immutable and shared"
                ),
                FrameworkFunction(
                    name = "public_share_object",
                    signature = "fun public_share_object<T: key + store>(obj: T)",
                    visibility = "public",
                    documentation = "Share an object, making it accessible to everyone"
                ),
                FrameworkFunction(
                    name = "transfer",
                    signature = "fun transfer<T: key>(obj: T, recipient: address)",
                    visibility = "public",
                    documentation = "Transfer an object with only the key ability"
                ),
                FrameworkFunction(
                    name = "freeze_object",
                    signature = "fun freeze_object<T: key>(obj: T)",
                    visibility = "public",
                    documentation = "Freeze an object with only the key ability"
                ),
                FrameworkFunction(
                    name = "share_object",
                    signature = "fun share_object<T: key>(obj: T)",
                    visibility = "public",
                    documentation = "Share an object with only the key ability"
                )
            ),
            structs = emptyList(),
            constants = emptyList()
        )
        moduleCache["0x2::transfer"] = transferModule
        
        // Coin module
        val coinModule = FrameworkModule(
            address = "0x2",
            name = "coin",
            qualifiedName = "0x2::coin",
            functions = listOf(
                FrameworkFunction(
                    name = "value",
                    signature = "fun value<T>(coin: &Coin<T>): u64",
                    visibility = "public",
                    documentation = "Get the value of a coin"
                ),
                FrameworkFunction(
                    name = "split",
                    signature = "fun split<T>(coin: &mut Coin<T>, amount: u64, ctx: &mut TxContext): Coin<T>",
                    visibility = "public",
                    documentation = "Split a coin into two coins"
                ),
                FrameworkFunction(
                    name = "join",
                    signature = "fun join<T>(coin: &mut Coin<T>, other: Coin<T>)",
                    visibility = "public",
                    documentation = "Join two coins together"
                ),
                FrameworkFunction(
                    name = "destroy_zero",
                    signature = "fun destroy_zero<T>(coin: Coin<T>)",
                    visibility = "public",
                    documentation = "Destroy a coin with zero value"
                )
            ),
            structs = listOf(
                FrameworkStruct(
                    name = "Coin",
                    abilities = setOf("key", "store"),
                    fields = listOf(
                        FrameworkField("id", "UID", "The coin's unique identifier"),
                        FrameworkField("balance", "Balance<T>", "The coin's balance")
                    ),
                    documentation = "A coin of type T"
                ),
                FrameworkStruct(
                    name = "TreasuryCap",
                    abilities = setOf("key", "store"),
                    fields = listOf(
                        FrameworkField("id", "UID", "The treasury cap's unique identifier"),
                        FrameworkField("total_supply", "Supply<T>", "Total supply tracker")
                    ),
                    documentation = "Capability to mint and burn coins"
                )
            ),
            constants = emptyList()
        )
        moduleCache["0x2::coin"] = coinModule
    }
    
    private fun loadDocumentation(qualifiedName: String): String? {
        // In a real implementation, load from bundled documentation
        return when (qualifiedName) {
            "0x2::object::UID" -> """
                # UID
                
                A unique identifier for Sui objects. Every object in Sui must have a UID field.
                
                ## Example
                ```move
                struct MyObject has key {
                    id: UID,
                    value: u64
                }
                
                fun create_object(ctx: &mut TxContext): MyObject {
                    MyObject {
                        id: object::new(ctx),
                        value: 42
                    }
                }
                ```
            """.trimIndent()
            
            "0x2::tx_context::TxContext" -> """
                # TxContext
                
                Transaction context provided by the Sui runtime. Contains information about the current transaction.
                
                ## Common Usage
                - Get sender address: `tx_context::sender(ctx)`
                - Create new objects: `object::new(ctx)`
                - Get fresh addresses: `tx_context::fresh_object_address(ctx)`
                
                ## Note
                Entry functions typically receive `ctx: &mut TxContext` as their last parameter.
            """.trimIndent()
            
            else -> null
        }
    }
    
    companion object {
        fun getInstance(project: Project): SuiFrameworkLibrary {
            return project.getService(SuiFrameworkLibrary::class.java)
        }
        
        private val FRAMEWORK_TYPES = setOf(
            "UID", "ID", "TxContext", "Coin", "Balance", "Supply",
            "Table", "Bag", "ObjectBag", "ObjectTable", "LinkedTable",
            "VecMap", "VecSet", "PriorityQueue", "Option", "String"
        )
    }
}

/**
 * Base interface for framework items.
 */
sealed interface FrameworkItem {
    val name: String
    val documentation: String?
}

/**
 * Framework module information.
 */
data class FrameworkModule(
    val address: String,
    override val name: String,
    val qualifiedName: String,
    val functions: List<FrameworkFunction>,
    val structs: List<FrameworkStruct>,
    val constants: List<FrameworkConstant>,
    override val documentation: String? = null
) : FrameworkItem

/**
 * Framework function information.
 */
data class FrameworkFunction(
    override val name: String,
    val signature: String,
    val visibility: String,
    override val documentation: String? = null
) : FrameworkItem

/**
 * Framework struct information.
 */
data class FrameworkStruct(
    override val name: String,
    val abilities: Set<String>,
    val fields: List<FrameworkField>,
    override val documentation: String? = null
) : FrameworkItem

/**
 * Framework struct field.
 */
data class FrameworkField(
    val name: String,
    val type: String,
    val documentation: String? = null
)

/**
 * Framework constant information.
 */
data class FrameworkConstant(
    override val name: String,
    val type: String,
    val value: String?,
    override val documentation: String? = null
) : FrameworkItem
