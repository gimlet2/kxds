package com.restmonkeys.kxds.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("schema", "http://www.w3.org/2001/XMLSchema", "sx")
data class Schema(
    @XmlElement(false)
    val targetNamespace: String,
    @XmlElement(false)
    val elementFormDefault: String,
    val element: Element
)

@XmlSerialName("element", "http://www.w3.org/2001/XMLSchema", "sx")
@Serializable
data class Element(
    @XmlElement(false)
    val name: String,
)

@XmlSerialName("complexType", "http://www.w3.org/2001/XMLSchema", "sx")
@Serializable
data class ComplexType(
    val sequence: Sequence
)

@XmlSerialName("sequence", "http://www.w3.org/2001/XMLSchema", "sx")
@Serializable
data class Sequence(
    val elements: List<SequenceElement>
)

@XmlSerialName("element", "http://www.w3.org/2001/XMLSchema", "sx")
@Serializable
data class SequenceElement(
    @XmlElement(false)
    val name: String,
    @XmlElement(false)
    val type: String,
)