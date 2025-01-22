package org.restmonkeys

import Note
import com.restmonkeys.kxds.XDSProcessorAnnotation

@XDSProcessorAnnotation(schema = "schema.xsd")
fun main() {
    val note = Note(
        to = "Tove",
        from = "Jani",
        heading = "Reminder",
        body = "Don't forget me this weekend!",
    )
    println(note)
}

