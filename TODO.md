# Move Language Plugin - TODO List

## Priority 1: Complete Phase 2 Features (5% remaining)

### Enhanced Refactoring Support
- [ ] Implement "Extract Function" refactoring for Move code blocks
- [ ] Add "Extract Constant" refactoring for literal values
- [ ] Implement "Inline Function" refactoring
- [ ] Add "Change Signature" refactoring with parameter updates
- [ ] Create comprehensive refactoring tests

### Advanced Navigation Features
- [ ] Implement "Go to Super" for trait implementations
- [ ] Add "Go to Type Declaration" for complex types
- [ ] Implement breadcrumbs navigation for nested structures
- [ ] Add "Related Symbol" navigation for modules and structs

## Priority 2: Phase 3 - Debugger Integration (Critical for Advanced Users)

### Move Debugger Support
- [ ] Create `MoveDebuggerSupport` class extending `XDebuggerSupport`
- [ ] Implement breakpoint handling for Move files
- [ ] Add stack frame representation for Move execution
- [ ] Create variable inspection support
- [ ] Implement step over/into/out functionality
- [ ] Add watch expressions evaluation
- [ ] Create debugger configuration type
- [ ] Write comprehensive debugger tests

### Debugger UI Components
- [ ] Create custom debugger tool window
- [ ] Add Move-specific variable renderers
- [ ] Implement execution line indicators
- [ ] Add breakpoint validation

## Priority 3: Phase 3 - Performance Analysis Tools

### Profiling Support
- [ ] Create `MoveProfiler` service for gas consumption analysis
- [ ] Implement execution time profiling
- [ ] Add memory usage tracking for Move VM
- [ ] Create performance inspection tools
- [ ] Add gas optimization suggestions

### Performance UI
- [ ] Create profiler tool window
- [ ] Add flame graph visualization
- [ ] Implement performance reports generation
- [ ] Add comparative analysis features

## Priority 4: Enhanced Core Features

### Type System Improvements
- [ ] Complete generic type inference
- [ ] Add type parameter constraints validation
- [ ] Implement phantom type checking
- [ ] Add ability constraints verification

### Semantic Analysis Enhancements
- [ ] Add borrow checker integration
- [ ] Implement move/copy semantics validation
- [ ] Add resource safety analysis
- [ ] Implement acquires clause validation

### Code Generation
- [ ] Add "Generate Getter/Setter" for struct fields
- [ ] Implement "Generate Constructor" for structs
- [ ] Add "Generate Test Function" action
- [ ] Create "Generate Module Template" action

## Priority 5: Integration Improvements

### Build System Integration
- [ ] Add support for Move.toml project files
- [ ] Implement dependency resolution from Move package manager
- [ ] Add build configuration UI
- [ ] Create run configurations for Move scripts

### Version Control Integration
- [ ] Add Move-specific diff viewer
- [ ] Implement smart merge for Move files
- [ ] Add commit message templates for Move changes

### External Tools Integration
- [ ] Integrate with Move Prover
- [ ] Add support for Move coverage tools
- [ ] Implement Move package manager integration
- [ ] Add blockchain explorer integration

## Priority 6: Quality of Life Improvements

### Enhanced Inspections
- [ ] Add "Unused Import" inspection
- [ ] Implement "Dead Code" detection
- [ ] Add "Resource Leak" inspection
- [ ] Create "Gas Optimization" suggestions
- [ ] Implement security vulnerability checks

### Code Templates
- [ ] Create file templates for common Move patterns
- [ ] Add postfix templates for Move idioms
- [ ] Implement surround templates
- [ ] Create custom live template variables

### Documentation
- [ ] Add quick documentation provider
- [ ] Implement external documentation integration
- [ ] Create in-IDE Move tutorial
- [ ] Add context-sensitive help

## Priority 7: Testing and Quality

### Test Coverage
- [ ] Achieve 90%+ test coverage for all features
- [ ] Add integration tests with real Move projects
- [ ] Create performance benchmarks
- [ ] Add stress tests for large files

### Documentation
- [ ] Complete user guide documentation
- [ ] Create developer documentation
- [ ] Add video tutorials
- [ ] Create sample projects

### Platform Support
- [ ] Test and fix issues on all IntelliJ-based IDEs
- [ ] Ensure compatibility with latest IntelliJ versions
- [ ] Add support for older IDE versions (if needed)

## Estimated Timeline

- **Phase 2 Completion**: 1-2 weeks
- **Debugger Integration**: 3-4 weeks
- **Performance Tools**: 2-3 weeks
- **Enhanced Features**: 2-3 weeks
- **Quality & Polish**: 1-2 weeks

**Total Estimated Time**: 9-14 weeks for full completion

## Notes

1. Debugger integration is the most complex remaining feature and should be prioritized if advanced users need it
2. Performance analysis tools can be developed incrementally
3. Many enhancements can be shipped as updates after initial release
4. Community feedback should guide priority adjustments
