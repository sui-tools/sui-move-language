# Sui Move Language Plugin v2.0 - API Reference

## Overview
This document provides a comprehensive API reference for developers who want to extend or integrate with the Sui Move Language Plugin.

## Core Services

### MoveTypeInferenceEngine
Main service for type inference and type checking.

```kotlin
class MoveTypeInferenceEngine {
    fun inferType(element: PsiElement): MoveType?
    fun resolveTypeParameters(element: PsiElement): Map<String, MoveType>
    fun checkTypeConstraints(type: MoveType, constraints: List<Ability>): Boolean
    fun unifyTypes(type1: MoveType, type2: MoveType): MoveType?
}
```

**Usage Example:**
```kotlin
val typeEngine = project.service<MoveTypeInferenceEngine>()
val inferredType = typeEngine.inferType(myExpression)
```

### MoveTypeCache
Caches resolved types for performance.

```kotlin
class MoveTypeCache {
    fun getCachedType(element: PsiElement): MoveType?
    fun cacheType(element: PsiElement, type: MoveType)
    fun invalidate()
    fun invalidateFile(file: VirtualFile)
}
```

### SuiFrameworkLibrary
Provides access to Sui framework modules and documentation.

```kotlin
class SuiFrameworkLibrary {
    fun getFrameworkModules(): List<MoveModule>
    fun getModuleDocumentation(moduleName: String): String?
    fun getBuiltinTypes(): List<MoveType>
    fun getBuiltinFunctions(): List<MoveFunction>
}
```

## PSI Elements

### Core PSI Interfaces

#### MoveElement
Base interface for all Move PSI elements.
```kotlin
interface MoveElement : PsiElement {
    fun getModule(): MoveModule?
    fun getContainingFunction(): MoveFunction?
}
```

#### MoveNamedElement
Elements with names (functions, structs, variables).
```kotlin
interface MoveNamedElement : MoveElement, PsiNamedElement {
    override fun getName(): String?
    override fun setName(name: String): PsiElement
}
```

#### MoveModule
Represents a Move module.
```kotlin
interface MoveModule : MoveNamedElement {
    fun getAddress(): String?
    fun getFunctions(): List<MoveFunction>
    fun getStructs(): List<MoveStruct>
    fun getConstants(): List<MoveConstant>
    fun getUseStatements(): List<MoveUseStatement>
}
```

#### MoveFunction
Represents a function declaration.
```kotlin
interface MoveFunction : MoveNamedElement {
    fun getParameters(): List<MoveParameter>
    fun getReturnType(): MoveType?
    fun getTypeParameters(): List<MoveTypeParameter>
    fun getBody(): MoveCodeBlock?
    fun isEntry(): Boolean
    fun isPublic(): Boolean
    fun getAcquires(): List<String>
}
```

#### MoveStruct
Represents a struct declaration.
```kotlin
interface MoveStruct : MoveNamedElement {
    fun getFields(): List<MoveStructField>
    fun getTypeParameters(): List<MoveTypeParameter>
    fun getAbilities(): List<Ability>
    fun hasAbility(ability: Ability): Boolean
}
```

## Completion API

### Custom Completion Provider
Create custom completion providers by extending `CompletionProvider`.

```kotlin
class MyCustomCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        // Add your completions
        result.addElement(
            LookupElementBuilder.create("myCompletion")
                .withIcon(MoveIcons.FILE)
                .withTypeText("Type info")
        )
    }
}
```

### Registering Completion Provider
Register in your `CompletionContributor`:
```kotlin
class MyCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            MyCustomCompletionProvider()
        )
    }
}
```

## Inspection API

### Creating Custom Inspections
Extend `LocalInspectionTool` for custom inspections.

```kotlin
class MyMoveInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : MoveVisitor() {
            override fun visitFunction(function: MoveFunction) {
                // Check function and report problems
                if (problemDetected(function)) {
                    holder.registerProblem(
                        function,
                        "Problem description",
                        ProblemHighlightType.WARNING,
                        MyQuickFix()
                    )
                }
            }
        }
    }
}
```

### Quick Fixes
Implement `LocalQuickFix` for problem resolution.

```kotlin
class MyQuickFix : LocalQuickFix {
    override fun getFamilyName() = "My Quick Fix"
    
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
        // Apply fix to element
    }
}
```

## Refactoring API

### Custom Refactoring Handler
Create custom refactoring handlers.

```kotlin
class MyRefactoringHandler : RefactoringActionHandler {
    override fun invoke(
        project: Project,
        elements: Array<PsiElement>,
        dataContext: DataContext
    ) {
        val element = elements.firstOrNull() ?: return
        // Perform refactoring
    }
}
```

### Rename Processor
Custom rename processing.

```kotlin
class MyRenameProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        return element is MoveNamedElement
    }
    
    override fun prepareRenaming(
        element: PsiElement,
        newName: String,
        allRenames: MutableMap<PsiElement, String>
    ) {
        // Add additional elements to rename
    }
}
```

## Type System API

### MoveType Hierarchy
```kotlin
sealed class MoveType {
    data class Primitive(val kind: PrimitiveKind) : MoveType()
    data class Struct(
        val module: String,
        val name: String,
        val typeArgs: List<MoveType>
    ) : MoveType()
    data class Vector(val elementType: MoveType) : MoveType()
    data class Reference(
        val mutable: Boolean,
        val type: MoveType
    ) : MoveType()
    data class TypeParameter(val name: String) : MoveType()
}

enum class PrimitiveKind {
    U8, U16, U32, U64, U128, U256,
    BOOL, ADDRESS, SIGNER
}

enum class Ability {
    COPY, DROP, STORE, KEY
}
```

### Type Resolution
```kotlin
fun resolveType(typeElement: MoveTypeElement): MoveType? {
    val typeEngine = project.service<MoveTypeInferenceEngine>()
    return typeEngine.inferType(typeElement)
}
```

## Testing API

### Test Configuration
```kotlin
class MoveTestRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<MoveTestRunConfigurationOptions>(
    project, factory, name
) {
    fun getTestFilter(): String?
    fun setTestFilter(filter: String?)
    fun getWorkingDirectory(): String?
}
```

### Test Runner
```kotlin
class MoveTestRunner {
    fun runTests(
        configuration: MoveTestRunConfiguration,
        executor: Executor
    ): RunContentDescriptor
    
    fun parseTestResults(output: String): TestResults
}
```

## Debugging API

### Debug Process
```kotlin
class MoveDebugProcess(
    session: XDebugSession,
    connection: DebuggerConnection
) : XDebugProcess(session) {
    override fun startStepOver(context: XSuspendContext?)
    override fun startStepInto(context: XSuspendContext?)
    override fun stop()
    override fun resume(context: XSuspendContext?)
}
```

### Breakpoint Handler
```kotlin
class MoveLineBreakpointHandler : XBreakpointHandler<XLineBreakpoint<MoveBreakpointProperties>>() {
    override fun createBreakpoint(
        project: Project,
        file: VirtualFile,
        line: Int
    ): XLineBreakpoint<MoveBreakpointProperties>?
}
```

## Utility Classes

### MoveElementFactory
Create PSI elements programmatically.

```kotlin
object MoveElementFactory {
    fun createIdentifier(project: Project, name: String): PsiElement
    fun createType(project: Project, typeText: String): MoveTypeElement
    fun createExpression(project: Project, text: String): MoveExpression
    fun createStatement(project: Project, text: String): MoveStatement
    fun createFunction(
        project: Project,
        name: String,
        parameters: List<Pair<String, String>>,
        returnType: String?,
        body: String
    ): MoveFunction
}
```

### MovePsiUtil
Utility functions for PSI manipulation.

```kotlin
object MovePsiUtil {
    fun findModule(element: PsiElement): MoveModule?
    fun findFunction(element: PsiElement): MoveFunction?
    fun getContainingFile(element: PsiElement): MoveFile?
    fun isTestFunction(function: MoveFunction): Boolean
    fun isEntryFunction(function: MoveFunction): Boolean
}
```

## Extension Points

### Plugin Extension Points
Register custom implementations in `plugin.xml`:

```xml
<extensions defaultExtensionNs="com.intellij">
    <!-- Completion -->
    <completion.contributor 
        language="Move"
        implementationClass="com.example.MyCompletionContributor"/>
    
    <!-- Inspection -->
    <localInspection
        language="Move"
        displayName="My Inspection"
        groupName="Move"
        enabledByDefault="true"
        implementationClass="com.example.MyInspection"/>
    
    <!-- Refactoring -->
    <refactoring.moveHandler
        language="Move"
        implementationClass="com.example.MyRefactoringHandler"/>
    
    <!-- Type Provider -->
    <lang.typeProvider
        language="Move"
        implementationClass="com.example.MyTypeProvider"/>
</extensions>
```

## Event Handling

### PSI Change Listener
```kotlin
class MyPsiTreeChangeListener : PsiTreeChangeAdapter() {
    override fun childAdded(event: PsiTreeChangeEvent) {
        if (event.child is MoveElement) {
            // Handle Move element addition
        }
    }
    
    override fun childrenChanged(event: PsiTreeChangeEvent) {
        // Handle changes
    }
}

// Register listener
project.messageBus.connect().subscribe(
    PsiManager.PSI_TREE_CHANGE_TOPIC,
    MyPsiTreeChangeListener()
)
```

### Type Cache Events
```kotlin
interface TypeCacheListener {
    fun cacheInvalidated()
    fun typeResolved(element: PsiElement, type: MoveType)
}

// Subscribe to type cache events
project.messageBus.connect().subscribe(
    TYPE_CACHE_TOPIC,
    object : TypeCacheListener {
        override fun cacheInvalidated() {
            // Handle cache invalidation
        }
    }
)
```

## Best Practices

### Performance
1. **Use Type Cache**: Always check cache before computing types
2. **Batch Operations**: Group PSI modifications
3. **Smart PSI**: Use `SmartPsiElementPointer` for long-lived references
4. **Indexing**: Use file-based indexes for cross-file lookups

### Thread Safety
1. **Read Action**: Always read PSI in read action
2. **Write Action**: Modify PSI only in write action
3. **Application Thread**: UI updates on EDT
4. **Background Tasks**: Use `ProgressManager` for long operations

### Error Handling
```kotlin
fun safePsiOperation(element: PsiElement): Result? {
    return ApplicationManager.getApplication().runReadAction<Result> {
        try {
            if (element.isValid) {
                // Perform operation
                computeResult(element)
            } else null
        } catch (e: ProcessCanceledException) {
            throw e // Re-throw to respect cancellation
        } catch (e: Exception) {
            logger.error("Operation failed", e)
            null
        }
    }
}
```

## Examples

### Complete Example: Custom Completion Provider
```kotlin
class FrameworkModuleCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val module = MovePsiUtil.findModule(position) ?: return
        
        val framework = project.service<SuiFrameworkLibrary>()
        framework.getFrameworkModules().forEach { frameworkModule ->
            result.addElement(
                LookupElementBuilder.create(frameworkModule.name)
                    .withIcon(MoveIcons.MODULE)
                    .withTypeText("Framework Module")
                    .withTailText(" from ${frameworkModule.address}")
                    .withInsertHandler { context, _ ->
                        // Custom insert handling
                        val document = context.document
                        document.insertString(
                            context.tailOffset,
                            "::${frameworkModule.name}"
                        )
                    }
            )
        }
    }
}
```

---

For more examples and the latest API updates, visit the [plugin repository](https://github.com/sui-move-language/intellij-plugin).
