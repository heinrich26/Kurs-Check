package data

import com.google.gson.Gson
import getResource


fun main() {
    println(Gson().fromJson(getResource("dataStruct.json"), JsonDataStructure::class.java).toFachData())

}
