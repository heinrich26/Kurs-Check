package data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import testKurswahl

fun main() {
    val mapper = jacksonObjectMapper()

    val out = mapper.writeValueAsString(testKurswahl)

    println(out)
}