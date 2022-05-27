package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import getResource


fun main() {
    val builder = GsonBuilder()
    builder.registerTypeAdapterFactory(RegelAdapterFactory())
    builder.setExclusionStrategies(AnnotationExclusionStrategy())
    val gson = builder.create()
    println(gson.fromJson(getResource("dataStruct.json"), JsonDataStructure::class.java).toFachData())
//    println(gson.fromJson(getResource("regeln.json"), Regeln::class.java))
}
