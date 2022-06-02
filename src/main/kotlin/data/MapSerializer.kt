package data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class MapSerializer: JsonSerializer<Map<Fach, *>>() {
    override fun serialize(value: Map<Fach, *>, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeObject(value.mapKeys { it.key.kuerzel })
    }
}