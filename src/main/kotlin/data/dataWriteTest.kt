package data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import testKurswahl

fun main() {
//    val mapper = ObjectMapper().registerModule(kotlinModule())
//        .registerModule(SimpleModule().addSerializer(Fach::class.java, FachSerializer()))
    val mapper = jacksonObjectMapper()

    val out = mapper.writeValueAsString(testKurswahl)

    println(out)
}