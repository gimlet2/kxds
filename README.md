# KXDS - Kotlin XSD Data Class Generator

A Kotlin Symbol Processing (KSP) plugin that automatically generates Kotlin data classes from XML Schema Definition (XSD) files.

## Overview

KXDS simplifies working with XML schemas in Kotlin by automatically generating type-safe data classes from XSD files at compile time. This eliminates the need for manual data class creation and ensures your Kotlin code stays in sync with your XML schemas.

## Features

- **Automatic Code Generation**: Generates Kotlin data classes from XSD files using KSP
- **Type Safety**: Leverages Kotlin's type system for compile-time validation
- **XSD Support**: Supports complex types, sequences, and choices
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

## Example

See the `example` module for a complete working example of KXDS in action.

## Building the Project

To build the project:

```bash
./gradlew build
```

## Development

The project uses Gradle with Kotlin DSL for build configuration. Key technologies:

- **KSP**: For compile-time code generation
- **JAXB**: For parsing XSD schemas
- **KotlinPoet**: For generating Kotlin code

## License

This project is part of the RestMonkeys organization.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.
