# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- CHANGELOG.md to track version history and changes
- SECURITY.md for vulnerability reporting guidelines
- CONTRIBUTING.md with contribution guidelines
- CODE_OF_CONDUCT.md for community standards
- Issue and pull request templates for better collaboration

### Changed
- Updated documentation to reflect Kotlin 2.2.21 and KSP 2.2.21-2.0.4

## [0.1.0] - 2025-01-XX

### Added
- Initial release of KXDS
- KSP-based code generation from XSD schemas
- Support for complex types with sequences and choices
- Support for attributes (required and optional)
- Support for element cardinality (minOccurs, maxOccurs)
- Support for nillable elements
- Support for simple types with enumerations
- Support for value classes with validation annotations
- Support for 25+ XSD built-in types including:
  - String types (string, token, normalizedString, etc.)
  - Numeric types (int, long, short, byte, decimal, unsigned variants)
  - Boolean
  - Date/time types (dateTime, date, time, duration)
  - Binary types (hexBinary, base64Binary)
  - URI types
- Comprehensive test suite with 40+ unit tests
- Example module demonstrating usage
- CI/CD pipelines for building, testing, and publishing
- Documentation including README, QUICK_START, and PUBLISHING guides

### Changed
- N/A (initial release)

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

[Unreleased]: https://github.com/gimlet2/kxds/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/gimlet2/kxds/releases/tag/v0.1.0
