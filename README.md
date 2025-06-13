# Sui Move Language Support for JetBrains IDEs

A comprehensive IDE plugin for the Move programming language, specifically tailored for Sui blockchain development.

## Installation

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

### From JetBrains Marketplace (Coming Soon)
1. Open your JetBrains IDE (IntelliJ IDEA, CLion, etc.)
2. Go to `Settings/Preferences` → `Plugins`
3. Search for "Sui Move Language"
4. Click `Install`

<!-- Plugin description -->
Sui Move Language Support is a powerful JetBrains IDE plugin that provides comprehensive support for the Move programming language, specifically optimized for Sui blockchain development.

This plugin offers:
- Full syntax highlighting and code parsing for Move files
- Intelligent code completion with context awareness
- Real-time error detection and semantic analysis
- Code navigation and refactoring capabilities
- Integration with Sui CLI tools
- Project templates and file templates
- Customizable code formatting

Whether you're building smart contracts, developing dApps, or exploring the Sui blockchain, this plugin enhances your development experience with professional IDE features tailored for Move development.
<!-- Plugin description end -->

## Features

### Core Language Support
- **Syntax Highlighting**: Full syntax highlighting for Move keywords, types, functions, and literals
- **Code Parsing**: Robust parser with error recovery for Move language constructs
- **Error Detection**: Real-time syntax and semantic error detection with inline highlighting
- **Code Formatting**: Automatic code formatting with customizable style settings

### Code Intelligence
- **Code Completion**: Context-aware completion for:
  - Keywords and language constructs
  - Types (primitives, vectors, structs)
  - Functions and methods
  - Module members
  - Built-in functions
- **Semantic Analysis**: 
  - Type checking and inference
  - Function signature validation
  - Reference resolution
  - Move-specific validations

### Navigation & Refactoring
- **Go to Definition**: Navigate to symbol definitions with Ctrl/Cmd+Click
- **Find Usages**: Find all usages of symbols across your project
- **Structure View**: Hierarchical view of modules, functions, and structs
- **Rename Refactoring**: Safely rename symbols with automatic reference updates
- **File Templates**: Quick creation of Move modules and scripts

### Code Quality Tools
- **Inspections**:
  - Unused variable detection
  - Naming convention violations
  - Type mismatches
  - Missing imports
- **Intention Actions**:
  - Add type annotations
  - Convert functions to public
  - Import missing modules
- **Live Templates**: Code snippets for common patterns:
  - `fun` - Create a function
  - `pubfun` - Create a public function
  - `entry` - Create an entry function
  - `struct` - Create a struct
  - `module` - Create a module
  - `test` - Create a test function

### Sui Integration
- **Sui CLI Integration**: Direct integration with Sui CLI commands
- **Build Support**: Build projects with error highlighting
- **Test Runner**: Run Move tests from the IDE
- **Deploy Support**: Deploy modules to Sui network
- **Compiler Error Parsing**: Display Move compiler errors inline

### Editor Features
- **Brace Matching**: Automatic brace pairing and highlighting
- **Comment Support**: Line and block comment shortcuts
- **Quote Handling**: Smart quote completion
- **Code Folding**: Collapse/expand code blocks
- **Parameter Hints**: Display parameter names in function calls

## Configuration

### Sui CLI Path
1. Go to `Settings/Preferences` → `Tools` → `Sui Move`
2. Set the path to your Sui CLI executable
3. If Sui is in your PATH, you can leave this empty

### Code Style
1. Go to `Settings/Preferences` → `Editor` → `Code Style` → `Move`
2. Configure indentation, spacing, and other formatting options

## Usage

### Creating a New Move Project
1. `File` → `New` → `Project`
2. Select "Move" as the project type
3. Configure project settings

### Working with Move Files
- Create new Move files: Right-click in project → `New` → `Move File`
- Use code completion: Start typing and press `Ctrl+Space`
- Navigate to definitions: `Ctrl/Cmd+Click` on symbols
- Find usages: Right-click on symbol → `Find Usages`
- Refactor: Right-click on symbol → `Refactor` → `Rename`

### Running Sui Commands
- Open the Move tool window (bottom panel)
- Use buttons for Build, Test, and Deploy
- View command output in the tool window

### Using Live Templates
Type the abbreviation and press `Tab`:
- `fun` + Tab → Creates a function template
- `struct` + Tab → Creates a struct template
- `module` + Tab → Creates a module template

## Development

### Building from Source
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

### Project Structure
```
src/main/
├── kotlin/com/suimove/intellij/
│   ├── lexer/          # Lexical analysis
│   ├── parser/         # Syntax parsing
│   ├── psi/            # PSI elements and references
│   ├── highlighting/   # Syntax highlighting
│   ├── completion/     # Code completion
│   ├── annotator/      # Error annotations
│   ├── formatter/      # Code formatting
│   ├── actions/        # IDE actions
│   ├── settings/       # Plugin settings
│   ├── analysis/       # Semantic analysis
│   ├── compiler/       # Compiler integration
│   ├── inspections/    # Code inspections
│   ├── intentions/     # Intention actions
│   └── refactoring/    # Refactoring support
└── resources/
    ├── META-INF/       # Plugin configuration
    ├── fileTemplates/  # File templates
    ├── liveTemplates/  # Live templates
    └── messages/       # Internationalization
```

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Setup
1. Install IntelliJ IDEA (Community or Ultimate)
2. Install Java 17 or higher
3. Clone the repository
4. Import as Gradle project
5. Run `./gradlew runIde` to test

### Submitting Changes
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Sui Foundation for the Move language specification
- JetBrains for the IntelliJ Platform SDK
- Move community for feedback and contributions

## Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/sui-move-language/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/sui-move-language/discussions)
- **Documentation**: [Wiki](https://github.com/yourusername/sui-move-language/wiki)

## Roadmap

### Upcoming Features
- [ ] Debugger integration
- [ ] Performance profiling tools
- [ ] Visual dependency graphs
- [ ] Advanced security analysis
- [ ] Integration with Sui Explorer

### Version History
- **1.0.0** - Initial release with full language support
- **0.9.0** - Beta release with core features
- **0.5.0** - Alpha release with basic syntax support

---

Made with ❤️ for the Sui Move developer community
