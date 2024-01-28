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
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL


/**
 * Gibt den Inhalt der angeforderten Ressource zurück
 * @param fileName Name/Pfad der Datei
 */
fun getResource(fileName: String): String? = {}.javaClass.classLoader.getResource(fileName)?.readText()

/**
 * Gibt die URL der angeforderten Ressource zurück
 * @param fileName Name/Pfad der Datei
 */
fun getResourceURL(fileName: String): URL? = {}.javaClass.classLoader.getResource(fileName)

/**
 * [ObjectMapper] für das laden von [FachData], welcher Kommentare erlaubt und unbekannte Properties zulässt.
 */
fun fachdataObjectMapper() = jacksonObjectMapper().apply {
    factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

/**
 * Ließt die `dataStruct.json` als [FachData] Objekt ein
 */
@Deprecated("Läd die gehardcodedte FachData und keine Schulspezifischen Daten, TESTING ONLY")
fun readDataStruct(): FachData = fachdataObjectMapper().readValue(getResourceURL("dataStruct.json")!!)

/**
 * Returns a list of `null`s of the given type with the given [size].
 */
inline fun <reified T> listOfNulls(size: Int): List<T?> = arrayOfNulls<T?>(size).toList()