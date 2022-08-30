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

package com.kurswahlApp

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kurswahlApp.data.Consts.FILETYPE_EXTENSION
import com.kurswahlApp.data.FachData
import java.io.File
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.filechooser.FileFilter


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
 * Ließt die `dataStruct.json` als [FachData] Objekt ein
 */
@Deprecated("Läd die gehardcodedte FachData und keine Schulspezifischen Daten, TESTING ONLY")
fun readDataStruct(): FachData {
    val mapper = jacksonObjectMapper()
    mapper.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
    return mapper.readValue(getResourceURL("dataStruct.json")!!)
}


/** Erstellt ein [ImageIcon] mit dem gegebenen [path] und einer optionalen [description]. */
fun createImageIcon(path: String, description: String? = null): ImageIcon? {
    val imgURL: URL? = getResourceURL(path)
    return if (imgURL != null) {
        ImageIcon(imgURL, description)
    } else {
        System.err.println("Couldn't find file: $path")
        null
    }
}

object KurswahlFileFilter : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || f.extension == FILETYPE_EXTENSION

    override fun getDescription(): String = "Kurswahl Dateien (.$FILETYPE_EXTENSION)"
}

object PngFileFilter : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || f.extension == "png"

    override fun getDescription(): String = "Png Dateien (.png)"
}


/**
 * Returns a list of `null`s of the given type with the given [size].
 */
inline fun <reified T> listOfNulls(size: Int): List<T?> = arrayOfNulls<T?>(size).toList()