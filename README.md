# KXDS - Kotlin XSD Data Class Generator

A Kotlin Symbol Processing (KSP) plugin that automatically generates Kotlin data classes from XML Schema Definition (XSD) files.

## Overview

KXDS simplifies working with XML schemas in Kotlin by automatically generating type-safe data classes from XSD files at compile time. This eliminates the need for manual data class creation and ensures your Kotlin code stays in sync with your XML schemas.

## Features

- **Automatic Code Generation**: Generates Kotlin data classes from XSD files using KSP
- **Type Safety**: Leverages Kotlin's type system for compile-time validation
- **Comprehensive XSD Support**: 
  - Complex types with sequences and choices
  - Attributes (required and optional)
  - Element cardinality (minOccurs, maxOccurs) for optional and list properties
  - Nillable elements (nullable properties)
  - Simple types with enumerations
  - Wide range of XSD built-in types including:
    - String types (string, token, normalizedString, etc.)
    - Numeric types (int, long, short, byte, decimal, unsigned variants)
    - Boolean
    - Date/time types (dateTime, date, time, duration)
    - Binary types (hexBinary, base64Binary)
    - URI types
- **Simple Integration**: Easy to integrate into existing Kotlin projects

## Project Structure

The project consists of three main modules:

- **kxds-hosting**: The KSP processor that performs the code generation
- **example**: A sample project demonstrating how to use KXDS
- **src**: The root module

## Requirements

- JDK 21 or higher
- Gradle 8.11.1 or higher
- Kotlin 2.1.0

## Usage

### Adding KXDS to Your Project

1. Add the KSP plugin and KXDS dependencies to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    kotlin("jvm") version "2.1.0"
}

dependencies {
    implementation(project(":kxds-hosting"))
    ksp(project(":kxds-hosting"))
}
```

2. Configure the path to your XSD files:

```kotlin
ksp {
    arg("path", "${layout.projectDirectory}/src/main/resources/xds/")
}
```

### Generating Data Classes

1. Create an XSD schema file (e.g., `schema.xsd`):

```xml
<?xml version="1.0"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="https://www.w3schools.com"
           xmlns="https://www.w3schools.com"
           elementFormDefault="qualified">

    <xs:element name="Note" type="Note"/>
    <xs:complexType name="Note">
        <xs:sequence>
            <xs:element name="to" type="xs:string"/>
            <xs:element name="from" type="xs:string"/>
            <xs:element name="heading" type="xs:string"/>
            <xs:element name="body" type="xs:string"/>
        </xs:sequence>
    </xs:complexType>

</xs:schema>
```

2. Annotate a function or class with `@XDSProcessorAnnotation`:

```kotlin
import com.restmonkeys.kxds.XDSProcessorAnnotation

@XDSProcessorAnnotation(schema = "schema.xsd")
fun main() {
    val note = Note(
        to = "Tove",
        from = "Jani",
        heading = "Reminder",
        body = "Don't forget me this weekend!",
    )
    println(note)
}
```

3. Build your project - KXDS will automatically generate the `Note` data class from your XSD schema.

## Supported XSD Features

### Data Types

KXDS supports all common XSD built-in types:

- **String Types**: string, normalizedString, token, language, Name, NCName, ID, IDREF, ENTITY, NMTOKEN
- **Numeric Types (Signed)**: 
  - int, integer, long, short, byte, decimal
  - positiveInteger, negativeInteger, nonPositiveInteger, nonNegativeInteger
- **Numeric Types (Unsigned)**: 
  - unsignedLong, unsignedInt, unsignedShort, unsignedByte
- **Boolean**: boolean
- **Floating Point**: float, double
- **Date/Time**: dateTime, date, time, duration
- **Binary**: hexBinary, base64Binary
- **URI**: anyURI

### Structural Features

#### Attributes

Both required and optional attributes are supported:

```xml
<xs:complexType name="Person">
    <xs:sequence>
        <xs:element name="name" type="xs:string"/>
    </xs:sequence>
    <xs:attribute name="id" type="xs:string" use="required"/>
    <xs:attribute name="country" type="xs:string" use="optional"/>
</xs:complexType>
```

Generates:
```kotlin
data class Person(
    val name: String,
    val id: String,
    val country: String? = null
)
```

#### Cardinality (minOccurs, maxOccurs)

Elements with varying cardinality are properly handled. Required elements remain non-nullable, elements with `minOccurs="0"` become nullable with a default value, and elements with `maxOccurs > 1` or `unbounded` generate List properties:

```xml
<xs:element name="title" type="xs:string"/>
<xs:element name="summary" type="xs:string" minOccurs="0"/>
<xs:element name="tags" type="xs:string" maxOccurs="unbounded"/>
```

Generates:
```kotlin
val title: String
val summary: String? = null
val tags: List<String>
```

#### Nillable Elements

Nillable elements become nullable Kotlin properties:

```xml
<xs:element name="price" type="xs:decimal" nillable="true"/>
```

Generates:
```kotlin
val price: BigDecimal? = null
```

#### Enumerations

Simple types with enumerations are converted to Kotlin enums:

```xml
<xs:simpleType name="Status">
    <xs:restriction base="xs:string">
        <xs:enumeration value="active"/>
        <xs:enumeration value="inactive"/>
        <xs:enumeration value="pending"/>
    </xs:restriction>
</xs:simpleType>
```

Generates:
```kotlin
enum class Status {
    ACTIVE,
    INACTIVE,
    PENDING
}
```

## Example

See the `example` module for a complete working example of KXDS in action.

## Building the Project

To build the project:

```bash
./gradlew build
```

To run tests:

```bash
./gradlew test
```

## Development

The project uses Gradle with Kotlin DSL for build configuration. Key technologies:

- **KSP**: For compile-time code generation
- **JAXB**: For parsing XSD schemas
- **KotlinPoet**: For generating Kotlin code

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.
