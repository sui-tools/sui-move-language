# Sui Move Language Plugin v2.0 Architecture

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        IntelliJ Platform API                         │
├─────────────────────────────────────────────────────────────────────┤
│                    Sui Move Language Plugin v2.0                     │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │   Core Layer    │  │  Language Layer  │  │   Sui Layer      │  │
│  │                 │  │                  │  │                  │  │
│  │ • PSI Parser    │  │ • Type System    │  │ • Framework Lib  │  │
│  │ • Lexer         │  │ • Completion     │  │ • CLI Bridge     │  │
│  │ • File Types    │  │ • References     │  │ • Deploy Tools   │  │
│  │ • Project Model │  │ • Inspections    │  │ • Test Runner    │  │
│  └─────────────────┘  └──────────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │   Tool Layer    │  │    UI Layer      │  │  Service Layer   │  │
│  │                 │  │                  │  │                  │  │
│  │ • Debugger      │  │ • Tool Windows   │  │ • Type Cache     │  │
│  │ • Profiler      │  │ • Editors        │  │ • Index Service  │  │
│  │ • Formatter     │  │ • Dialogs        │  │ • Build Service  │  │
│  │ • Refactoring   │  │ • Actions        │  │ • Network Client │  │
│  └─────────────────┘  └──────────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

## 📦 Component Architecture

### 1. Core Layer (Foundation)

#### PSI (Program Structure Interface) Tree
```
MoveFile
├── MoveModuleDeclaration
│   ├── MoveAddressBlock
│   ├── MoveIdentifier (module name)
│   └── MoveModuleBody
│       ├── MoveUseDeclaration[]
│       ├── MoveStructDeclaration[]
│       ├── MoveFunctionDeclaration[]
│       └── MoveConstDeclaration[]
├── MoveScriptDeclaration
└── MoveAddressBlock[]
```

#### Key Components
- **MoveLexer**: Token generation from source text
- **MoveParser**: AST construction using Grammar-Kit
- **MoveFileType**: File type registration and icons
- **MoveLanguage**: Language definition and configuration

### 2. Language Layer (Intelligence)

#### Type System Architecture
```
┌─────────────────────────────────────────┐
│         MoveTypeInferenceEngine         │
├─────────────────────────────────────────┤
│ • Type Variable Resolution              │
│ • Generic Type Instantiation            │
│ • Ability Constraint Checking           │
│ • Type Unification                      │
└─────────────────────────────────────────┘
              ↓ uses
┌─────────────────────────────────────────┐
│            MoveTypeCache                │
├─────────────────────────────────────────┤
│ • PSI → Type Mapping                    │
│ • Invalidation on Changes               │
│ • Hierarchical Type Index               │
└─────────────────────────────────────────┘
```

#### Completion System
```
MoveCompletionContributor
├── MoveTypeAwareCompletionProvider
│   ├── analyzeContext()
│   ├── filterByType()
│   └── rankByRelevance()
├── MoveFunctionCompletionProvider
│   ├── collectVisibleFunctions()
│   ├── generateSignatures()
│   └── addParameterInfo()
└── MoveImportCompletionProvider
    ├── findUnimportedSymbols()
    ├── generateImportStatements()
    └── optimizeImports()
```

### 3. Sui Layer (Blockchain Integration)

#### Sui Framework Integration
```
SuiFrameworkLibrary (Service)
├── Framework Sources
│   ├── 0x1::option
│   ├── 0x1::vector
│   ├── 0x2::object
│   ├── 0x2::transfer
│   ├── 0x2::tx_context
│   └── ... (all framework modules)
├── Documentation Index
└── Type Definitions
```

#### Sui CLI Bridge
```
SuiCliService
├── CommandExecutor
│   ├── build()
│   ├── test()
│   ├── publish()
│   └── call()
├── OutputParser
│   ├── parseErrors()
│   ├── parseTestResults()
│   └── parseGasReport()
└── NetworkManager
    ├── devnet
    ├── testnet
    └── mainnet
```

### 4. Tool Layer (Developer Tools)

#### Debugger Architecture
```
MoveDebuggerSupport
├── MoveDebugProcess
│   ├── Breakpoint Manager
│   ├── Stack Frame Handler
│   ├── Variable Evaluator
│   └── Step Controller
├── MoveDebuggerUI
│   ├── Variable View
│   ├── Stack View
│   └── Breakpoint View
└── MoveVMConnector
    └── Debug Protocol Handler
```

#### Refactoring Engine
```
MoveRefactoringSupport
├── Extract Function
│   ├── Flow Analysis
│   ├── Parameter Detection
│   └── Code Generation
├── Rename
│   ├── Usage Search
│   ├── Conflict Detection
│   └── Batch Update
└── Move Members
    ├── Dependency Analysis
    └── Import Update
```

## 🔄 Data Flow Architecture

### Type Resolution Flow
```
User Types Code
    ↓
Lexer → Parser → PSI Tree
    ↓
Type Inference Engine
    ↓
Type Cache (with invalidation)
    ↓
Editor Annotations & Completion
```

### Build & Test Flow
```
User Triggers Build/Test
    ↓
MoveBuildConfiguration
    ↓
SuiCliService.build/test
    ↓
Output Parser
    ↓
Error Highlighting / Test Results UI
```

## 💾 Service Architecture

### Application-Level Services
```kotlin
// Singleton services registered in plugin.xml
interface MoveTypeService : Service {
    fun inferType(element: PsiElement): MoveType?
    fun resolveGenericType(type: MoveType, context: PsiElement): MoveType
}

interface SuiFrameworkService : Service {
    fun getFrameworkModule(name: String): MoveModule?
    fun getDocumentation(element: PsiElement): String?
}

interface MoveBuildService : Service {
    fun buildProject(project: Project): BuildResult
    fun getLastBuildResult(): BuildResult?
}
```

### Project-Level Services
```kotlin
// Per-project services
interface MoveProjectService : Service {
    fun getProjectRoot(): VirtualFile
    fun getDependencies(): List<MoveDependency>
    fun getConfiguration(): MoveProjectConfig
}

interface MoveIndexService : Service {
    fun getModuleByAddress(address: String): List<MoveModule>
    fun findTypeByName(name: String): List<MoveType>
}
```

## 🔌 Extension Points

### Custom Extension Points
```xml
<extensionPoints>
  <!-- Type providers for custom types -->
  <extensionPoint name="moveTypeProvider"
                  interface="com.suimove.intellij.MoveTypeProvider"/>
  
  <!-- Custom inspections -->
  <extensionPoint name="moveInspectionProvider"
                  interface="com.suimove.intellij.MoveInspectionProvider"/>
  
  <!-- Sui network providers -->
  <extensionPoint name="suiNetworkProvider"
                  interface="com.suimove.intellij.SuiNetworkProvider"/>
</extensionPoints>
```

### IntelliJ Platform Extensions
```xml
<extensions defaultExtensionNs="com.intellij">
  <!-- Language -->
  <fileType implementation="...MoveFileType"/>
  <lang.parserDefinition implementation="...MoveParserDefinition"/>
  
  <!-- Code Insight -->
  <completion.contributor implementation="...MoveCompletionContributor"/>
  <lang.findUsagesProvider implementation="...MoveFindUsagesProvider"/>
  <lang.refactoringSupport implementation="...MoveRefactoringSupport"/>
  
  <!-- Debugging -->
  <programRunner implementation="...MoveDebugRunner"/>
  <xdebugger.breakpointType implementation="...MoveBreakpointType"/>
  
  <!-- UI -->
  <toolWindow id="Sui Move" implementation="...SuiToolWindowFactory"/>
  <projectConfigurable implementation="...MoveProjectConfigurable"/>
</extensions>
```

## 🏃 Runtime Architecture

### Threading Model
```
EDT (UI Thread)
├── User Actions
├── UI Updates
└── Quick Operations

Background Thread Pool
├── Type Inference
├── Index Building
├── Network Requests
└── Heavy Computations

Read Action
├── PSI Access
├── Index Queries
└── Safe Data Reading

Write Action
├── PSI Modifications
├── Document Changes
└── Project Updates
```

### Caching Strategy
```
Three-Level Cache Architecture:

1. Memory Cache (Fast)
   - Recent type resolutions
   - Active file indices
   - UI state

2. Persistent Cache (Medium)
   - Serialized type data
   - Framework documentation
   - Build artifacts

3. Network Cache (Slow)
   - Package registry data
   - Remote dependencies
   - Blockchain state
```

## 🔐 Security Architecture

### Secure Operations
- All network requests use HTTPS
- Private keys never stored in memory
- Sensitive data encrypted in persistent storage
- Sandboxed script execution

### Permission Model
- Read-only access to framework files
- Write access only to project files
- Network access requires user consent
- Deployment requires explicit confirmation

## 📊 Performance Considerations

### Optimization Strategies
1. **Lazy Loading**: Load only what's needed
2. **Incremental Processing**: Process only changes
3. **Parallel Computation**: Use all CPU cores
4. **Smart Caching**: Cache expensive operations
5. **Memory Management**: Release unused resources

### Performance Targets
- Completion: < 100ms response time
- Type inference: < 50ms for local types
- Indexing: < 5s for 1000 file project
- Memory: < 500MB for large projects

## 🧪 Testing Architecture

### Test Infrastructure
```
Test Framework
├── Unit Tests
│   ├── Parser Tests
│   ├── Type Tests
│   └── Completion Tests
├── Integration Tests
│   ├── Project Tests
│   ├── Debugger Tests
│   └── Refactoring Tests
└── Performance Tests
    ├── Memory Tests
    ├── Speed Tests
    └── Stress Tests
```

### Test Data Organization
```
testData/
├── parser/          # Parser test cases
├── completion/      # Completion scenarios
├── inspection/      # Inspection test cases
├── refactoring/     # Refactoring tests
├── sui/            # Sui-specific tests
└── performance/    # Performance benchmarks
```

---

This architecture provides a solid foundation for building a professional-grade IDE plugin that can scale with the growing Sui ecosystem.
