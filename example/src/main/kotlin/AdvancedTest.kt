package org.restmonkeys

import com.restmonkeys.kxds.XDSProcessorAnnotation

@XDSProcessorAnnotation(schema = "advanced-schema.xsd")
fun testAdvancedTypes() {
    // This will trigger the generation of:
    // - EmailAddress value class with @Pattern annotation
    // - Username value class with @Size annotation  
    // - Percentage value class with @DecimalMin and @DecimalMax annotations
    // - Price value class with @Digits annotation
    // - User data class
}
