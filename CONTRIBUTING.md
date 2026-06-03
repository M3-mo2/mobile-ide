# Contributing to Mobile IDE

Thank you for your interest in contributing to Mobile IDE! This document provides guidelines and instructions for contributing.

## Development Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Git

### Setup Steps

1. Fork and clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Run tests to verify setup

## Code Style

We follow the [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide).

### Key Points

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use trailing commas in multi-line declarations
- Prefer explicit types over inference for public APIs
- Document all public APIs with KDoc

### Example

```kotlin
/**
 * Represents a position in the text buffer.
 * Line and column are 0-based.
 */
data class Position(val line: Int, val column: Int) {
    init {
        require(line >= 0) { "Line must be non-negative" }
        require(column >= 0) { "Column must be non-negative" }
    }
}
```

## Branch Strategy

We use Git Flow:

- `main`: Production code
- `develop`: Integration branch
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `release/*`: Release preparation
- `hotfix/*`: Emergency fixes

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code refactoring
- `test`: Tests
- `chore`: Maintenance

Examples:
```
feat(editor-core): add multiple cursor support
fix(editor-ui): resolve cursor blinking issue
docs: update README with build instructions
```

## Pull Request Process

1. Update documentation for any API changes
2. Add tests for new functionality
3. Ensure all tests pass
4. Update CHANGELOG.md
5. Request review from maintainers
6. Address review feedback
7. Squash commits if requested

## Testing

### Unit Tests

- Target: 90% coverage for editor-core
- Test pure functions extensively
- Mock external dependencies
- Use descriptive test names

```kotlin
@Test
fun `insert text at beginning`() {
    val buffer = TextBufferFactory.fromText("World")
    val newBuffer = buffer.insert(0, "Hello, ")
    assertEquals("Hello, World", newBuffer.getText())
}
```

### Integration Tests

- Test module interactions
- Use in-memory databases
- Test file I/O with temporary files

### UI Tests

- Test critical user flows
- Use Espresso for Android UI
- Test on different screen sizes

## Performance

- Profile before optimizing
- Use Android Profiler for memory/CPU
- Benchmark critical paths
- Document performance characteristics

## Documentation

- Document all public APIs
- Include usage examples
- Document architecture decisions in ADRs
- Keep README up to date

## Questions?

- Open an issue for questions
- Join our community discussions
- Contact maintainers

Thank you for contributing!
