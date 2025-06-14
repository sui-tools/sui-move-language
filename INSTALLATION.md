# Sui Move Language Plugin - Installation Guide

## Quick Installation

### Option 1: Install from ZIP file (Recommended)

1. **Locate the plugin file**: 
   - Path: `build/distributions/sui-move-language-0.1.0.zip`
   - Size: ~143 KB

2. **Install in IntelliJ IDEA**:
   - Open IntelliJ IDEA
   - Go to **File → Settings** (on macOS: **IntelliJ IDEA → Preferences**)
   - Navigate to **Plugins**
   - Click the gear icon (⚙️) → **Install Plugin from Disk...**
   - Browse and select `sui-move-language-0.1.0.zip`
   - Click **OK**

3. **Restart IntelliJ IDEA**
   - You'll be prompted to restart the IDE
   - Click **Restart**

### Option 2: Build from Source

```bash
# Clone and build
git clone https://github.com/ravidsrk/sui-move-language.git
cd sui-move-language
./gradlew build -x test

# Install the generated ZIP
# Follow Option 1 steps with the ZIP from build/distributions/
```

## Verify Installation

1. **Check Plugin is Active**:
   - Go to **Settings → Plugins → Installed**
   - Look for "Sui Move Language"
   - Ensure it's enabled (checkbox is checked)

2. **Test with a Move File**:
   - Create a new file with `.move` extension
   - You should see:
     - Syntax highlighting
     - Move file icon
     - Code completion (Ctrl+Space)

## Features Available After Installation

- ✅ Syntax highlighting for Move files
- ✅ Code completion and auto-import
- ✅ Go to declaration (Ctrl+Click)
- ✅ Find usages (Alt+F7)
- ✅ Structure view (Ctrl+F12)
- ✅ Code formatting (Ctrl+Alt+L)
- ✅ Brace matching
- ✅ Comment/uncomment (Ctrl+/)
- ✅ Code folding
- ✅ Move-specific inspections
- ✅ Quick fixes and intentions
- ✅ Sui CLI integration

## Configuration

After installation, configure the Sui CLI path:
1. Go to **Settings → Tools → Sui Move**
2. Set the path to your Sui CLI executable
3. Configure other preferences as needed

## Troubleshooting

### Plugin doesn't appear after installation
- Ensure you restarted IntelliJ IDEA
- Check **Settings → Plugins → Installed** and enable if disabled
- Verify the ZIP file isn't corrupted

### Move files not recognized
- Ensure files have `.move` extension
- Check **Settings → Editor → File Types** for Move file type

### Build errors when compiling from source
- Ensure Java 17 is installed (required by IntelliJ Platform)
- Run `./gradlew clean build -x test`

## Support

For issues or feature requests, please visit:
- GitHub Issues: [your-repo-url]/issues
- Plugin Homepage: [your-plugin-page]
