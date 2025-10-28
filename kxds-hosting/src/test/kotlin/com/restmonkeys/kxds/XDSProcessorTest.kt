package com.restmonkeys.kxds

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.xml.namespace.QName
import kotlin.test.assertEquals

class XDSProcessorTest {

    @Test
    fun `test string type mapping`() {
        val processor = createProcessor()
        assertEquals(String::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "string")))
        assertEquals(String::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "normalizedString")))
        assertEquals(String::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "token")))
    }

    @Test
    fun `test signed integer type mapping`() {
        val processor = createProcessor()
        assertEquals(Int::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "int")))
        assertEquals(Int::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "integer")))
        assertEquals(Long::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "long")))
        assertEquals(Short::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "short")))
        assertEquals(Byte::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "byte")))
    }

    @Test
    fun `test unsigned integer type mapping`() {
        val processor = createProcessor()
        assertEquals(BigInteger::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "unsignedLong")))
        assertEquals(Long::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "unsignedInt")))
        assertEquals(Int::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "unsignedShort")))
        assertEquals(Short::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "unsignedByte")))
    }

    @Test
    fun `test decimal type mapping`() {
        val processor = createProcessor()
        assertEquals(java.math.BigDecimal::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "decimal")))
    }

    @Test
    fun `test boolean type mapping`() {
        val processor = createProcessor()
        assertEquals(Boolean::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "boolean")))
    }

    @Test
    fun `test floating point type mapping`() {
        val processor = createProcessor()
        assertEquals(Float::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "float")))
        assertEquals(Double::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "double")))
    }

    @Test
    fun `test date and time type mapping`() {
        val processor = createProcessor()
        assertEquals(LocalDateTime::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "dateTime")))
        assertEquals(LocalDate::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "date")))
        assertEquals(LocalTime::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "time")))
        assertEquals(Duration::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "duration")))
    }

    @Test
    fun `test binary type mapping`() {
        val processor = createProcessor()
        assertEquals(ByteArray::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "hexBinary")))
        assertEquals(ByteArray::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "base64Binary")))
    }

    @Test
    fun `test URI type mapping`() {
        val processor = createProcessor()
        assertEquals(URI::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "anyURI")))
    }

    @Test
    fun `test constrained integer type mapping`() {
        val processor = createProcessor()
        assertEquals(BigInteger::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "positiveInteger")))
        assertEquals(BigInteger::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "negativeInteger")))
        assertEquals(BigInteger::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "nonPositiveInteger")))
        assertEquals(BigInteger::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "nonNegativeInteger")))
    }

    @Test
    fun `test unsupported type defaults to String`() {
        val processor = createProcessor()
        assertEquals(String::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "unknownType")))
        assertEquals(String::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "QName")))
        assertEquals(String::class, processor.getType(QName("http://www.w3.org/2001/XMLSchema", "NOTATION")))
    }

    @Test
    fun `test enum value conversion`() {
        val processor = createProcessor()
        val enumSpec = processor.toEnum("StatusType", listOf("active", "inactive", "pending"))
        
        assertEquals("StatusType", enumSpec.name)
        assertEquals(3, enumSpec.enumConstants.size)
        
        val constants = enumSpec.enumConstants.map { it.key }
        assertEquals(listOf("ACTIVE", "INACTIVE", "PENDING"), constants)
    }

    @Test
    fun `test enum with special characters`() {
        val processor = createProcessor()
        val enumSpec = processor.toEnum("MyEnum", listOf("value-1", "value.2", "value 3", "123value"))
        
        val constants = enumSpec.enumConstants.map { it.key }
        assertEquals(listOf("VALUE_1", "VALUE_2", "VALUE_3", "_123VALUE"), constants)
    }

    private fun createProcessor(): XDSProcessor {
        // Create a mock logger that doesn't do anything
        val logger = object : KSPLogger {
            override fun error(message: String, symbol: KSNode?) {}
            override fun warn(message: String, symbol: KSNode?) {}
            override fun info(message: String, symbol: KSNode?) {}
            override fun logging(message: String, symbol: KSNode?) {}
            override fun exception(e: Throwable) {}
        }
        
        // Create a mock code generator
        val codeGenerator = object : CodeGenerator {
            override val generatedFile: MutableList<java.io.File> = mutableListOf()
            
            override fun associate(
                sources: List<KSFile>,
                packageName: String,
                fileName: String,
                extensionName: String
            ) {}
            
            override fun associateByPath(
                sources: List<KSFile>,
                path: String,
                extensionName: String
            ) {}
            
            override fun associateWithClasses(
                classes: List<com.google.devtools.ksp.symbol.KSClassDeclaration>,
                packageName: String,
                fileName: String,
                extensionName: String
            ) {}
            
            override fun createNewFile(
                dependencies: Dependencies,
                packageName: String,
                fileName: String,
                extensionName: String
            ): java.io.OutputStream {
                return java.io.ByteArrayOutputStream()
            }
            
            override fun createNewFileByPath(
                dependencies: Dependencies,
                path: String,
                extensionName: String
            ): java.io.OutputStream {
                return java.io.ByteArrayOutputStream()
            }
        }
        
        return XDSProcessor(codeGenerator, logger, emptyMap())
    }
}