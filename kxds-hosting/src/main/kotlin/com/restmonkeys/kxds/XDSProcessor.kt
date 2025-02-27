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
                logger.warn("Found schema: $schemaPath")
                File("$rootPath/$schemaPath").readText().also {
                    logger.warn(it)

                    generate(it)
                }
            }

        return emptyList()
    }

    fun generate(schema: String) {
        val context = JAXBContext.newInstance(Schema::class.java.packageName, Schema::class.java.classLoader)
        val result = context.createUnmarshaller()
            .unmarshal(schema.trim().byteInputStream())
        result as Schema
        result.simpleTypeOrComplexTypeOrGroup.forEach {
            when (it) {
                is TopLevelComplexType -> {
                    logger.warn(it.name)
                    toDataClass(it).toCode()
                }

                is TopLevelElement -> {
                    logger.warn(it.name)
                    it.complexType?.let { complexType ->
                        toDataClass(complexType).toCode()
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

    fun toDataClass(complexType: ComplexType): TypeSpec {
        return TypeSpec.classBuilder(complexType.name)
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
            else -> {
                Unit::class
            }
        }
    }
}

annotation class XDSProcessorAnnotation(val schema: String = "")