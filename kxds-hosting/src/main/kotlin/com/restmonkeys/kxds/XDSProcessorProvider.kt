package com.restmonkeys.kxds

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for the XDSProcessor symbol processor.
 * 
 * This class is registered as a service provider and is used by KSP to instantiate
 * the XDSProcessor when processing Kotlin code.
 */
class XDSProcessorProvider : SymbolProcessorProvider {
    /**
     * Creates a new instance of XDSProcessor with the provided environment.
     * 
     * @param environment The symbol processor environment containing configuration and utilities
     * @return A new XDSProcessor instance
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return XDSProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}