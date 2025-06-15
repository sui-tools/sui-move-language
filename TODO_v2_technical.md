# Sui Move Language Plugin v2.0 - Technical Implementation TODOs

## 🔧 Core Architecture Improvements

### Type System Foundation
- [ ] Create `MoveTypeInferenceEngine` class
  - [ ] Implement type variable resolution
  - [ ] Add generic type parameter tracking
  - [ ] Create type constraint solver
  - [ ] Add ability constraint checking
- [ ] Create `MoveTypeCache` service
  - [ ] Cache resolved types per PSI element
  - [ ] Implement cache invalidation on file changes
  - [ ] Add type hierarchy indexing
- [ ] Implement `MoveTypeAnnotator`
  - [ ] Highlight type mismatches
  - [ ] Show inferred types in tooltips
  - [ ] Add type hints for let bindings

### Code Completion Engine
- [ ] Extend `MoveCompletionContributor`
  - [ ] Add `MoveTypeAwareCompletionProvider`
  - [ ] Implement `MoveFunctionCompletionProvider`
  - [ ] Create `MoveStructFieldCompletionProvider`
  - [ ] Add `MoveImportCompletionProvider`
- [ ] Create `MoveCompletionContext`
  - [ ] Track current scope and available symbols
  - [ ] Implement visibility checking
  - [ ] Add generic parameter context
- [ ] Implement completion features:
  - [ ] Chain completion (e.g., `object.field.method`)
  - [ ] Smart casting in completion
  - [ ] Postfix completion templates
  - [ ] Parameter info while typing

### Sui Framework Integration
- [ ] Create `SuiFrameworkLibrary` service
  - [ ] Bundle Sui framework sources
  - [ ] Index framework modules and types
  - [ ] Provide quick documentation
- [ ] Implement `SuiSpecificInspections`
  - [ ] `TransferObjectInspection` - validate transfer/freeze/share
  - [ ] `TxContextUsageInspection` - ensure proper TxContext usage
  - [ ] `EntryFunctionInspection` - validate entry function signatures
  - [ ] `ObjectCapabilityInspection` - check key/store abilities
- [ ] Add Sui-specific intentions:
  - [ ] "Convert to entry function"
  - [ ] "Add TxContext parameter"
  - [ ] "Generate transfer function"

## 🧪 Testing Infrastructure

### Test Runner Implementation
- [ ] Create `MoveTestRunConfiguration`
  - [ ] Extend `RunConfigurationBase`
  - [ ] Add test filtering options
  - [ ] Support test parameters
- [ ] Implement `MoveTestRunner`
  - [ ] Parse test output from `sui move test`
  - [ ] Create test result tree
  - [ ] Support re-running failed tests
- [ ] Add test UI components:
  - [ ] Gutter icons for test functions
  - [ ] Test results tool window
  - [ ] Test progress indicator
  - [ ] Coverage visualization

### Debugger Support
- [ ] Create `MoveDebuggerSupport`
  - [ ] Implement `XDebuggerSupport`
  - [ ] Add Move-specific breakpoint types
  - [ ] Create stack frame representation
- [ ] Implement `MoveDebugProcess`
  - [ ] Connect to Move VM debugger
  - [ ] Handle stepping commands
  - [ ] Manage breakpoint synchronization
- [ ] Create debugger UI:
  - [ ] Variable view with Move type rendering
  - [ ] Expression evaluation dialog
  - [ ] Memory view for objects

## 🛠️ Refactoring Tools

### Extract Function Refactoring
- [ ] Create `MoveExtractFunctionHandler`
  - [ ] Analyze selected code for dependencies
  - [ ] Determine required parameters
  - [ ] Generate function signature
  - [ ] Handle return values
- [ ] Implement conflict detection:
  - [ ] Check for variable captures
  - [ ] Validate control flow
  - [ ] Ensure type safety

### Rename Refactoring Enhancement
- [ ] Extend `MoveRenameHandler`
  - [ ] Add cross-module rename support
  - [ ] Implement usage search in comments
  - [ ] Add preview dialog
  - [ ] Support batch renaming

## 📦 Project Management

### Move.toml Editor
- [ ] Create `MoveTomlFileEditor`
  - [ ] Implement form-based UI
  - [ ] Add dependency management tab
  - [ ] Create address management section
- [ ] Implement `MoveDependencyResolver`
  - [ ] Resolve git dependencies
  - [ ] Handle version conflicts
  - [ ] Download and cache dependencies
- [ ] Add dependency features:
  - [ ] Auto-complete for known packages
  - [ ] Version update notifications
  - [ ] Dependency graph visualization

### Build System Integration
- [ ] Create `MoveBuildConfiguration`
  - [ ] Support multiple build profiles
  - [ ] Add pre/post build tasks
  - [ ] Implement incremental builds
- [ ] Enhance `MoveCompilerService`
  - [ ] Add build caching
  - [ ] Implement parallel compilation
  - [ ] Create build progress reporting

## 🔍 Code Analysis

### Advanced Inspections
- [ ] Implement performance inspections:
  - [ ] `ExpensiveOperationInLoopInspection`
  - [ ] `UnnecessaryCopyInspection`
  - [ ] `GasOptimizationInspection`
- [ ] Add security inspections:
  - [ ] `UnprotectedObjectAccessInspection`
  - [ ] `MissingAbilityCheckInspection`
  - [ ] `ReentrancyVulnerabilityInspection`
- [ ] Create code quality inspections:
  - [ ] `UnusedImportInspection`
  - [ ] `DeadCodeInspection`
  - [ ] `DuplicateCodeInspection`

### Quick Fixes
- [ ] Implement import fixes:
  - [ ] "Import module" for unresolved references
  - [ ] "Optimize imports" to remove unused
  - [ ] "Add use statement" with smart placement
- [ ] Add type-related fixes:
  - [ ] "Add type annotation"
  - [ ] "Change type to match"
  - [ ] "Add generic parameter"

## 🎨 UI/UX Enhancements

### Tool Windows
- [ ] Create "Sui Move" tool window:
  - [ ] Account management tab
  - [ ] Network selection
  - [ ] Transaction history
  - [ ] Package explorer
- [ ] Enhance structure view:
  - [ ] Add sorting options
  - [ ] Show visibility modifiers
  - [ ] Include generic parameters

### Editor Enhancements
- [ ] Add parameter hints:
  - [ ] Show parameter names inline
  - [ ] Display types for complex expressions
  - [ ] Add hints for generic instantiations
- [ ] Implement code folding:
  - [ ] Fold use statements
  - [ ] Fold struct/function bodies
  - [ ] Custom folding regions

## 📊 Performance Optimizations

### Indexing Performance
- [ ] Optimize `MoveFileIndexer`
  - [ ] Implement incremental indexing
  - [ ] Add parallel processing
  - [ ] Create lightweight indices
- [ ] Add caching layers:
  - [ ] PSI element cache
  - [ ] Type resolution cache
  - [ ] Reference cache

### Memory Optimization
- [ ] Implement weak references for caches
- [ ] Add memory pressure listeners
- [ ] Create disposable services
- [ ] Optimize AST node creation

## 🧪 Testing Requirements

### Unit Tests
- [ ] Type inference tests (100+ cases)
- [ ] Completion tests (200+ scenarios)
- [ ] Inspection tests (50+ per inspection)
- [ ] Refactoring tests (comprehensive coverage)

### Integration Tests
- [ ] Full project compilation tests
- [ ] Multi-module navigation tests
- [ ] Debugger integration tests
- [ ] Performance benchmarks

### UI Tests
- [ ] Tool window interaction tests
- [ ] Editor feature tests
- [ ] Configuration dialog tests

## 📚 Documentation Tasks

### User Documentation
- [ ] Write comprehensive user guide
- [ ] Create video tutorials
- [ ] Add in-IDE tips and tricks
- [ ] Write troubleshooting guide

### Developer Documentation
- [ ] Document plugin architecture
- [ ] Create contribution guide
- [ ] Add API documentation
- [ ] Write testing guide

---

## 🚀 Getting Started

1. Set up development environment with IntelliJ IDEA 2024.1+
2. Clone the repository and import as Gradle project
3. Start with type system foundation
4. Implement core completion features
5. Add Sui-specific functionality
6. Comprehensive testing
7. Performance optimization
8. Documentation and release

Each TODO item should be created as a GitHub issue with appropriate labels and milestones.
