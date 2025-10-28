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
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import org.w3._2001.xmlschema.ComplexType
import org.w3._2001.xmlschema.LocalElement
import org.w3._2001.xmlschema.Schema
import org.w3._2001.xmlschema.TopLevelComplexType
import org.w3._2001.xmlschema.TopLevelElement
import java.io.File
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
                    complexType.choice?.particle?.forEach {
                        when (val p = it) {
                            is JAXBElement<*> -> {
                                when (val e = p.value) {
                                    is LocalElement -> {
                                        c.addParameter(e.name, getType(e.type)).build()
                                        t.addProperty(
                                            PropertySpec.builder(e.name, getType(e.type))
                                                .initializer(e.name)
                                                .build()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    complexType.sequence?.particle?.forEach {
                        when (val p = it) {
                            is JAXBElement<*> -> {
                                when (val e = p.value) {
                                    is LocalElement -> {
                                        c.addParameter(e.name, getType(e.type)).build()
                                        t.addProperty(
                                            PropertySpec.builder(e.name, getType(e.type))
                                                .initializer(e.name)
                                                .build()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }.build())
            }.build()

    }

    fun getType(t: QName): KClass<*> {
        return when (t.localPart) {
            "string" -> String::class
            "int", "integer" -> Int::class
            "long" -> Long::class
            "short" -> Short::class
            "byte" -> Byte::class
            "boolean" -> Boolean::class
            "float" -> Float::class
            "double" -> Double::class
            "decimal" -> java.math.BigDecimal::class
            else -> {
                logger.warn("Unsupported type '${t.localPart}', defaulting to String")
                String::class
            }
        }
    }
}

annotation class XDSProcessorAnnotation(val schema: String = "")