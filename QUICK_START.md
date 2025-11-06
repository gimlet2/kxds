# Quick Start Guide for KXDS

This guide will help you get started with KXDS in your Kotlin project.

## Installation

### Step 1: Add the KSP plugin to your project

In your `build.gradle.kts`, add the KSP plugin:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    kotlin("jvm") version "2.1.0"
}
```

### Step 2: Add KXDS dependencies

Add the KXDS library to your dependencies:

```kotlin
dependencies {
    implementation("org.restmonkeys:kxds-hosting:0.1.0")
    ksp("org.restmonkeys:kxds-hosting:0.1.0")
}
```

**Note**: If you want to use a development version from GitHub Packages, you'll need to add authentication. See [PUBLISHING.md](PUBLISHING.md#using-the-published-plugin) for details.

### Step 3: Configure the XSD path

Tell KXDS where to find your XSD files:

```kotlin
ksp {
    arg("path", "${layout.projectDirectory}/src/main/resources/xsd/")
}
```

## Usage

### Create an XSD Schema

Create an XSD file in your resources directory (e.g., `src/main/resources/xsd/note.xsd`):

```xml
<?xml version="1.0"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="https://example.com"
           xmlns="https://example.com"
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

### Annotate Your Code

In your Kotlin code, add the `@XDSProcessorAnnotation` to trigger code generation:

```kotlin
import com.restmonkeys.kxds.XDSProcessorAnnotation

@XDSProcessorAnnotation(schema = "note.xsd")
fun main() {
    val note = Note(
        to = "John",
        from = "Jane",
        heading = "Meeting",
        body = "Don't forget about our meeting tomorrow!"
    )
    println(note)
}
```

### Build Your Project

Run a Gradle build to generate the data classes:

```bash
./gradlew build
```

KXDS will automatically generate a `Note` data class based on your XSD schema!

## What Gets Generated?

For the above schema, KXDS generates:

```kotlin
data class Note(
    val to: String,
    val from: String,
    val heading: String,
    val body: String
)
```

## Next Steps

- Check out the [README](README.md) for more features like:
  - Handling optional fields and lists
  - Working with enumerations
  - Using attributes
  - Working with nillable elements
- See the [example](example/) module for more complex usage scenarios
- Read [PUBLISHING.md](PUBLISHING.md) if you want to contribute or publish your own version

## Troubleshooting

### Generated classes not found

Make sure to:
1. Build your project after adding the annotation
2. Check that the XSD file path is correct in the `ksp` configuration
3. Ensure the XSD file is in the resources directory

### KSP not running

Verify that:
1. The KSP plugin is properly applied
2. The `ksp` dependency is added (not just `implementation`)
3. You have annotated at least one file with `@XDSProcessorAnnotation`

## Requirements

- JDK 17 or higher
- Gradle 8.11.1 or higher
- Kotlin 2.1.0
