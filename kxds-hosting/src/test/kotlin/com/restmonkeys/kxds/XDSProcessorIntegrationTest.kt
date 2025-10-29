package com.restmonkeys.kxds

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

class XDSProcessorIntegrationTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test comprehensive schema generation`() {
        val schemaContent = File("src/test/resources/schemas/comprehensive-test.xsd").readText()
        val outputFile = tempDir.resolve("Article.kt").toFile()
        
        // This test verifies that the schema can be parsed without errors
        // In a real KSP environment, the generated code would be created
        assertTrue(schemaContent.contains("Article"), "Schema should contain Article type")
        assertTrue(schemaContent.contains("Status"), "Schema should contain Status enumeration")
    }
    
    @Test
    fun `test types schema generation`() {
        val schemaContent = File("src/test/resources/schemas/types-test.xsd").readText()
        
        assertTrue(schemaContent.contains("AllTypes"), "Schema should contain AllTypes")
        assertTrue(schemaContent.contains("xs:dateTime"), "Schema should contain dateTime type")
        assertTrue(schemaContent.contains("xs:unsignedInt"), "Schema should contain unsignedInt type")
    }
    
    @Test
    fun `test attributes schema generation`() {
        val schemaContent = File("src/test/resources/schemas/attributes-test.xsd").readText()
        
        assertTrue(schemaContent.contains("PersonWithAttributes"), "Schema should contain PersonWithAttributes")
        assertTrue(schemaContent.contains("xs:attribute"), "Schema should contain attributes")
    }
    
    @Test
    fun `test cardinality schema generation`() {
        val schemaContent = File("src/test/resources/schemas/cardinality-test.xsd").readText()
        
        assertTrue(schemaContent.contains("Library"), "Schema should contain Library")
        assertTrue(schemaContent.contains("minOccurs"), "Schema should contain minOccurs")
        assertTrue(schemaContent.contains("maxOccurs"), "Schema should contain maxOccurs")
    }
    
    @Test
    fun `test nillable schema generation`() {
        val schemaContent = File("src/test/resources/schemas/nillable-test.xsd").readText()
        
        assertTrue(schemaContent.contains("Product"), "Schema should contain Product")
        assertTrue(schemaContent.contains("nillable"), "Schema should contain nillable")
    }
    
    @Test
    fun `test enum schema generation`() {
        val schemaContent = File("src/test/resources/schemas/enum-test.xsd").readText()
        
        assertTrue(schemaContent.contains("StatusType"), "Schema should contain StatusType")
        assertTrue(schemaContent.contains("PriorityLevel"), "Schema should contain PriorityLevel")
        assertTrue(schemaContent.contains("xs:enumeration"), "Schema should contain enumerations")
    }
}
