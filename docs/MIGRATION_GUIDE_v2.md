# Migration Guide: Sui Move Plugin v1 to v2

## Overview
This guide helps you migrate from Sui Move Language Plugin v1 to v2. Version 2 is a complete rewrite with significant improvements in performance, features, and stability.

## Breaking Changes

### Configuration Changes

#### Settings Location
- **v1**: Settings stored in `.idea/move.xml`
- **v2**: Settings in standard IntelliJ location
- **Action**: Settings will be migrated automatically on first run

#### Project Structure
- **v1**: Required `.move` folder
- **v2**: Uses `Move.toml` as project marker
- **Action**: No action needed if you have `Move.toml`

### API Changes

#### PSI Element Hierarchy
The PSI (Program Structure Interface) has been completely redesigned:

**v1 PSI Elements:**
```kotlin
// Old v1 interfaces
interface MvFunction : MvNamedElement
interface MvStruct : MvNamedElement
```

**v2 PSI Elements:**
```kotlin
// New v2 interfaces
interface MoveFunction : MoveNamedElement {
    fun getParameters(): List<MoveParameter>
    fun getReturnType(): MoveType?
    fun isEntry(): Boolean
    // More methods...
}
```

**Migration Steps:**
1. Update all references from `Mv*` to `Move*`
2. Use new method names (see API Reference)
3. Update visitor patterns

#### Type System
**v1**: Basic type checking
```kotlin
// v1 type checking
val type = element.getType() // Returns String
```

**v2**: Full type inference engine
```kotlin
// v2 type inference
val typeEngine = project.service<MoveTypeInferenceEngine>()
val type = typeEngine.inferType(element) // Returns MoveType
```

### Extension Point Changes

#### Completion Contributors
**v1**:
```xml
<completion.contributor
    implementationClass="com.mv.MvCompletionContributor"/>
```

**v2**:
```xml
<completion.contributor
    language="Move"
    implementationClass="com.suimove.MoveCompletionContributor"/>
```

#### Inspections
**v1**: Registered globally
**v2**: Language-specific registration required

## New Features in v2

### Type System
- Full type inference with generics
- Ability constraint checking
- Type-aware code completion
- Inline type hints

### Testing Support
- Integrated test runner
- Test result visualization
- Coverage support
- Test navigation

### Debugging
- Full debugging support
- Breakpoints and stepping
- Variable inspection
- Expression evaluation

### Refactoring
- Extract function
- Inline function/variable
- Safe delete with usage search
- Cross-module rename

### Sui-Specific Features
- Object capability validation
- Entry function checking
- Framework integration
- Quick fixes for common issues

## Migration Checklist

### For Users

- [ ] **Backup Settings**: Export v1 settings before upgrading
- [ ] **Update Plugin**: Uninstall v1, install v2
- [ ] **Verify CLI Path**: Check Sui CLI configuration
- [ ] **Reindex Project**: Invalidate caches after upgrade
- [ ] **Review Shortcuts**: Some shortcuts may have changed

### For Extension Developers

- [ ] **Update Dependencies**:
  ```kotlin
  dependencies {
      implementation("com.suimove:intellij-move-plugin:2.0.0")
  }
  ```

- [ ] **Update Package Names**:
  ```kotlin
  // Old
  import com.mv.psi.*
  import com.mv.utils.*
  
  // New
  import com.suimove.intellij.psi.*
  import com.suimove.intellij.utils.*
  ```

- [ ] **Update Extension Points**:
  ```xml
  <!-- Old -->
  <extensions defaultExtensionNs="com.mv">
  
  <!-- New -->
  <extensions defaultExtensionNs="com.suimove.intellij">
  ```

- [ ] **Update PSI Visitors**:
  ```kotlin
  // Old
  class MyVisitor : MvVisitor() {
      override fun visitFunction(fn: MvFunction) { }
  }
  
  // New
  class MyVisitor : MoveVisitor() {
      override fun visitFunction(fn: MoveFunction) { }
  }
  ```

- [ ] **Update Type Handling**:
  ```kotlin
  // Old - String-based types
  val type = element.getType()
  if (type == "u64") { }
  
  // New - Type objects
  val type = typeEngine.inferType(element)
  if (type is MoveType.Primitive && type.kind == PrimitiveKind.U64) { }
  ```

## Common Migration Issues

### Issue: "Cannot find symbol MvElement"
**Solution**: Update all `Mv*` references to `Move*`

### Issue: "Type checking not working"
**Solution**: Use the new `MoveTypeInferenceEngine` service

### Issue: "Completion not showing"
**Solution**: Ensure language is set to "Move" in extension points

### Issue: "Tests not running"
**Solution**: Configure Sui CLI path in settings

### Issue: "Old shortcuts not working"
**Solution**: Check keymap settings, some defaults have changed

## Performance Improvements

### Indexing
- v2 is 3x faster at indexing large projects
- Incremental indexing reduces memory usage

### Type Inference
- Cached type resolution
- Lazy computation of complex types

### Completion
- Faster response time
- More relevant suggestions

## Deprecation Timeline

### Deprecated in v2
- String-based type system
- Global inspection registration
- `.move` project markers

### Removed in v2
- Legacy PSI interfaces (`Mv*`)
- Old completion providers
- String-based type checking

## Getting Help

### Resources
- [User Guide](USER_GUIDE.md)
- [API Reference](API_REFERENCE.md)
- [GitHub Issues](https://github.com/sui-move-language/intellij-plugin/issues)

### Migration Support
- Join our Discord for migration help
- Check FAQ for common issues
- Report migration bugs with "migration" label

## Version Compatibility

| Plugin Version | IntelliJ Version | Sui CLI Version |
|----------------|------------------|-----------------|
| v1.x           | 2022.1+          | 0.x             |
| v2.0           | 2023.1+          | 1.0+            |

## Example: Migrating a Custom Inspection

### v1 Inspection
```kotlin
class MyInspectionV1 : BaseInspection() {
    override fun buildVisitor(holder: ProblemsHolder): PsiElementVisitor {
        return object : MvVisitor() {
            override fun visitFunction(fn: MvFunction) {
                if (fn.name?.startsWith("test") == true && 
                    !fn.hasAnnotation("test")) {
                    holder.registerProblem(fn, "Test function needs annotation")
                }
            }
        }
    }
}
```

### v2 Inspection
```kotlin
class MyInspectionV2 : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : MoveVisitor() {
            override fun visitFunction(fn: MoveFunction) {
                if (fn.name?.startsWith("test") == true && 
                    !fn.hasAttribute("test")) {
                    holder.registerProblem(
                        fn.nameIdentifier ?: fn,
                        "Test function needs #[test] annotation",
                        ProblemHighlightType.WARNING,
                        AddTestAnnotationQuickFix()
                    )
                }
            }
        }
    }
}
```

## Summary

Version 2 brings significant improvements:
- ✅ Complete type system
- ✅ Better performance
- ✅ More features
- ✅ Improved stability

While migration requires some code updates, the benefits far outweigh the effort. The new architecture provides a solid foundation for future enhancements.

---

*Last updated: December 2024*
