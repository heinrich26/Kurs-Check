package data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class FachSerializer : JsonSerializer<Fach>() {
    override fun serialize(value: Fach?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null)
            gen.writeNull()
        else gen.writeString(value.kuerzel)
    }
}