# Sui Move Language Plugin v2.0 - User Guide

## Table of Contents
1. [Installation](#installation)
2. [Getting Started](#getting-started)
3. [Code Completion](#code-completion)
4. [Type System Features](#type-system-features)
5. [Testing Support](#testing-support)
6. [Debugging](#debugging)
7. [Refactoring](#refactoring)
8. [Sui-Specific Features](#sui-specific-features)
9. [Keyboard Shortcuts](#keyboard-shortcuts)
10. [Troubleshooting](#troubleshooting)

## Installation

### Requirements
- IntelliJ IDEA 2023.1 or later
- Sui CLI installed and in PATH
- Java 17 or later

### Installing the Plugin
1. Open IntelliJ IDEA
2. Go to **Settings/Preferences** → **Plugins**
3. Click **Marketplace** and search for "Sui Move Language"
4. Click **Install** and restart IntelliJ IDEA

### First-Time Setup
1. Go to **Settings** → **Languages & Frameworks** → **Sui Move**
2. Set the Sui CLI path (auto-detected if in PATH)
3. Configure default test timeout (optional)

## Getting Started

### Creating a New Move Project
1. **File** → **New** → **Project**
2. Select **Sui Move** from the left panel
3. Choose project template:
   - **Package** - Standard Move package
   - **Sui Project** - Sui-specific project with Move.toml
4. Enter project name and location
5. Click **Create**

### Opening an Existing Project
1. **File** → **Open**
2. Navigate to your Move project directory
3. Select the folder containing `Move.toml`
4. Click **OK**

## Code Completion

### Smart Type-Aware Completion
The plugin provides intelligent code completion based on type inference:

```move
module 0x1::example {
    struct MyStruct {
        value: u64,
        flag: bool
    }
    
    fun test() {
        let s = MyStruct { value: 10, flag: true };
        s. // <- Press Ctrl+Space to see field completions
    }
}
```

### Built-in Types and Functions
All Move built-in types and functions are available:
- Types: `u8`, `u16`, `u32`, `u64`, `u128`, `u256`, `bool`, `address`, `signer`, `vector<T>`
- Functions: `move_from`, `move_to`, `borrow_global`, `exists`, `assert!`, `abort`

### Module Import Completion
When typing `use` statements, the plugin suggests available modules:
```move
use 0x1:: // <- Shows all modules at address 0x1
use std:: // <- Shows standard library modules
```

### Postfix Templates
Type `.` after an expression for postfix completions:
- `.let` - Wrap in let binding
- `.if` - Wrap in if statement
- `.assert` - Wrap in assert!
- `.return` - Wrap in return statement

## Type System Features

### Type Inference
The plugin automatically infers types throughout your code:
```move
let x = 42; // Inferred as u64
let v = vector[1, 2, 3]; // Inferred as vector<u64>
```

### Type Hints
Hover over any variable to see its inferred type. Type hints are also shown inline for let bindings without explicit annotations.

### Type Error Highlighting
Type mismatches are highlighted in red with detailed error messages:
```move
let x: u64 = true; // Error: Type mismatch
```

### Generic Type Support
Full support for generic types with constraint checking:
```move
fun swap<T: copy + drop>(x: &mut T, y: &mut T) {
    // Generic parameters tracked and validated
}
```

## Testing Support

### Running Tests
1. **Gutter Icons**: Click the green arrow next to test functions
2. **Context Menu**: Right-click in editor → **Run 'test_name'**
3. **Tool Window**: Use the Sui Move tool window to run all tests

### Test Annotations
```move
#[test]
fun test_example() {
    assert!(1 + 1 == 2, 0);
}

#[test_only]
fun helper_function() {
    // Only available in test mode
}

#[expected_failure(abort_code = 0)]
fun test_failure() {
    abort 0
}
```

### Test Results
- View test results in the **Run** tool window
- Failed tests show detailed error messages
- Re-run failed tests with one click
- Test coverage visualization (if enabled)

## Debugging

### Setting Breakpoints
1. Click in the gutter next to any line of code
2. Red circle indicates an active breakpoint
3. Right-click breakpoint for conditions

### Starting Debug Session
1. Set breakpoints in your code
2. Right-click test function → **Debug 'test_name'**
3. Debugger stops at breakpoints

### Debug Features
- **Step Over** (F8) - Execute current line
- **Step Into** (F7) - Enter function calls
- **Step Out** (Shift+F8) - Exit current function
- **Resume** (F9) - Continue to next breakpoint

### Variable Inspection
- View local variables in **Variables** pane
- Hover over variables in code to see values
- Evaluate expressions in **Evaluate Expression** dialog (Alt+F8)

## Refactoring

### Rename (Shift+F6)
Safely rename any identifier across your entire project:
1. Place cursor on identifier
2. Press **Shift+F6**
3. Type new name
4. Press **Enter** to apply

### Extract Function (Ctrl+Alt+M)
Extract selected code into a new function:
1. Select code to extract
2. Press **Ctrl+Alt+M** (Cmd+Alt+M on Mac)
3. Enter function name and adjust parameters
4. Click **OK**

### Inline Function/Variable (Ctrl+Alt+N)
Inline a function or variable:
1. Place cursor on function/variable name
2. Press **Ctrl+Alt+N** (Cmd+Alt+N on Mac)
3. Choose inline options
4. Click **OK**

### Safe Delete
Safely delete unused code:
1. Place cursor on item to delete
2. Press **Alt+Delete**
3. Review usages (if any)
4. Confirm deletion

## Sui-Specific Features

### Object Validation
The plugin validates Sui object requirements:
- First field must be `id: UID`
- Objects must have `key` ability
- Proper `store` ability for nested objects

### Entry Function Validation
Entry functions are validated for:
- Correct parameter types
- Proper `TxContext` usage
- Valid return types

### Quick Fixes
- **Add UID field** - Adds missing `id: UID` field
- **Add key ability** - Adds missing `key` ability
- **Add TxContext** - Adds required `&mut TxContext` parameter
- **Convert to entry** - Adds `entry` modifier

### Sui CLI Integration
Access Sui CLI commands from the tool window:
- **Build** - `sui move build`
- **Test** - `sui move test`
- **Publish** - `sui client publish`

## Keyboard Shortcuts

### Essential Shortcuts
| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Code Completion | Ctrl+Space | Cmd+Space |
| Quick Fix | Alt+Enter | Alt+Enter |
| Go to Declaration | Ctrl+B | Cmd+B |
| Find Usages | Alt+F7 | Alt+F7 |
| Rename | Shift+F6 | Shift+F6 |
| Extract Function | Ctrl+Alt+M | Cmd+Alt+M |
| Format Code | Ctrl+Alt+L | Cmd+Alt+L |
| Comment Line | Ctrl+/ | Cmd+/ |
| Run Test | Ctrl+Shift+F10 | Cmd+Shift+F10 |
| Debug Test | Ctrl+Shift+F9 | Cmd+Shift+F9 |

### Navigation
| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Navigate Back | Ctrl+Alt+Left | Cmd+Alt+Left |
| Navigate Forward | Ctrl+Alt+Right | Cmd+Alt+Right |
| Go to Test | Ctrl+Shift+T | Cmd+Shift+T |
| File Structure | Ctrl+F12 | Cmd+F12 |
| Recent Files | Ctrl+E | Cmd+E |

## Troubleshooting

### Common Issues

#### Plugin Not Loading
1. Ensure IntelliJ IDEA version is 2023.1+
2. Check **Help** → **Show Log** for errors
3. Try reinstalling the plugin

#### Sui CLI Not Found
1. Install Sui CLI: `cargo install --locked --git https://github.com/MystenLabs/sui.git --branch main sui`
2. Add to PATH or set in plugin settings
3. Restart IntelliJ IDEA

#### Type Inference Not Working
1. Ensure project has valid `Move.toml`
2. **File** → **Invalidate Caches** → **Invalidate and Restart**
3. Check for syntax errors in code

#### Tests Not Running
1. Verify Sui CLI is properly configured
2. Check test function has `#[test]` annotation
3. Ensure no compilation errors

### Getting Help
- **GitHub Issues**: Report bugs and request features
- **Discord**: Join the Sui community for support
- **Documentation**: Check the latest docs at [sui.io](https://sui.io)

### Performance Tips
1. **Exclude build directories** from indexing
2. **Increase memory** if working with large projects:
   - Help → Edit Custom VM Options
   - Set `-Xmx2048m` or higher
3. **Disable unused inspections** in Settings → Editor → Inspections

## Advanced Features

### Custom Live Templates
Create custom code snippets:
1. **Settings** → **Editor** → **Live Templates**
2. Add new template group "Move"
3. Define templates with variables

### Code Style Configuration
Customize formatting:
1. **Settings** → **Editor** → **Code Style** → **Move**
2. Adjust indentation, spacing, and wrapping
3. Import/export code style schemes

### File Templates
Create custom file templates:
1. **Settings** → **Editor** → **File and Code Templates**
2. Add new Move file templates
3. Use variables for dynamic content

---

For more information and updates, visit the [Sui Move Language Plugin GitHub repository](https://github.com/sui-move-language/intellij-plugin).
