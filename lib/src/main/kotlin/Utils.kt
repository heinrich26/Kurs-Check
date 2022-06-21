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