# Sui Move Language Support for JetBrains IDEs

[![CI](https://github.com/ravidsrk/sui-move-language/actions/workflows/ci.yml/badge.svg)](https://github.com/ravidsrk/sui-move-language/actions/workflows/ci.yml)
[![Code Quality](https://github.com/ravidsrk/sui-move-language/actions/workflows/code-quality.yml/badge.svg)](https://github.com/ravidsrk/sui-move-language/actions/workflows/code-quality.yml)
[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![License](https://img.shields.io/github/license/ravidsrk/sui-move-language.svg)](LICENSE)

<!-- Plugin description -->
A comprehensive IDE plugin for the Move programming language, specifically tailored for Sui blockchain development.

This plugin provides full language support for Move in JetBrains IDEs, including:
- Syntax highlighting and code analysis
- Smart code completion and navigation
- Integrated build tools and SDK management
- Project templates and file templates
- Real-time error detection and quick fixes
- Refactoring support and code formatting

Perfect for developers building smart contracts and applications on the Sui blockchain.
<!-- Plugin description end -->

## 🚀 Features

- **Syntax Highlighting**: Full syntax highlighting for Move files
- **Code Completion**: Smart code completion for Move constructs
- **Project Templates**: Quick project setup with Move module templates
- **Build Integration**: Build, test, and deploy Move modules from the IDE
- **Error Highlighting**: Real-time error detection and highlighting
- **Code Navigation**: Go to definition, find usages, and more
- **Live Templates**: Predefined code snippets for common patterns
- **Tool Window**: Dedicated Move tool window for build output

## 📦 Installation

### From JetBrains Marketplace (Coming Soon)
1. Open IntelliJ IDEA
2. Go to **File → Settings → Plugins → Marketplace**
3. Search for "Sui Move Language"
4. Click **Install**
5. Restart the IDE

### From Distribution File
1. Download the plugin distribution: `build/distributions/sui-move-language-0.1.0.zip`
2. In IntelliJ IDEA: **File → Settings → Plugins → ⚙️ → Install Plugin from Disk...**
3. Select the downloaded ZIP file
4. Restart the IDE

### From Source
```bash
# Clone the repository
git clone https://github.com/ravidsrk/sui-move-language.git
cd sui-move-language

# Build the plugin
./gradlew build -x test

# The plugin ZIP will be in: build/distributions/sui-move-language-0.1.0.zip

# Or run in sandbox IDE for testing
./gradlew runIde -x compileTestKotlin -x test
```

## 🛠️ Development

### Prerequisites
- JDK 17 or higher
- IntelliJ IDEA 2023.2 or higher
- Gradle 8.2 or higher

### Building
```bash
# Clone the repository
git clone https://github.com/ravidsrk/sui-move-language.git
cd sui-move-language

# Build the plugin
./gradlew buildPlugin

# Run tests
./gradlew test

# Run IDE with plugin for testing
./gradlew runIde
```

### Testing
The project includes a comprehensive test suite:
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.suimove.intellij.MoveAnnotatorTest"

# Run with test report
./gradlew test --scan
```

### Code Quality
```bash
# Run ktlint
./gradlew ktlintCheck

# Run detekt
./gradlew detekt

# Format code
./gradlew ktlintFormat
```

## 🔄 CI/CD

This project uses GitHub Actions for continuous integration:

- **CI Workflow**: Runs on every push and pull request
  - Executes all tests
  - Builds the plugin
  - Verifies plugin compatibility
  - Uploads test results and artifacts

- **Code Quality Workflow**: Checks code style and quality
  - Runs ktlint for Kotlin code style
  - Runs detekt for static analysis

- **Release Workflow**: Automated releases on version tags
  - Builds and tests the plugin
  - Creates GitHub releases
  - Optionally publishes to JetBrains Marketplace

## 📋 Requirements

- **IntelliJ Platform**: 2023.2 - 2024.1.*
- **JDK**: 17 or higher
- **Sui CLI**: Optional, for full Move development experience

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Sui Foundation for the Move language
- JetBrains for the IntelliJ Platform SDK
- All contributors to this project

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/ravidsrk/sui-move-language/issues)
- **Discussions**: [GitHub Discussions](https://github.com/ravidsrk/sui-move-language/discussions)
- **Documentation**: [Wiki](https://github.com/ravidsrk/sui-move-language/wiki)

---

**Note**: This plugin is currently in active development. Some features may be incomplete or subject to change.
