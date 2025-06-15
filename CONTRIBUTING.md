# Contributing to Sui Move Language Plugin

Thank you for your interest in contributing to the Sui Move Language plugin! We welcome contributions from the community.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/sui-move-language.git
   cd sui-move-language
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/ravidsrk/sui-move-language.git
   ```

## Development Setup

### Prerequisites
- JDK 17 or higher
- IntelliJ IDEA 2023.2 or higher (Community or Ultimate)
- Gradle 8.2 or higher

### Building the Project
```bash
# Build the plugin
./gradlew buildPlugin

# Run tests
./gradlew test

# Run the plugin in a sandbox IDE
./gradlew runIde
```

## Making Changes

### Code Style
- Follow Kotlin coding conventions
- Use ktlint for code formatting:
  ```bash
  ./gradlew ktlintFormat
  ```
- Run detekt for static analysis:
  ```bash
  ./gradlew detekt
  ```

### Testing
- Write tests for all new features
- Ensure all tests pass before submitting:
  ```bash
  ./gradlew test
  ```
- Add integration tests for complex features

### Commit Messages
- Use clear and descriptive commit messages
- Follow conventional commits format:
  - `feat:` for new features
  - `fix:` for bug fixes
  - `docs:` for documentation changes
  - `test:` for test additions/changes
  - `refactor:` for code refactoring
  - `chore:` for maintenance tasks

Example:
```
feat: add support for Move 2024 syntax

- Add new keywords for Move 2024
- Update parser to handle new syntax
- Add tests for new features
```

## Submitting Changes

1. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and commit:
   ```bash
   git add .
   git commit -m "feat: add amazing feature"
   ```

3. Push to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

4. Create a Pull Request:
   - Go to the original repository on GitHub
   - Click "New Pull Request"
   - Select your fork and branch
   - Fill in the PR template
   - Submit the PR

## Pull Request Guidelines

### PR Title
- Use a clear and descriptive title
- Follow the same format as commit messages

### PR Description
- Describe what changes you made
- Explain why these changes are needed
- Reference any related issues
- Include screenshots for UI changes

### PR Checklist
- [ ] Tests pass locally
- [ ] Code follows style guidelines
- [ ] Documentation is updated
- [ ] Changelog is updated (for significant changes)
- [ ] PR has a clear description

## Reporting Issues

### Bug Reports
When reporting bugs, please include:
- Plugin version
- IDE version and type
- Operating system
- Steps to reproduce
- Expected behavior
- Actual behavior
- Error messages/logs

### Feature Requests
For feature requests, please:
- Check if the feature already exists
- Search for similar requests
- Provide a clear use case
- Explain the expected behavior

## Code of Conduct

### Our Standards
- Be respectful and inclusive
- Welcome newcomers and help them get started
- Accept constructive criticism
- Focus on what is best for the community
- Show empathy towards others

### Unacceptable Behavior
- Harassment or discrimination
- Trolling or insulting comments
- Public or private harassment
- Publishing others' private information
- Other unprofessional conduct

## Getting Help

- **Discord**: Join our Discord server (link in README)
- **GitHub Discussions**: Ask questions and discuss ideas
- **Issue Tracker**: Report bugs and request features

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

Thank you for contributing to the Sui Move Language plugin!
