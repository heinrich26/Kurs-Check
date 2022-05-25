package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import getResource


fun main() {
//    println(Gson().fromJson(getResource("dataStruct.json"), JsonDataStructure::class.java).toFachData())
    val builder = GsonBuilder()
    builder.registerTypeAdapterFactory(RegelAdapterFactory())
    val gson = builder.create()
    println(gson.fromJson(getResource("regeln.json"), Regeln::class.java))
}
