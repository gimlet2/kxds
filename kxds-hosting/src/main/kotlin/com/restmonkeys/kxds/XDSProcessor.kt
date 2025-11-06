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
import org.w3._2001.xmlschema.Pattern
import org.w3._2001.xmlschema.Restriction
import org.w3._2001.xmlschema.Schema
import org.w3._2001.xmlschema.TopLevelComplexType
import org.w3._2001.xmlschema.TopLevelElement
import org.w3._2001.xmlschema.TopLevelSimpleType
import org.w3._2001.xmlschema.TotalDigits
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
                        } else {
                            // Handle non-enumeration restrictions (e.g., pattern, minLength, maxLength, etc.)
                            val baseType = restriction.base
                            if (baseType != null && shouldGenerateValueClass(baseType, restriction)) {
                                logger.info("Generating value class for simple type: ${it.name} with restrictions")
                                toValueClass(it.name, restriction).toCode()
                            }
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
    
    /**
     * Determine if we should generate a value class for this restriction
     * Currently supports: string, decimal, and dateTime base types
     */
    fun shouldGenerateValueClass(baseType: QName, restriction: Restriction): Boolean {
        val baseName = baseType.localPart
        val facets = restriction.facets ?: return false
        
        // Check if there are any relevant facets (excluding enumerations which are already handled)
        val hasRelevantFacets = facets.any { facet ->
            when (facet) {
                is JAXBElement<*> -> {
                    val facetName = facet.name.localPart
                    facetName in listOf(
                        "pattern", "minLength", "maxLength", "length",
                        "minInclusive", "maxInclusive", "minExclusive", "maxExclusive",
                        "totalDigits", "fractionDigits"
                    )
                }
                is Pattern, is TotalDigits -> true
                else -> false
            }
        }
        
        if (!hasRelevantFacets) return false
        
        // Only generate for string, decimal, and dateTime types
        return baseName in listOf("string", "decimal", "dateTime")
    }
    
    /**
     * Generate a value class with validation annotations based on the restriction
     */
    fun toValueClass(className: String, restriction: Restriction): TypeSpec {
        val baseType = restriction.base ?: error("Restriction must have a base type")
        val kotlinType = getType(baseType)
        
        // Collect all facets first
        val facetMap = mutableMapOf<String, String>()
        restriction.facets?.forEach { facet ->
            when (facet) {
                is JAXBElement<*> -> {
                    val facetName = facet.name.localPart
                    val facetValue = when (val value = facet.value) {
                        is NoFixedFacet -> value.value
                        is Facet -> value.value
                        else -> null
                    }
                    if (facetValue != null) {
                        facetMap[facetName] = facetValue
                    }
                }
                is Pattern -> {
                    facetMap["pattern"] = facet.value
                }
                is TotalDigits -> {
                    facetMap["totalDigits"] = facet.value.toString()
                }
            }
        }
        
        // Create the value class with a single property
        val propertySpec = PropertySpec.builder("value", kotlinType.asTypeName())
            .initializer("value")
            .also { propertyBuilder ->
                // Add validation annotations based on collected facets
                addValidationAnnotations(propertyBuilder, facetMap, baseType.localPart)
            }
            .build()
        
        return TypeSpec.classBuilder(className)
            .addModifiers(KModifier.VALUE)
            .addAnnotation(JvmInline::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("value", kotlinType.asTypeName())
                    .build()
            )
            .addProperty(propertySpec)
            .build()
    }
    
    /**
     * Add validation annotations to a property based on collected facets
     */
    private fun addValidationAnnotations(
        propertyBuilder: PropertySpec.Builder,
        facetMap: Map<String, String>,
        baseType: String
    ) {
        // Handle pattern
        facetMap["pattern"]?.let { pattern ->
            val patternAnnotation = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "Pattern"))
                .addMember("regexp = %S", pattern)
                .build()
            propertyBuilder.addAnnotation(patternAnnotation)
        }
        
        // Handle size constraints (length, minLength, maxLength)
        val length = facetMap["length"]?.toIntOrNull()
        val minLength = facetMap["minLength"]?.toIntOrNull()
        val maxLength = facetMap["maxLength"]?.toIntOrNull()
        
        if (length != null) {
            // Exact length specified
            val sizeAnnotation = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "Size"))
                .addMember("min = %L, max = %L", length, length)
                .build()
            propertyBuilder.addAnnotation(sizeAnnotation)
        } else if (minLength != null || maxLength != null) {
            // Min and/or max length specified
            val sizeBuilder = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "Size"))
            if (minLength != null) {
                sizeBuilder.addMember("min = %L", minLength)
            }
            if (maxLength != null) {
                if (minLength != null) {
                    sizeBuilder.addMember("max = %L", maxLength)
                } else {
                    sizeBuilder.addMember("max = %L", maxLength)
                }
            }
            propertyBuilder.addAnnotation(sizeBuilder.build())
        }
        
        // Handle decimal range constraints
        when (baseType) {
            "decimal" -> {
                // Handle min constraints
                facetMap["minInclusive"]?.let { minValue ->
                    val minAnnotation = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "DecimalMin"))
                        .addMember("value = %S", minValue)
                        .build()
                    propertyBuilder.addAnnotation(minAnnotation)
                }
                
                facetMap["minExclusive"]?.let { minValue ->
                    val minAnnotation = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "DecimalMin"))
                        .addMember("value = %S", minValue)
                        .addMember("inclusive = false")
                        .build()
                    propertyBuilder.addAnnotation(minAnnotation)
                }
                
                // Handle max constraints
                facetMap["maxInclusive"]?.let { maxValue ->
                    val maxAnnotation = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "DecimalMax"))
                        .addMember("value = %S", maxValue)
                        .build()
                    propertyBuilder.addAnnotation(maxAnnotation)
                }
                
                facetMap["maxExclusive"]?.let { maxValue ->
                    val maxAnnotation = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "DecimalMax"))
                        .addMember("value = %S", maxValue)
                        .addMember("inclusive = false")
                        .build()
                    propertyBuilder.addAnnotation(maxAnnotation)
                }
                
                // Handle digit constraints
                val totalDigits = facetMap["totalDigits"]?.toIntOrNull()
                val fractionDigits = facetMap["fractionDigits"]?.toIntOrNull()
                
                if (totalDigits != null || fractionDigits != null) {
                    val digitsBuilder = AnnotationSpec.builder(ClassName("jakarta.validation.constraints", "Digits"))
                    if (totalDigits != null && fractionDigits != null) {
                        digitsBuilder.addMember("integer = %L, fraction = %L", totalDigits - fractionDigits, fractionDigits)
                    } else if (totalDigits != null) {
                        digitsBuilder.addMember("integer = %L, fraction = 0", totalDigits)
                    } else if (fractionDigits != null) {
                        digitsBuilder.addMember("integer = 999, fraction = %L", fractionDigits)
                    }
                    propertyBuilder.addAnnotation(digitsBuilder.build())
                }
            }
            "dateTime", "date", "time" -> {
                // For date/time types, log the constraints as comments
                facetMap["minInclusive"]?.let { logger.info("MinInclusive constraint on $baseType: $it") }
                facetMap["maxInclusive"]?.let { logger.info("MaxInclusive constraint on $baseType: $it") }
                facetMap["minExclusive"]?.let { logger.info("MinExclusive constraint on $baseType: $it") }
                facetMap["maxExclusive"]?.let { logger.info("MaxExclusive constraint on $baseType: $it") }
            }
        }
    }
}

annotation class XDSProcessorAnnotation(val schema: String = "")