# Sui Move Language Plugin - Version 2.0 Roadmap

## 🎯 Version 2.0 Goals
Transform the Sui Move Language plugin from a basic syntax highlighter to a comprehensive IDE experience for Sui blockchain development.

## 📋 Feature Categories & TODOs

### 1. Code Intelligence & Completion (Priority: HIGH)
- [ ] **Smart Code Completion**
  - [ ] Context-aware completion based on type inference
  - [ ] Auto-import suggestions when typing unimported symbols
  - [ ] Method/field completion for struct instances
  - [ ] Generic type parameter completion
  - [ ] Completion for Sui framework types (Object, TxContext, etc.)
- [ ] **Type System Integration**
  - [ ] Full type inference engine
  - [ ] Type checking with error highlighting
  - [ ] Generic type resolution
  - [ ] Ability constraint validation
- [ ] **Code Generation**
  - [ ] Generate test functions from function signatures
  - [ ] Generate getters/setters for struct fields
  - [ ] Generate module scaffolding
  - [ ] Generate common Sui patterns (entry functions, object creation)

### 2. Sui-Specific Features (Priority: HIGH)
- [ ] **Sui Framework Integration**
  - [ ] Built-in documentation for Sui framework modules
  - [ ] Sui-specific code inspections (transfer, share_object usage)
  - [ ] Object capability validation
  - [ ] TxContext parameter validation
- [ ] **Smart Contract Tools**
  - [ ] Deploy contracts directly from IDE
  - [ ] Transaction builder UI
  - [ ] Gas estimation display
  - [ ] Network switcher (devnet/testnet/mainnet)
- [ ] **Sui CLI Integration**
  - [ ] Run sui move build with error parsing
  - [ ] Run sui move test with result visualization
  - [ ] Package publishing assistant
  - [ ] Account management UI

### 3. Testing & Debugging (Priority: HIGH)
- [ ] **Test Runner**
  - [ ] Gutter icons to run individual tests
  - [ ] Test results tool window
  - [ ] Test coverage visualization
  - [ ] Test failure navigation
  - [ ] Re-run failed tests
- [ ] **Debugger Support**
  - [ ] Set breakpoints in Move code
  - [ ] Step through execution
  - [ ] Variable inspection
  - [ ] Call stack visualization
  - [ ] Conditional breakpoints
- [ ] **Testing Utilities**
  - [ ] Test data generators
  - [ ] Mock object creation
  - [ ] Test scenario templates

### 4. Refactoring & Code Quality (Priority: MEDIUM)
- [ ] **Advanced Refactoring**
  - [ ] Extract function/module
  - [ ] Inline variable/function
  - [ ] Move declarations between modules
  - [ ] Rename with usage search across project
  - [ ] Safe delete with usage checking
- [ ] **Code Inspections**
  - [ ] Unused imports/variables/functions
  - [ ] Unreachable code detection
  - [ ] Security vulnerability checks
  - [ ] Gas optimization suggestions
  - [ ] Best practices enforcement
- [ ] **Quick Fixes**
  - [ ] Import missing modules
  - [ ] Remove unused imports
  - [ ] Fix visibility modifiers
  - [ ] Add missing type annotations
  - [ ] Convert between hex/decimal addresses

### 5. Project Management (Priority: MEDIUM)
- [ ] **Move.toml Enhancement**
  - [ ] Visual dependency editor
  - [ ] Dependency conflict resolution
  - [ ] Version upgrade assistant
  - [ ] Add dependencies from registry
- [ ] **Multi-Module Support**
  - [ ] Module dependency graph
  - [ ] Cross-module refactoring
  - [ ] Module templates
  - [ ] Package structure validation
- [ ] **Build System**
  - [ ] Custom build configurations
  - [ ] Pre/post build tasks
  - [ ] Build profiles (dev/prod)

### 6. Developer Experience (Priority: MEDIUM)
- [ ] **Documentation**
  - [ ] Quick documentation (Ctrl+Q) for all symbols
  - [ ] External documentation links
  - [ ] Inline parameter hints
  - [ ] Code examples in documentation
- [ ] **Code Formatting**
  - [ ] Configurable formatting rules
  - [ ] Format on save option
  - [ ] Import optimization
  - [ ] Code style settings UI
- [ ] **Templates & Snippets**
  - [ ] Sui-specific live templates
  - [ ] File templates for common patterns
  - [ ] Postfix templates
  - [ ] Surround templates

### 7. Integration & Ecosystem (Priority: LOW)
- [ ] **Version Control**
  - [ ] Git integration for Move files
  - [ ] Diff viewer for Move code
  - [ ] Merge conflict resolution
- [ ] **External Tools**
  - [ ] Integration with Move Prover
  - [ ] Integration with Move coverage tools
  - [ ] Export to Move playground
- [ ] **Package Registry**
  - [ ] Browse Sui package registry
  - [ ] Install packages from IDE
  - [ ] Publish packages with wizard

## 🚀 Implementation Phases

### Phase 1: Core Intelligence (2-3 months)
1. Type inference engine
2. Smart code completion
3. Sui framework integration
4. Basic test runner

### Phase 2: Developer Tools (2-3 months)
1. Debugger support
2. Advanced refactoring
3. Code inspections
4. Move.toml UI

### Phase 3: Ecosystem Integration (1-2 months)
1. Sui CLI full integration
2. Deploy & transaction tools
3. Package registry
4. Documentation system

## 📊 Success Metrics
- [ ] 90%+ code completion accuracy
- [ ] < 100ms completion response time
- [ ] Zero false-positive inspections
- [ ] 95%+ test coverage
- [ ] 5-star marketplace rating

## 🎯 Version 2.0 Release Criteria
- All HIGH priority features implemented
- Comprehensive test coverage
- Performance benchmarks met
- Documentation complete
- Community beta testing completed

## 📅 Estimated Timeline
- **Start Date**: June 2025
- **Beta Release**: September 2025
- **Final Release**: October 2025

---

*This roadmap is a living document and will be updated based on community feedback and technical discoveries.*
