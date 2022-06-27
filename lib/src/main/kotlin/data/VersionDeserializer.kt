/*
 * Copyright (c) 2022  Hendrik Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
