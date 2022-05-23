package data

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import getResource


fun main() {
    val gson = Gson()
    println(gson.fromJson(getResource("dataStruct.json"), JsonDataStructure::class.java))

}
