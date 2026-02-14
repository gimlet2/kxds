---
name: Bug Report
about: Report a bug or unexpected behavior in KXDS
title: '[BUG] '
labels: bug
assignees: ''
---

## Bug Description

A clear and concise description of what the bug is.

## Environment

- **KXDS Version**: [e.g., 0.1.0]
- **Kotlin Version**: [e.g., 2.2.21]
- **KSP Version**: [e.g., 2.2.21-2.0.4]
- **JDK Version**: [e.g., JDK 21]
- **Gradle Version**: [e.g., 8.11.1]
- **Operating System**: [e.g., macOS 14.0, Ubuntu 22.04, Windows 11]

## XSD Schema

Please provide the minimal XSD schema that reproduces the issue:

```xml
<!-- Paste your XSD schema here -->
```

## Expected Behavior

A clear and concise description of what you expected to happen.

## Actual Behavior

A clear and concise description of what actually happened.

## Generated Code

If applicable, paste the incorrect code that was generated:

```kotlin
// Paste generated code here
```

## Steps to Reproduce

1. Create an XSD file with [describe structure]
2. Add annotation `@XDSProcessorAnnotation(schema = "...")`
3. Run `./gradlew build`
4. Observe [describe issue]

## Error Messages

If applicable, paste any error messages or stack traces:

```
Paste error messages here
```

## Additional Context

Add any other context about the problem here, such as:
- Does this work with a simpler schema?
- Did this work in a previous version?
- Any workarounds you've found?

## Possible Solution

If you have ideas about what might be causing the issue or how to fix it, please share them here.
