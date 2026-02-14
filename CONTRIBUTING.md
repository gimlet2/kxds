# Contributing to KXDS

Thank you for your interest in contributing to KXDS! We welcome contributions from the community and are grateful for your support.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Submitting Changes](#submitting-changes)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Enhancements](#suggesting-enhancements)
- [Questions](#questions)

## Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/kxds.git
   cd kxds
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/gimlet2/kxds.git
   ```
4. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## How to Contribute

There are many ways to contribute to KXDS:

- 🐛 **Report bugs** - Help us identify and fix issues
- 💡 **Suggest features** - Share your ideas for improvements
- 📝 **Improve documentation** - Fix typos, clarify instructions, add examples
- 🔧 **Submit code changes** - Fix bugs or implement new features
- ✅ **Write tests** - Improve test coverage
- 🎨 **Improve examples** - Create better demonstrations of KXDS capabilities

## Development Setup

### Prerequisites

- **JDK 21** or higher
- **Gradle 9.3.1** or higher (included via wrapper)
- **Git**
- An IDE with Kotlin support (IntelliJ IDEA recommended)

### Building the Project

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run tests with coverage (if configured)
./gradlew test jacocoTestReport

# Clean build
./gradlew clean build
```

### Project Structure

- `kxds-hosting/` - The KSP processor that generates Kotlin code from XSD
- `example/` - Example project demonstrating KXDS usage
- `src/` - Root module source

### Working with the Example

The `example` module is useful for testing changes:

```bash
# Build and run the example
cd example
../gradlew clean build
../gradlew run
```

## Coding Standards

### Kotlin Style Guide

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Add KDoc comments for public APIs

### Code Quality

- **No warnings**: Fix all compiler warnings
- **No unused imports**: Remove unused imports
- **Proper formatting**: Use IntelliJ IDEA's code formatter
- **Null safety**: Leverage Kotlin's null safety features

### Commit Messages

Write clear, concise commit messages:

```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or tooling changes

**Example:**
```
feat: Add support for xs:union types

- Parse union type definitions from XSD schemas
- Generate sealed classes for union types
- Add tests for union type generation

Closes #123
```

## Testing Guidelines

### Writing Tests

- Write tests for all new features and bug fixes
- Place tests in `kxds-hosting/src/test/kotlin/`
- Use descriptive test names: `test should do something when condition`
- Follow the Arrange-Act-Assert pattern

### Test Structure

```kotlin
@Test
fun `should generate data class from simple complex type`() {
    // Arrange
    val xsdContent = """
        <xs:complexType name="Person">
            <xs:sequence>
                <xs:element name="name" type="xs:string"/>
            </xs:sequence>
        </xs:complexType>
    """
    
    // Act
    val result = processXSD(xsdContent)
    
    // Assert
    assertThat(result).contains("data class Person")
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests XDSProcessorTest

# Run specific test method
./gradlew test --tests XDSProcessorTest."test name"
```

## Submitting Changes

### Pull Request Process

1. **Update your branch** with the latest upstream changes:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run all tests** to ensure nothing is broken:
   ```bash
   ./gradlew clean test
   ```

3. **Push your changes** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create a Pull Request** on GitHub:
   - Use a clear, descriptive title
   - Reference any related issues (e.g., "Fixes #123")
   - Describe what changed and why
   - Include screenshots for UI changes
   - List any breaking changes

5. **Respond to feedback**:
   - Address reviewer comments promptly
   - Push additional commits if needed
   - Keep the PR focused on a single concern

### PR Checklist

Before submitting, ensure:

- [ ] Code follows the project's coding standards
- [ ] All tests pass locally
- [ ] New code has appropriate test coverage
- [ ] Documentation is updated (if applicable)
- [ ] CHANGELOG.md is updated (for significant changes)
- [ ] No merge conflicts with main branch
- [ ] Commit messages are clear and descriptive

## Reporting Bugs

### Before Submitting a Bug Report

1. **Check existing issues** to see if it's already reported
2. **Update to the latest version** to see if it's already fixed
3. **Gather information**:
   - KXDS version
   - Kotlin version
   - JDK version
   - Operating system
   - Build tool (Gradle version)

### How to Submit a Bug Report

Use the bug report template and include:

- **Clear title**: Brief, descriptive summary
- **Description**: Detailed explanation of the issue
- **Steps to reproduce**: Numbered list of steps
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **XSD schema**: Minimal schema that reproduces the issue
- **Generated code**: The incorrect code generated (if applicable)
- **Error messages**: Full stack traces or error messages
- **Environment**: Version information

## Suggesting Enhancements

We welcome feature requests! When suggesting an enhancement:

1. **Check existing issues** to avoid duplicates
2. **Be specific**: Clearly describe the feature
3. **Explain the use case**: Why is this feature needed?
4. **Provide examples**: Show how it would work
5. **Consider alternatives**: Mention alternative solutions you've considered

## Questions

Have questions about contributing? You can:

- Open a [GitHub Discussion](https://github.com/gimlet2/kxds/discussions)
- Check the [README](README.md) and [QUICK_START](QUICK_START.md) guides
- Look through existing issues and PRs

## Recognition

Contributors will be recognized in:

- Release notes
- CHANGELOG.md
- GitHub contributor list

Thank you for contributing to KXDS! 🎉
