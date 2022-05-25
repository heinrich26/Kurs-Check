package data

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class RegelAdapterFactory: TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        return when (type.rawType) {
            RegelHolder::class.java -> object : TypeAdapter<T>() {
                override fun write(out: JsonWriter?, value: T) {}

                override fun read(reader: JsonReader): T? {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull()
                        return null
                    }

                    reader.beginObject()

                    if (reader.nextName() != "type") throw NoSuchElementException("Der Wert von 'type' muss immer vor 'value' stehen!")

                    val classType = reader.nextString()
                    reader.nextName()
                    val holder = RegelHolder(regel = gson.fromJson(reader, when (classType) {
                        "Wildcard" -> WildcardRegel::class.java
                        "IfThen" -> IfThenRegel::class.java
                        "Kuerzel" -> KuerzelRegel::class.java
                        else -> return null
                    })) as T
                    reader.endObject()
                    return holder
                }
            }
            else -> null
        }
    }
}