/*
 * Copyright (c) 2022-2024  Hendrik Horstmann
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

typealias JsonVersion = Pair<Int, Int>
private const val INVALID_VERSION_ERROR_MSG = "Ungültige Versionsbezeichnung! Eine Version besteht aus einer numerischen " +
        "Haupt- und Subversion, getrennt durch einen „.“!"
/**
 * Zerlegt einen Versions String bestehend aus numerischer Major- und Subversion,
 * getrennt durch einen Punkt
 */
class VersionDeserializer : JsonDeserializer<JsonVersion>() {
    @Throws(InvalidAttributeValueException::class)
    override fun deserialize(p: JsonParser, ctx: DeserializationContext): JsonVersion {
        val (first, second) = p.valueAsString.split('.').takeIf { it.size == 2 } ?:
            throw InvalidAttributeValueException(INVALID_VERSION_ERROR_MSG)

        return try {
            first.toInt() to second.toInt()
        } catch (e: NumberFormatException) {
            throw InvalidAttributeValueException(INVALID_VERSION_ERROR_MSG)
        }
    }

}
