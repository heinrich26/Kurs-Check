package com.kurswahlApp.data

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import javax.management.InvalidAttributeValueException

/**
 * Zerlegt einen Versions String bestehend aus numerischer Major- und Subversion,
 * getrennt durch einen Punkt
 */
class VersionDeserializer : JsonDeserializer<Pair<Int, Int>>() {
    @Throws(InvalidAttributeValueException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Pair<Int, Int> {
        val str = p.valueAsString

        if (str.count { it == '.' } != 1)
            throw InvalidAttributeValueException("Ungültige Versionsbezeichnung! Eine Version besteht aus einer numerischen Haupt- und Subversion, getrennt durch einen \".\"!")

        return str.split(".", ignoreCase = true).let {
            try {
                it[0].toInt() to it[1].toInt()
            } catch (e: NumberFormatException) {
                throw InvalidAttributeValueException("Ungültige Versionsbezeichnung! Eine Version besteht aus einer numerischen Haupt- und Subversion, getrennt durch einen \".\"!")
            }
        }
    }

}
