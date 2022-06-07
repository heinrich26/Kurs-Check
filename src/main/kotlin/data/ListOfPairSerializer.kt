package data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider


class ListOfPairSerializer : JsonSerializer<List<Pair<*, *>>>() {
    override fun serialize(value: List<Pair<*, *>>?, gen: JsonGenerator, serializers: SerializerProvider?) {
        if (value == null) gen.writeNull()
        else gen.writeObject(value.toMap())
    }

}
