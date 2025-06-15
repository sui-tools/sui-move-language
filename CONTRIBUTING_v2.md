# Contributing to Sui Move Language Plugin v2.0

## 🚀 Quick Start for v2.0 Development

### Prerequisites
- IntelliJ IDEA 2024.1+ (Ultimate or Community)
- JDK 17+
- Gradle 8.5+
- Git

### Setting Up Development Environment

1. **Fork and Clone**
   ```bash
   git clone https://github.com/YOUR_USERNAME/sui-move-language.git
   cd sui-move-language
   git checkout -b v2.0-development
   ```

2. **Import Project**
   - Open IntelliJ IDEA
   - File → Open → Select the project directory
   - Import as Gradle project

3. **Configure SDK**
   - File → Project Structure → Project
   - Set Project SDK to JDK 17
   - Set Language Level to 17

4. **Run Configuration**
   - Run → Edit Configurations
   - Add → Gradle
   - Tasks: `runIde`
   - This launches a sandboxed IDE instance

### Development Workflow

1. **Pick a Task**
   - Check [TODO_v2_technical.md](TODO_v2_technical.md)
   - Create/claim a GitHub issue
   - Create a feature branch

2. **Implement Feature**
   - Follow existing code patterns
   - Add comprehensive tests
   - Update documentation

3. **Testing**
   ```bash
   ./gradlew test          # Run all tests
   ./gradlew runIde        # Test in IDE
   ./gradlew verifyPlugin  # Verify compatibility
   ```

4. **Submit PR**
   - Ensure all tests pass
   - Update CHANGELOG.md
   - Reference the issue number

## 📁 Project Structure for v2.0

```
src/main/kotlin/com/suimove/intellij/
├── completion/          # Code completion providers
│   ├── MoveTypeAwareCompletionProvider.kt
│   ├── MoveFunctionCompletionProvider.kt
│   └── MoveImportCompletionProvider.kt
├── typing/             # Type system
│   ├── MoveTypeInferenceEngine.kt
│   ├── MoveTypeCache.kt
│   └── MoveTypeAnnotator.kt
├── sui/                # Sui-specific features
│   ├── SuiFrameworkLibrary.kt
│   ├── inspections/
│   └── intentions/
├── testing/            # Test runner
│   ├── MoveTestRunConfiguration.kt
│   └── MoveTestRunner.kt
├── debugger/           # Debugger support
│   ├── MoveDebuggerSupport.kt
│   └── MoveDebugProcess.kt
└── refactoring/        # Refactoring handlers
    ├── MoveExtractFunctionHandler.kt
    └── MoveRenameHandler.kt
```

## 🧪 Testing Guidelines

### Unit Tests
- Place tests in `src/test/kotlin`
- Mirror the main source structure
- Use `MoveTestBase` for common setup
- Aim for 90%+ coverage

### Test Data
- Place test Move files in `src/test/testData`
- Use descriptive names
- Include both positive and negative cases

### Example Test
```kotlin
class MoveTypeInferenceTest : MoveTestBase() {
    fun testStructFieldTypeInference() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Coin<T> { value: u64 }
                fun test(c: Coin<SUI>) {
                    let v = c.val<caret>ue;
                }
            }
        """.trimIndent())
        
        val element = myFixture.elementAtCaret
        val type = MoveTypeInferenceEngine.inferType(element)
        assertEquals("u64", type?.toString())
    }
}
```

## 🎨 Code Style

### Kotlin Style
- Follow official Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions small and focused

### PSI Patterns
```kotlin
// Good: Use smart casts and null safety
val function = element.parent as? MoveFunction ?: return
val returnType = function.returnType

// Good: Use PSI patterns for navigation
val resolved = element.reference?.resolve() as? MoveNamedElement

// Good: Cache expensive computations
private val typeCache = CachedValuesManager.getCachedValue(element) {
    CachedValueProvider.Result.create(
        computeType(element),
        PsiModificationTracker.MODIFICATION_COUNT
    )
}
```

## 🔧 Debugging Tips

1. **Enable Internal Mode**
   - Help → Edit Custom Properties
   - Add: `idea.is.internal=true`
   - Restart IDE

2. **PSI Viewer**
   - Tools → View PSI Structure
   - Invaluable for understanding AST

3. **Log Debugging**
   ```kotlin
   import com.intellij.openapi.diagnostic.logger
   
   private val LOG = logger<MoveTypeInferenceEngine>()
   LOG.debug("Inferring type for: $element")
   ```

4. **Performance Profiling**
   - Use IDE's built-in profiler
   - Focus on indexing and completion

## 📚 Resources

### IntelliJ Platform
- [IntelliJ Platform SDK Docs](https://plugins.jetbrains.com/docs/intellij/)
- [PSI Cookbook](https://plugins.jetbrains.com/docs/intellij/psi-cookbook.html)
- [Custom Language Support](https://plugins.jetbrains.com/docs/intellij/custom-language-support.html)

### Move Language
- [Move Book](https://move-language.github.io/move/)
- [Sui Move Documentation](https://docs.sui.io/build/move)
- [Sui Framework Source](https://github.com/MystenLabs/sui/tree/main/crates/sui-framework)

### Community
- [Sui Discord](https://discord.gg/sui)
- [Plugin Development Forum](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development)

## 🤝 Getting Help

- Create a discussion on GitHub for design questions
- Join our Discord channel for real-time help
- Check existing issues for similar problems
- Don't hesitate to ask questions!

## 🎯 v2.0 Priorities

Focus areas for contributions:
1. **Type System** - Foundation for all advanced features
2. **Code Completion** - Most visible user feature
3. **Sui Integration** - Unique value proposition
4. **Test Runner** - Essential for developers

Happy coding! 🚀
