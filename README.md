# Move Language for Sui

[![CI](https://github.com/sui-tools/sui-move-language/actions/workflows/ci.yml/badge.svg)](https://github.com/sui-tools/sui-move-language/actions/workflows/ci.yml)
[![Code Quality](https://github.com/sui-tools/sui-move-language/actions/workflows/code-quality.yml/badge.svg)](https://github.com/sui-tools/sui-move-language/actions/workflows/code-quality.yml)
[![License](https://img.shields.io/github/license/sui-tools/sui-move-language.svg)](LICENSE)
[![Version](https://img.shields.io/jetbrains/plugin/v/27656.svg)](https://plugins.jetbrains.com/plugin/27656-move-language-for-sui)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/27656.svg)](https://plugins.jetbrains.com/plugin/27656-move-language-for-sui)

<!-- Plugin description -->
**Move Language for Sui** is a professional-grade IDE plugin for the Move programming language, specifically tailored for Sui blockchain development.

This plugin provides comprehensive language support for Move in JetBrains IDEs, featuring advanced type inference, intelligent code completion, integrated testing and debugging, and powerful refactoring tools. Whether you're building smart contracts, modules, or full applications on Sui, this plugin delivers the development experience you expect from a modern IDE.

Key highlights:
- **Advanced Type System**: Full type inference with generic support
- **Smart Code Completion**: Context-aware suggestions powered by type analysis
- **Integrated Testing**: Run and debug Move tests directly from the IDE
- **Professional Debugging**: Breakpoints, stepping, and variable inspection
- **Powerful Refactoring**: Extract function, inline, rename, and more
- **Sui Framework Integration**: Deep understanding of Sui-specific patterns

Perfect for developers building production-grade smart contracts and applications on the Sui blockchain.
<!-- Plugin description end -->

## 🚀 Features

### Core Language Support
- **Syntax Highlighting**: Full syntax highlighting with semantic coloring
- **Advanced Type Inference**: Complete type system with generic type resolution
- **Smart Code Completion**: 
  - Type-aware completions
  - Struct field suggestions
  - Function parameter hints
  - Import assistance
  - Sui framework API completions
- **Code Navigation**: 
  - Go to definition/implementation
  - Find usages with scope filtering
  - Structure view and file structure popup
  - Quick documentation lookup

### Development Tools
- **Integrated Test Runner**:
  - Run individual tests or test modules
  - Test coverage visualization
  - Test results tree view
  - Failure stack traces with navigation
- **Full Debugging Support**:
  - Breakpoints and conditional breakpoints
  - Step through code execution
  - Variable and expression evaluation
  - Call stack navigation
  - Watch expressions
- **Build Integration**: 
  - Build, test, and deploy from the IDE
  - Sui CLI integration
  - Package management
  - Deploy to devnet/testnet/mainnet

### Code Quality
- **Real-time Inspections**:
  - Type checking
  - Unused code detection
  - Sui-specific best practices
  - Security vulnerability detection
- **Quick Fixes**:
  - Auto-import suggestions
  - Type mismatch corrections
  - Missing ability additions
  - Code generation helpers
- **Code Formatting**:
  - Configurable formatting rules
  - Format on save
  - Optimize imports

### Refactoring Tools
- **Extract Function**: Extract code into reusable functions
- **Inline**: Inline variables and functions
- **Rename**: Safe rename with reference updates
- **Move**: Move definitions between modules
- **Safe Delete**: Delete with usage checking
- **Change Signature**: Update function signatures

### Sui-Specific Features
- **Framework Integration**: Deep knowledge of Sui framework modules
- **Object Model Support**: Understand Sui's object system
- **Transaction Building**: Visual transaction builder
- **Gas Estimation**: Estimate gas costs before deployment
- **Network Integration**: Switch between networks seamlessly

## 📦 Installation

### From JetBrains Marketplace
1. Open IntelliJ IDEA (2023.1 or later)
2. Go to **Settings/Preferences → Plugins → Marketplace**
3. Search for "Move Language for Sui"
4. Click **Install**
5. Restart the IDE

Or install directly from: [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/27656-move-language-for-sui)

### System Requirements
- IntelliJ IDEA 2023.1+ (Community or Ultimate)
- JDK 17 or higher
- Sui CLI (optional, for full functionality)

## 🎯 Quick Start

### Create a New Move Project
1. **File → New → Project**
2. Select **Move** from the left panel
3. Choose **Sui Move Project**
4. Configure project settings
5. Click **Create**

### Import Existing Project
1. **File → Open**
2. Select your Move project directory
3. The plugin will auto-detect Move.toml

### Running Tests
- Right-click on a test function → **Run 'test_name'**
- Click the gutter icon next to test functions
- Use **Run → Run...** for more options

### Debugging
1. Set breakpoints by clicking the gutter
2. Right-click test → **Debug 'test_name'**
3. Use the debug tool window to control execution

## ⚙️ Configuration

### Plugin Settings
**Settings/Preferences → Languages & Frameworks → Move**

- **Sui CLI Path**: Configure custom Sui CLI location
- **Default Network**: Set default network (devnet/testnet/mainnet)
- **Type Inference**: Configure inference depth and caching
- **Code Style**: Customize formatting rules
- **Inspections**: Enable/disable specific inspections

### Performance Tuning
For large projects, optimize performance:
- Increase memory: `-Xmx4096m` in IDE VM options
- Enable type cache: Settings → Move → Performance
- Configure indexing scope in Project Structure

## 🛠️ Development

### Building from Source
```bash
git clone https://github.com/sui-tools/sui-move-language.git
cd sui-move-language
./gradlew buildPlugin
```

### Running in Development
```bash
./gradlew runIde
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## 📚 Documentation

- [Installation Guide](INSTALLATION.md)
- [Performance Optimization](PERFORMANCE_OPTIMIZATION.md)
- [Release Notes](RELEASE_NOTES_v2.0.0.md)
- [Contributing Guide](CONTRIBUTING.md)

## 🐛 Reporting Issues

Found a bug? Please report it on our [issue tracker](https://github.com/sui-tools/sui-move-language/issues) with:
- Plugin version
- IDE version
- Steps to reproduce
- Expected vs actual behavior

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Sui Foundation for the Move language and Sui blockchain
- JetBrains for the IntelliJ Platform SDK
- The Move and Sui developer community

## 🌟 Star History

[![Star History Chart](https://api.star-history.com/svg?repos=sui-tools/sui-move-language&type=Date)](https://star-history.com/#sui-tools/sui-move-language&Date)

---

**Happy coding with Sui Move!** 🚀
