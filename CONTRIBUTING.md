# Contributing to Sui Move Language Plugin

Thank you for your interest in contributing to the Sui Move Language plugin! We welcome contributions from the community.

## 🚀 Getting Started

### Prerequisites
- IntelliJ IDEA 2023.1+ (Community or Ultimate)
- JDK 17 or higher
- Gradle 8.5 or higher
- Git

### Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/YOUR_USERNAME/sui-move-language.git
   cd sui-move-language
   git remote add upstream https://github.com/sui-tools/sui-move-language.git
   ```

2. **Import Project**
   - Open IntelliJ IDEA
   - File → Open → Select the project directory
   - Import as Gradle project

3. **Configure SDK**
   - File → Project Structure → Project
   - Set Project SDK to JDK 17
   - Set Language Level to 17

## 🛠️ Building and Testing

```bash
# Build the plugin
./gradlew buildPlugin

# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "com.suimove.intellij.*TestName"

# Run the plugin in a sandbox IDE
./gradlew runIde

# Check code style
./gradlew ktlintCheck

# Format code
./gradlew ktlintFormat
```

## 📁 Project Structure

```
src/
├── main/
│   ├── kotlin/com/suimove/intellij/
│   │   ├── actions/          # IDE actions
│   │   ├── analysis/         # Type analysis
│   │   ├── annotator/        # Syntax highlighting
│   │   ├── cli/              # Sui CLI integration
│   │   ├── completion/       # Code completion
│   │   ├── debugger/         # Debugging support
│   │   ├── inspections/      # Code inspections
│   │   ├── parser/           # Language parser
│   │   ├── psi/              # PSI elements
│   │   ├── refactoring/      # Refactoring tools
│   │   ├── services/         # Plugin services
│   │   └── testing/          # Test runner
│   ├── resources/
│   │   ├── META-INF/plugin.xml
│   │   └── messages/
│   └── gen/                  # Generated parser/lexer
└── test/
    ├── kotlin/               # Test files
    └── testData/             # Test fixtures
```

## 💻 Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused

### Testing Requirements
- Write unit tests for all new features
- Maintain test coverage above 80%
- Use test fixtures in `testData/`
- Test both positive and negative cases

### PSI and Parser Development
- Grammar files are in `src/main/grammars/`
- Generate parser/lexer: `./gradlew generateParser`
- PSI interfaces extend `MovePsiElement`
- Use `MoveElementFactory` for PSI creation

### Type System Development
- Type inference logic in `services/type/`
- Cache types using `MoveTypeCache`
- Handle generics and type parameters
- Support Sui-specific types

### Adding New Features

1. **Code Completion**
   - Extend `CompletionContributor`
   - Add provider to `MoveCompletionContributor`
   - Test with various contexts

2. **Inspections**
   - Extend `LocalInspectionTool`
   - Register in `plugin.xml`
   - Provide quick fixes when possible

3. **Refactoring**
   - Implement refactoring handler
   - Add UI if needed
   - Handle edge cases carefully

## 🐛 Debugging Tips

### Plugin Debugging
```bash
# Enable internal actions
idea.is.internal=true

# Add to Help | Diagnostic Tools | Debug Log Settings
#com.suimove.intellij
```

### Common Issues
- **Parser Issues**: Check grammar file and regenerate
- **Type Resolution**: Enable type cache logging
- **Performance**: Use built-in profiler

## 📝 Submitting Changes

### Pull Request Process

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**
   - Write clean, documented code
   - Add/update tests
   - Update documentation

3. **Commit Guidelines**
   ```bash
   git commit -m "feat: add new completion provider for struct fields"
   ```
   
   Use conventional commits:
   - `feat:` New feature
   - `fix:` Bug fix
   - `docs:` Documentation
   - `test:` Tests
   - `refactor:` Code refactoring
   - `perf:` Performance improvement

4. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```
   - Fill out PR template
   - Link related issues
   - Add screenshots for UI changes

### Review Process
- All PRs require at least one review
- CI must pass (tests, linting)
- Address review feedback promptly
- Squash commits before merging

## 📚 Resources

### Documentation
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Move Language Spec](https://github.com/move-language/move)
- [Sui Documentation](https://docs.sui.io/)

### Tools
- [Grammar-Kit](https://github.com/JetBrains/Grammar-Kit) - Parser generation
- [PsiViewer](https://plugins.jetbrains.com/plugin/227-psiviewer) - PSI debugging
- [IntelliJ Platform Explorer](https://plugins.jetbrains.com/intellij-platform-explorer/)

### Community
- [Sui Discord](https://discord.gg/sui) - #dev-tools channel
- [GitHub Issues](https://github.com/sui-tools/sui-move-language/issues)

## 🤝 Getting Help

- **Questions**: Open a GitHub Discussion
- **Bugs**: Create a GitHub Issue
- **Ideas**: Share in Discussions
- **Chat**: Join Sui Discord

## 📄 License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT).

---

Thank you for contributing to the Sui Move Language plugin! Your efforts help make Move development better for everyone. 🚀
