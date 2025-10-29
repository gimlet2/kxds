package com.restmonkeys.kxds

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import org.w3._2001.xmlschema.Attribute
import org.w3._2001.xmlschema.ComplexType
import org.w3._2001.xmlschema.Facet
import org.w3._2001.xmlschema.LocalElement
import org.w3._2001.xmlschema.NoFixedFacet
import org.w3._2001.xmlschema.Schema
import org.w3._2001.xmlschema.TopLevelComplexType
import org.w3._2001.xmlschema.TopLevelElement
import org.w3._2001.xmlschema.TopLevelSimpleType
import java.io.File
import java.math.BigInteger
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.xml.namespace.QName
import kotlin.reflect.KClass

@OptIn(KspExperimental::class)
class XDSProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val rootPath = options["path"] ?: error("path option is required")
        resolver.getSymbolsWithAnnotation("com.restmonkeys.kxds.XDSProcessorAnnotation")
            .forEach {
                val schemaPath = it.getAnnotationsByType(XDSProcessorAnnotation::class).first().schema
                logger.info("Processing schema: $schemaPath")
                val schemaFile = File(rootPath, schemaPath)
                if (schemaFile.exists()) {
                    val schemaContent = schemaFile.readText()
                    logger.info("Schema loaded successfully, size: ${schemaContent.length} bytes")
                    generate(schemaContent)
                } else {
                    logger.error("Schema file not found: ${schemaFile.absolutePath}")
                }
            }

        return emptyList()
    }

    fun generate(schema: String) {
        val context = JAXBContext.newInstance(Schema::class.java.packageName, Schema::class.java.classLoader)
        val result = context.createUnmarshaller()
            .unmarshal(schema.trim().byteInputStream())
        val schemaObj = result as? Schema ?: run {
            logger.error("Invalid schema format")
            return
        }
        schemaObj.simpleTypeOrComplexTypeOrGroup.forEach {
            when (it) {
                is TopLevelComplexType -> {
                    logger.info("Generating class for complex type: ${it.name}")
                    toDataClass(it).toCode()
                }

                is TopLevelElement -> {
                    logger.info("Generating class for element: ${it.name}")
                    it.complexType?.let { complexType ->
                        toDataClass(complexType, it.name).toCode()
                    }
                }
                
                is TopLevelSimpleType -> {
                    logger.info("Processing simple type: ${it.name}")
                    it.restriction?.let { restriction ->
                        val enumerations = restriction.facets
                            ?.filterIsInstance<JAXBElement<*>>()
                            ?.filter { elem -> elem.name.localPart == "enumeration" }
                            ?.mapNotNull { elem -> 
                                when (val value = elem.value) {
                                    is NoFixedFacet -> value.value
                                    is Facet -> value.value
                                    else -> null
                                }
                            }
                            ?: emptyList()
                        
                        if (enumerations.isNotEmpty()) {
                            logger.info("Generating enum for simple type: ${it.name} with ${enumerations.size} values")
                            toEnum(it.name, enumerations).toCode()
                        }
                    }
                }
            }
        }
    }

    fun TypeSpec.toCode() {
        codeGenerator.createNewFile(Dependencies.ALL_FILES, "", name!!).bufferedWriter().use {
            FileSpec.builder("", name!!).addType(this)
                .build().writeTo(it)
        }
    }

    fun toDataClass(complexType: ComplexType, explicitName: String? = null): TypeSpec {
        val className = explicitName ?: complexType.name
        return TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA).also { t ->
                t.primaryConstructor(FunSpec.constructorBuilder().also { c ->
                    // Process attributes
                    complexType.attributeOrAttributeGroup?.forEach { attr ->
                        when (attr) {
                            is Attribute -> {
                                val attrType = attr.type ?: QName("http://www.w3.org/2001/XMLSchema", "string")
                                val kotlinType = getType(attrType)
                                val isOptional = attr.use != "required"
                                val paramType = if (isOptional) kotlinType.asTypeName().copy(nullable = true) else kotlinType.asTypeName()
                                
                                val paramBuilder = ParameterSpec.builder(attr.name, paramType)
                                if (isOptional) {
                                    paramBuilder.defaultValue("null")
                                }
                                c.addParameter(paramBuilder.build())
                                
                                t.addProperty(
                                    PropertySpec.builder(attr.name, paramType)
                                        .initializer(attr.name)
                                        .build()
                                )
                            }
                        }
                    }
                    
                    // Process choice elements
                    complexType.choice?.particle?.forEach {
                        when (val p = it) {
                            is JAXBElement<*> -> {
                                when (val e = p.value) {
                                    is LocalElement -> {
                                        addElementProperty(c, t, e)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Process sequence elements
                    complexType.sequence?.particle?.forEach {
                        when (val p = it) {
                            is JAXBElement<*> -> {
                                when (val e = p.value) {
                                    is LocalElement -> {
                                        addElementProperty(c, t, e)
                                    }
                                }
                            }
                        }
                    }
                }.build())
            }.build()
    }
    
    private fun addElementProperty(
        constructor: FunSpec.Builder,
        typeSpec: TypeSpec.Builder,
        element: LocalElement
    ) {
        val baseType = getType(element.type)
        val minOccurs = element.minOccurs?.toInt() ?: 1
        val maxOccurs = element.maxOccurs
        val isNillable = element.isNillable == true
        
        // Determine the Kotlin type based on cardinality
        val kotlinType = when {
            // maxOccurs > 1 or unbounded -> List
            maxOccurs == "unbounded" || (maxOccurs != null && maxOccurs.toIntOrNull()?.let { it > 1 } == true) -> {
                LIST.parameterizedBy(baseType.asTypeName())
            }
            // minOccurs = 0 or nillable -> nullable
            minOccurs == 0 || isNillable -> {
                baseType.asTypeName().copy(nullable = true)
            }
            // Required element
            else -> {
                baseType.asTypeName()
            }
        }
        
        val paramBuilder = ParameterSpec.builder(element.name, kotlinType)
        
        // Add default value for optional elements
        when {
            maxOccurs == "unbounded" || (maxOccurs != null && maxOccurs.toIntOrNull()?.let { it > 1 } == true) -> {
                if (minOccurs == 0) {
                    paramBuilder.defaultValue("emptyList()")
                }
            }
            minOccurs == 0 || isNillable -> {
                paramBuilder.defaultValue("null")
            }
        }
        
        constructor.addParameter(paramBuilder.build())
        
        typeSpec.addProperty(
            PropertySpec.builder(element.name, kotlinType)
                .initializer(element.name)
                .build()
        )
    }
    
    fun toEnum(enumName: String, values: List<String>): TypeSpec {
        return TypeSpec.enumBuilder(enumName).also { enumBuilder ->
            values.forEach { value ->
                // Convert enum value to valid Kotlin identifier
                val enumConstantName = value
                    .replace("-", "_")
                    .replace(".", "_")
                    .replace(" ", "_")
                    .uppercase()
                    .let { if (it.firstOrNull()?.isDigit() == true) "_$it" else it }
                
                enumBuilder.addEnumConstant(enumConstantName)
            }
        }.build()
    }

    fun getType(t: QName): KClass<*> {
        return when (t.localPart) {
            // String types
            "string", "normalizedString", "token" -> String::class
            "language", "Name", "NCName", "ID", "IDREF", "ENTITY", "NMTOKEN" -> String::class
            
            // Numeric types - signed
            "int", "integer" -> Int::class
            "long" -> Long::class
            "short" -> Short::class
            "byte" -> Byte::class
            "decimal" -> java.math.BigDecimal::class
            "positiveInteger", "negativeInteger", "nonPositiveInteger", "nonNegativeInteger" -> BigInteger::class
            
            // Numeric types - unsigned
            "unsignedLong" -> java.math.BigInteger::class  // ULong in Kotlin doesn't have a direct Java equivalent
            "unsignedInt" -> Long::class  // unsigned int fits in signed long
            "unsignedShort" -> Int::class  // unsigned short fits in signed int
            "unsignedByte" -> Short::class  // unsigned byte fits in signed short
            
            // Boolean
            "boolean" -> Boolean::class
            
            // Floating point
            "float" -> Float::class
            "double" -> Double::class
            
            // Date and time types
            "dateTime" -> LocalDateTime::class
            "date" -> LocalDate::class
            "time" -> LocalTime::class
            "duration" -> Duration::class
            
            // Binary types
            "hexBinary", "base64Binary" -> ByteArray::class
            
            // URI
            "anyURI" -> URI::class
            
            // QName and NOTATION - kept as String for simplicity
            "QName", "NOTATION" -> String::class
            
            else -> {
                logger.warn("Unsupported type '${t.localPart}', defaulting to String")
                String::class
            }
        }
    }
}

annotation class XDSProcessorAnnotation(val schema: String = "")